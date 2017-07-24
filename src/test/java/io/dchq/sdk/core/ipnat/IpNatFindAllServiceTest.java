package io.dchq.sdk.core.ipnat;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.IpNatService;
import io.dchq.sdk.core.ServiceFactory;

public class IpNatFindAllServiceTest extends AbstractServiceTest {
	private IpNatService ipnatService;
	private IpPool ipPool;
	private IpPool ipPoolCreated;
	private boolean success;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for aprox 10 mints
	int countBeforeCreate;
	int countAfterCreate;

	public IpNatFindAllServiceTest(String ipPoolName, EntitlementType entitlementType, String ipPoolId, String drescription, boolean isprifix, boolean success) {
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
		return Arrays.asList(new Object[][] { { "testipnat",EntitlementType.OWNER, "402881845c9458a6015c945ac24c0004", "test description", true,true } });
	}

	public int testVPCPosition(String id) {
		ResponseEntity<List<IpPool>> response = ipnatService.findAll(0, 500);
		assertNotNull(response);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}]  " + message.getMessageText());
		}
		assertNotNull(response.isErrors());
		assertEquals(false, response.isErrors());
		int position = 0;
		if (id != null) {
			for (IpPool obj : response.getResults()) {
				position++;
				if (obj.getId().equals(id)) {
					logger.info("  Object Matched in FindAll {}  at Position : {}", id, position);
					assertEquals("Recently Created Object is not at Positon 1 :" + obj.getId(), 1, position);
				}
			}
		}
		logger.info(" Total Number of Objects :{}", response.getResults().size());
		return response.getResults().size();
	}
	@Ignore
	@Test
	public void findAllTest() {
		logger.info("Create IP/Nat name[{}] ", ipPool.getName());
		countBeforeCreate = testVPCPosition(null);
		ResponseEntity<IpPool> response = ipnatService.create(ipPool);
		Assert.assertNotNull(response);
		for (Message msg : response.getMessages()) {
			logger.warn("Error [{}]  " + msg.getMessageText());
		}

		if (this.success) {
			Assert.assertEquals(false, response.isErrors());
			Assert.assertNotNull(response.getResults());
			if (response.getResults() != null && !response.isErrors()) {
				this.ipPoolCreated = response.getResults();
				logger.info("Create IP/Nat Successful..");
			}
			logger.info("IP/Nat state [{}]", ipPoolCreated.getIpStatus().name());
			while (ipPoolCreated.getIpStatus().name().equals("PROVISIONING")
					&& (System.currentTimeMillis() < endTime)) {
				try {
					// sleep for some time
					Thread.sleep(10000);
					response = ipnatService.findById(ipPoolCreated.getId());
					Assert.assertEquals(false, response.isErrors());
					Assert.assertNotNull(response.getResults());
					this.ipPoolCreated = response.getResults();
				} catch (InterruptedException e) {
					// ignore
				}

			}
			logger.info("IP/Nat state [{}]", ipPoolCreated.getIpStatus().name());
			countAfterCreate = testVPCPosition(ipPoolCreated.getId());
			assertEquals(countBeforeCreate + 1, countAfterCreate);

		} else {

			Assert.assertEquals(true, response.isErrors());
			Assert.assertEquals(null, response.getResults());
		}
	}

	@After
	public void cleanUp() {
		if (this.ipPoolCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<IpPool> responseDelete = ipnatService.delete(ipPoolCreated.getId());
			// Assert.assertEquals(false, responseDelete.isErrors());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error IP/Nat deletion: [{}] ", message.getMessageText());
			}
		}
	}

}
