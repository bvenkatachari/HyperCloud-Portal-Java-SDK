package io.dchq.sdk.core.ipnat;

import static junit.framework.TestCase.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
public class IpNatSearchServiceTest extends AbstractServiceTest {
	private IpNatService ipnatService;
	private IpPool ipPool;
	private IpPool ipPoolCreated;
	private boolean success;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for aprox 10 mints

	public IpNatSearchServiceTest(String ipPoolName, EntitlementType entitlementType, String ipPoolId, String drescription, boolean isprifix, boolean success) {
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
			{ "testipnat",EntitlementType.OWNER, "402881845c9458a6015c945ac24c0004", "test description", true,true }});
	}

	@Ignore
	@Test
	public void searchTest() {
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
				logger.info("Create IP/Nat Successful..");
			}
			logger.info("IP/Nat state [{}]", ipPoolCreated.getIpStatus().name());
			while (ipPoolCreated.getIpStatus().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime)) {
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
			ResponseEntity<List<IpPool>> resultFindResponse = ipnatService.search(ipPoolCreated.getName(), 0, 1);
			Assert.assertNotNull(resultFindResponse);
			Assert.assertEquals(false, resultFindResponse.isErrors());
			assertNotNull(resultFindResponse.getResults());

			Assert.assertEquals(1, resultFindResponse.getResults().size());
			IpPool searchedEntity = resultFindResponse.getResults().get(0);

			Assert.assertEquals(ipPoolCreated.getId(), searchedEntity.getId());
			Assert.assertEquals(ipPoolCreated.getName(), searchedEntity.getName());

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
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error IP/Nat deletion: [{}] ", message.getMessageText());
			}
		}
	}

}
