package io.dchq.sdk.core.ipnat;

import java.util.Arrays;
import java.util.Collection;

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
import com.dchq.schema.beans.one.vpc.VirtualPrivateCloud;
import com.dchq.schema.beans.one.vpc.VpcIpPool;

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
	private VpcIpPool ipPool;
	private VpcIpPool ipPoolCreated;
	private boolean success;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for aprox 10 mints
	VirtualPrivateCloud createVPC;
	
	public IpNatCreateServiceTest(String ipPoolName, String mask, EntitlementType entitlementType, String vpcId, String drescription, boolean isprifix, boolean success) {
		ipPool = new VpcIpPool();
		NameEntityBase vpc = new NameEntityBase();
		String prifix = RandomStringUtils.randomAlphabetic(3);

		if (ipPoolName != null && !ipPoolName.isEmpty() && isprifix) {
			ipPoolName = (ipPoolName + prifix).toLowerCase();
		}
		ipPool.setName(ipPoolName);
		ipPool.setEntitlementType(entitlementType);
		ipPool.setMask(mask);
		vpc.setId(vpcId);
		ipPool.setVirtualPrivateCloud(vpc);
		this.success = success;

	}

	@Before
	public void setUp() throws Exception {
		ipnatService = ServiceFactory.buildIpNatService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { { "testipnat", "21", EntitlementType.PUBLIC, "2c9180875dee60a2015dee9da49101db", "test description", true,true } });
	}
	@Test
	public void createTest() throws Exception {
		logger.info("Create IP/Nat with name[{}] ", ipPool.getName());
		ResponseEntity<VpcIpPool> resultResponse = ipnatService.create(ipPool);
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
			Assert.assertEquals(ipPoolCreated.getName(), ipPool.getName());
			Assert.assertEquals(ipPoolCreated.getMask(), ipPool.getMask());
		} else {

			Assert.assertEquals(true, resultResponse.isErrors());
			Assert.assertEquals(null, resultResponse.getResults());
		}

	}

	@After
	public void cleanUp() throws Exception {
		if(this.ipPoolCreated !=null)
		{
			logger.info("cleaning up...");
			ResponseEntity<VpcIpPool> responseDelete = ipnatService.delete(ipPoolCreated.getId());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error IP/Nat deletion: [{}] ", message.getMessageText());
			}
		}
	}

}
