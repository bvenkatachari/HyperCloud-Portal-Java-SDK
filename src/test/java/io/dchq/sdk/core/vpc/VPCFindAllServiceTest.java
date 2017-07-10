package io.dchq.sdk.core.vpc;

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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vpc.VirtualPrivateCloud;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.VPCService;

/**
 * 
 * @author Jagdeep Jain
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class VPCFindAllServiceTest extends AbstractServiceTest {
	private VPCService vpcService;

	VirtualPrivateCloud createVPC;
	VirtualPrivateCloud createdVPC;
	private int countBeforeCreate = 0, countAfterCreate = 0;
	boolean sussess;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mints

	public VPCFindAllServiceTest(String vpcName, String providerId, EntitlementType entitlementType, String ipv4Cidr, String description,boolean success) {
		String prifix = RandomStringUtils.randomAlphabetic(3);

		if (vpcName != null && !vpcName.isEmpty()) {
			vpcName = vpcName + "-" + prifix;
		}
		createVPC = new VirtualPrivateCloud();
		createVPC.setName(vpcName);
		createVPC.setEntitlementType(entitlementType);
		createVPC.setIpv4Cidr(ipv4Cidr);
		NameEntityBase entity = new NameEntityBase();
		entity.withId(providerId);
		createVPC.setProvider(entity);
		createVPC.setDescription(description);
		this.sussess = success;
	}

	@Before
	public void setUp() {
		vpcService = ServiceFactory.buildVPCService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			{"testvpc", "8a818a105c83f42a015c83fd71240014", EntitlementType.OWNER, "10.0.0.0/24", "descriptions test" , true},
			{"testvpcc", "8a818a105c83f42a015c83fd71240014", EntitlementType.PUBLIC, "10.0.0.0/24", "descriptions test" , true},
			{"testvpccc", "8a818a105c83f42a015c83fd71240014", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , true},
			{"", "8a818a105c83f42a015c83fd71240014", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , false},
			{null, "8a818a105c83f42a015c83fd71240014", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" ,false},
			{"@@@@@@@@@@@@@@@@@@@@@@@@@@", "8a818a105c83f42a015c83fd71240014", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , false},
			{"1111111111111111111111", "8a818a105c83f42a015c83fd71240014", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , false},
			{"testvpccc", "sssssssssssssssssssss", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , false},
			{"testvpccc", "", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , false},
			{"testvpccc", null, EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , false},
			{"testvpccc", "8a818a105c83f42a015c83fd71240014", EntitlementType.CUSTOM, "10.0.0.2/11", "descriptions test" , false},
			{"testvpccc", "8a818a105c83f42a015c83fd71240014", EntitlementType.CUSTOM, "172.0.0.0/24", "descriptions test" , false},
			{"testvpccc", "8a818a105c83f42a015c83fd71240014", null, "10.0.0.0/24", "descriptions test" , false},
		});
	}
	public int testVPCPosition(String id) {
		ResponseEntity<List<VirtualPrivateCloud>> response = vpcService.findAll(0, 500);
		assertNotNull(response);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}]  " + message.getMessageText());
		}
		assertNotNull(response.isErrors());
		assertEquals(false, response.isErrors());
		int position = 0;
		if (id != null) {
			for (VirtualPrivateCloud obj : response.getResults()) {
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
		logger.info("Create VPC name[{}] ", createVPC.getName());
		countBeforeCreate = testVPCPosition(null);
		ResponseEntity<VirtualPrivateCloud> response = vpcService.create(createVPC);
		Assert.assertNotNull(response);
		for (Message msg : response.getMessages()) {
			logger.warn("Error [{}]  " + msg.getMessageText());
		}
		
		if (this.sussess) {
			Assert.assertEquals(false, response.isErrors());
			Assert.assertNotNull(response.getResults());
			if (response.getResults() != null && !response.isErrors()) {
				this.createdVPC = response.getResults();
				logger.info("Create VPC Successful..");
			}
			logger.info("VPC state [{}]", createdVPC.getState().name());
			while(createdVPC.getState().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime))
			{
				try {
					// sleep for some time
					Thread.sleep(10000);
					response = vpcService.findById(createdVPC.getId());
					Assert.assertEquals(false, response.isErrors());
					Assert.assertNotNull(response.getResults());
					this.createdVPC = response.getResults();
				} catch (InterruptedException e) {
					// ignore
				}
				
			}
			logger.info("VPC state [{}]", createdVPC.getState().name());
			countAfterCreate = testVPCPosition(createdVPC.getId());
			assertEquals(countBeforeCreate + 1, countAfterCreate);

		} else {

			Assert.assertEquals(true, response.isErrors());
			Assert.assertEquals(null, response.getResults());
		}
	}

	@After
	public void cleanUp() {
		if(this.createdVPC !=null)
		{
			logger.info("cleaning up...");
			ResponseEntity<VirtualPrivateCloud> responseDelete = vpcService.delete(createdVPC.getId());
			Assert.assertEquals(false, responseDelete.isErrors());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error VPC deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
