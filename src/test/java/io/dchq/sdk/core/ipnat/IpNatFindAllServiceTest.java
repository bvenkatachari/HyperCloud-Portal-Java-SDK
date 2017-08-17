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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vpc.VpcIpPool;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.IpNatService;
import io.dchq.sdk.core.ServiceFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class IpNatFindAllServiceTest extends AbstractServiceTest {
	private IpNatService ipnatService;
	private VpcIpPool ipPool;
	private VpcIpPool ipPoolCreated;
	private boolean success;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for aprox 10 mints
	int countBeforeCreate;
	int countAfterCreate;

	public IpNatFindAllServiceTest(String ipPoolName, EntitlementType entitlementType, String mask, String vpcId, String drescription, boolean isprifix, boolean success) {
		ipPool = new VpcIpPool();
		String prifix = RandomStringUtils.randomAlphabetic(3);

		if (ipPoolName != null && !ipPoolName.isEmpty() && isprifix) {
			ipPoolName = (ipPoolName + prifix).toLowerCase();
		}
		ipPool.setName(ipPoolName);
		ipPool.setMask(mask);
		NameEntityBase namedEntityBased = new NameEntityBase();
		namedEntityBased.setId(vpcId);
		ipPool.setVirtualPrivateCloud(namedEntityBased);
		this.success = success;
	}

	@Before
	public void setUp() throws Exception {
		ipnatService = ServiceFactory.buildIpNatService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { { "testipnat",EntitlementType.OWNER, "21", "2c9180875dee60a2015dee9da49101db", "test description", true,true } });
	}

	public int testVPCPosition(String id) {
		ResponseEntity<List<VpcIpPool>> response = ipnatService.findAll(0, 500);
		assertNotNull(response);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}]  " + message.getMessageText());
		}
		assertNotNull(response.isErrors());
		assertEquals(false, response.isErrors());
		int position = 0;
		if (id != null) {
			for (VpcIpPool obj : response.getResults()) {
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
	@Test
	public void findAllTest() {
		logger.info("Create IP/Nat name[{}] ", ipPool.getName());
		countBeforeCreate = testVPCPosition(null);
		ResponseEntity<VpcIpPool> response = ipnatService.create(ipPool);
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
			ResponseEntity<VpcIpPool> responseDelete = ipnatService.delete(ipPoolCreated.getId());
			// Assert.assertEquals(false, responseDelete.isErrors());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error IP/Nat deletion: [{}] ", message.getMessageText());
			}
		}
	}

}
