package io.dchq.sdk.core.ipnat;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.network.IpPool;
import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.IpNatService;
import io.dchq.sdk.core.ServiceFactory;

public class IpNatUpdateServiceTest extends AbstractServiceTest{
	private IpNatService ipnatService;
	private IpPool ipPool;
	private IpPool ipPoolCreated;
	private boolean success;
	private String nameForEdit ;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for aprox 10 mints
	
	public IpNatUpdateServiceTest(String ipPoolName, boolean isprifix, boolean success) {
		String prifix = RandomStringUtils.randomAlphabetic(3);
		ipPool = new IpPool();
		if (ipPoolName != null && !ipPoolName.isEmpty() && isprifix) {
			ipPoolName = (ipPoolName + prifix).toLowerCase();
		}
		ipPool.setName(ipPoolName);
		this.success = success;

	}
	@Before
	public void setUp() throws Exception {
		ipnatService = ServiceFactory.buildIpNatService(rootUrl1, cloudadminusername, cloudadminpassword);
	}
	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] {
			{"testipnat",true,true}});
	}
	@Ignore
	@Test
	public void updateTest()
	{

		logger.info("Create IP/Nat name[{}] ", ipPool.getName());
		ResponseEntity<IpPool> resultResponse = ipnatService.create(ipPool);
		Assert.assertNotNull(resultResponse);

		for (Message msg : resultResponse.getMessages()) {
			logger.warn("Error [{}]  " + msg.getMessageText());
		}
		if (this.success) {
			Assert.assertEquals(false, resultResponse.isErrors());
			Assert.assertNotNull(resultResponse.getResults());

			if (resultResponse.getResults() != null && !resultResponse.isErrors()) {
				this.ipPoolCreated = resultResponse.getResults();
				logger.info("Create IP/Nat Successfull..");
			}
			logger.info("IP/Nat state [{}]", ipPoolCreated.getIpStatus().name());
			while(ipPoolCreated.getIpStatus().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime))
			{
				try {
					// wait for some time
					Thread.sleep(10000);
					logger.info("IP/Nat state [{}]", ipPoolCreated.getIpStatus().name());
					resultResponse = ipnatService.findById(ipPoolCreated.getId());
					Assert.assertEquals(false, resultResponse.isErrors());
					Assert.assertNotNull(resultResponse.getResults());
					this.ipPoolCreated = resultResponse.getResults();
				} catch (InterruptedException e) {
					// ignore
				}
			}
			logger.info("IP/Nat state [{}]", ipPoolCreated.getIpStatus().name());
			Assert.assertEquals("LIVE", ipPoolCreated.getIpStatus().name());
			//*********************** Edit IP/Nat name **********************
			ipPoolCreated.setName(this.nameForEdit);
			ResponseEntity<IpPool> resultFindResponse = ipnatService.update(ipPoolCreated);
			
			Assert.assertNotNull(resultFindResponse);
			Assert.assertEquals(false, resultFindResponse.isErrors());
			
			if(resultFindResponse.getResults() != null && !resultFindResponse.isErrors())
			{
				this.ipPoolCreated = resultFindResponse.getResults();
				logger.info("Create IP/Nat Successful..");
			}
			Assert.assertEquals(ipPoolCreated.getName(), ipPoolCreated.getName());

		} else {

			Assert.assertEquals(true, resultResponse.isErrors());
			Assert.assertEquals(null, resultResponse.getResults());
		}
	
	}
	@After
	public void cleanUp()
	{
		if(this.ipPoolCreated !=null)
		{
			logger.info("cleaning up...");
			ResponseEntity<IpPool> responseDelete = ipnatService.delete(ipPoolCreated.getId());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error IP/Nat deletion: [{}] ", message.getMessageText());
			}
		}
	}



}
