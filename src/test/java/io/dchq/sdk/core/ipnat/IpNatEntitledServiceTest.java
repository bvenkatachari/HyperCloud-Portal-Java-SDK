package io.dchq.sdk.core.ipnat;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

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
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.IpNatService;
import io.dchq.sdk.core.ServiceFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class IpNatEntitledServiceTest extends AbstractServiceTest {
	private IpNatService ipnatService;
	private IpNatService ipnatService1;
	private IpNatService ipnatService2;
	private IpPool ipPool;
	private IpPool ipPoolCreated;
	private boolean success;
	private boolean isEntitlementTypeUser ;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for aprox 10 mints

	public IpNatEntitledServiceTest(String ipPoolName, EntitlementType entitlementType, String ipPoolId, String drescription, boolean isprifix, boolean success) {
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
		ipnatService1 = ServiceFactory.buildIpNatService(rootUrl1, userId, password);
		ipnatService2 = ServiceFactory.buildIpNatService(rootUrl1, userId2, password2);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] {
			{ "testipnat",EntitlementType.OWNER, "402881845c9458a6015c945ac24c0004", "test description", true,true }
		});
	}
	
	@Ignore
	@Test
	public void findEntitleTest() {
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
				logger.info("Create IP/Nat Successfully..");
			}
			logger.info("IP/Nat state [{}]", ipPoolCreated.getIpStatus().name());
			while (ipPoolCreated.getIpStatus().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime)) {
				try {
					// sleep for some time
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
			if (ipPool.getEntitlementType().equals(EntitlementType.OWNER)) {
				ResponseEntity<IpPool> resultResponse1 = ipnatService1.findById(ipPoolCreated.getId());
				for (Message message : resultResponse1.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				////User may not be entitled to the IP/Nat
				Assert.assertNotNull(((Boolean) true).toString(), ((Boolean) resultResponse1.isErrors()).toString());
				Assert.assertEquals(null, resultResponse1.getResults());
				
			} else if (ipPool.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				
				ResponseEntity<IpPool> resultResponse1 = ipnatService1.findById(ipPoolCreated.getId());
				for (Message message : resultResponse1.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) resultResponse1.isErrors()).toString());
				assertNotNull(resultResponse1.getResults());
				assertNotNull(resultResponse1.getResults().getId());
				assertEquals(ipPoolCreated.getId(), resultResponse1.getResults().getId());

			} else if (ipPool.getEntitlementType().equals(EntitlementType.CUSTOM)) {
				
				ResponseEntity<IpPool> resultResponse1 = ipnatService1.findById(ipPoolCreated.getId());
				for (Message message : resultResponse1.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) resultResponse1.isErrors()).toString());
				assertNotNull(resultResponse1.getResults());
				assertNotNull(resultResponse1.getResults().getId());
				assertEquals(ipPoolCreated.getId(), resultResponse1.getResults().getId());
				
			} else if (ipPool.getEntitlementType().equals(EntitlementType.CUSTOM) && !isEntitlementTypeUser) {
				ResponseEntity<IpPool> resultResponseForGroupUser2 = ipnatService2.findById(ipPoolCreated.getId());
				for (Message message : resultResponseForGroupUser2.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) resultResponseForGroupUser2.isErrors()).toString());
				assertNotNull(resultResponseForGroupUser2.getResults());
				assertNotNull(resultResponseForGroupUser2.getResults().getId());
				assertEquals(ipPoolCreated.getId(), resultResponseForGroupUser2.getResults().getId());
				
			}

		} else {

			Assert.assertEquals(true, resultResponse.isErrors());
			Assert.assertEquals(null, resultResponse.getResults());
		}
	}

	@After
	public void cleanUp() {
		if (this.ipPoolCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<IpPool> responseDelete = ipnatService.delete(ipPoolCreated.getId());
			//Assert.assertEquals(false, responseDelete.isErrors());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error IP/Nat deletion: [{}] ", message.getMessageText());
			}
		}
	}


}
