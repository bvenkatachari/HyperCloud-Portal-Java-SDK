package io.dchq.sdk.core.ipnat;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.network.IpPool;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.IpNatService;
import io.dchq.sdk.core.ServiceFactory;

/**
 * 
 * @author msys
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class IpNatCreateServiceTest extends AbstractServiceTest {

	private IpNatService ipnatService;
	private IpPool ipPool;
	private IpPool ipPoolCreated;
	private boolean success;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for aprox 10 mints

	public IpNatCreateServiceTest(String ipPoolName, boolean isprifix, boolean success) {
		ipPool = new IpPool();
		String prifix = RandomStringUtils.randomAlphabetic(3);

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
		return Arrays.asList(new Object[][] { { "testipnat",true,true } });
	}

	@Ignore
	@Test
	public void createTest() throws Exception {

		logger.info("Create IP/Nat with name[{}] ", ipPool.getName());
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
				logger.info("Create IP/Nat Successfully..");
			}
			logger.info("VPC state [{}]", ipPoolCreated.getIpStatus().name());
			while (ipPoolCreated.getIpStatus().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime)) {
				try {
					// Sleep for some time
					Thread.sleep(10000);
					logger.info("VPC state[{}]", ipPoolCreated.getIpStatus().name());
					resultResponse = ipnatService.findById(ipPoolCreated.getId());
					Assert.assertEquals(false, resultResponse.isErrors());
					Assert.assertNotNull(resultResponse.getResults());
					this.ipPoolCreated = resultResponse.getResults();
				} catch (InterruptedException e) {
					// ignore
				}
			}
			logger.info("VPC state [{}]", ipPoolCreated.getIpStatus().name());
			Assert.assertEquals("LIVE", ipPoolCreated.getIpStatus().name());
			Assert.assertEquals(ipPoolCreated.getName(), ipPool.getName());

		} else {

			Assert.assertEquals(true, resultResponse.isErrors());
			Assert.assertEquals(null, resultResponse.getResults());
		}

	}

	@After
	public void cleanUp() throws Exception {

	}

}
