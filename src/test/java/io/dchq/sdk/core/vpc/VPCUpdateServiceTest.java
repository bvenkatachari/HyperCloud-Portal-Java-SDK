package io.dchq.sdk.core.vpc;

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

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.VPCService;

/**
 *
 * @author Jagdeep Jain
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class VPCUpdateServiceTest extends AbstractServiceTest {
	
	private VPCService vpcService;

	VirtualPrivateCloud createVPC;
	VirtualPrivateCloud createdVPC;
	VirtualPrivateCloud updatedVpc;
	boolean sussess;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mints
	
	public VPCUpdateServiceTest(String vpcName, String providerId, EntitlementType entitlementType, String ipv4Cidr, String description, boolean isprifix, boolean success) {
		String prifix = RandomStringUtils.randomAlphabetic(3);

		if (vpcName != null && !vpcName.isEmpty() && isprifix) {
			vpcName = (vpcName + prifix).toLowerCase();
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
	public void setUp()
	{
		vpcService = ServiceFactory.buildVPCService(rootUrl, cloudadminusername, cloudadminpassword);
	}
	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][]{ 
			// provider id "8a818a105c83f42a015c83fd71240014" Intesar's machine
			{"testvpc", "2c9180865d312fc4015d314da1ca006a", EntitlementType.OWNER, "10.0.0.0/24", "descriptions test" , true, true},
			{"testvp@@@@@@@@@@", "2c9180865d312fc4015d314da1ca006a", EntitlementType.PUBLIC, "10.0.0.0/24", "descriptions test" , true, true},
			{"testvp2121212121", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , true, true},
			{"", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , false, false},
			{null, "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test", false ,false},
			// TODO should not accept only special character or numeric value 
			//{"@@@@@@@@@@@@@@@@@@@@@@@@@@", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , false, false},
			//{"1111111111111111111111", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , false, false},
			{"testvpccc", "sssssssssssssssssssss", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , true, false},
			{"testvpccc", "", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , true, false},
			{"testvpccc", null, EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , true, false},
			// TODO accept valid ip
			//{"testvpccc", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0", "descriptions test" , true, false},
			//{"testvpccc", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "", "descriptions test" , true, false},
			// TODO null EntitledType not accept
			//{"testvpccc", "2c9180865d312fc4015d314da1ca006a", null, "10.0.0.0/24", "descriptions test" , true, false},
		});
	}
	
	@Test
	public void updateTest()
	{

		logger.info("Create VPC name[{}] ", createVPC.getName());
		ResponseEntity<VirtualPrivateCloud> resultResponse = vpcService.create(createVPC);
		Assert.assertNotNull(resultResponse);

		for (Message msg : resultResponse.getMessages()) {
			logger.warn("Error [{}]  " + msg.getMessageText());
		}
		if (this.sussess) {
			Assert.assertEquals(false, resultResponse.isErrors());
			Assert.assertNotNull(resultResponse.getResults());

			if (resultResponse.getResults() != null && !resultResponse.isErrors()) {
				this.createdVPC = resultResponse.getResults();
				logger.info("Create VPC Successfull..");
			}
			logger.info("VPC state [{}]", createdVPC.getState().name());
			while(createdVPC.getState().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime))
			{
				try {
					// wait for some time
					Thread.sleep(10000);
					logger.info("VPC state [{}]", createdVPC.getState().name());
					resultResponse = vpcService.findById(createdVPC.getId());
					Assert.assertEquals(false, resultResponse.isErrors());
					Assert.assertNotNull(resultResponse.getResults());
					this.createdVPC = resultResponse.getResults();
				} catch (InterruptedException e) {
					// ignore
				}
			}
			logger.info("VPC state [{}]", createdVPC.getState().name());
			Assert.assertEquals("LIVE", createdVPC.getState().name());
			createdVPC.setName(createVPC.getName()+"-updated");
			ResponseEntity<VirtualPrivateCloud> resultFindResponse = vpcService.update(createdVPC);
			
			Assert.assertNotNull(resultFindResponse);
			Assert.assertEquals(false, resultFindResponse.isErrors());
			
			if(resultFindResponse.getResults() != null && !resultFindResponse.isErrors())
			{
				this.updatedVpc = resultFindResponse.getResults();
				logger.info("Create VPC Successful..");
			}
			
			Assert.assertEquals(updatedVpc.getName(), createdVPC.getName());

		} else {

			Assert.assertEquals(true, resultResponse.isErrors());
			Assert.assertEquals(null, resultResponse.getResults());
		}
	
	}
	@After
	public void cleanUp()
	{
		if(this.createdVPC !=null)
		{
			logger.info("cleaning up...");
			ResponseEntity<VirtualPrivateCloud> responseDelete = vpcService.delete(createdVPC.getId());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error vpc deletion: [{}] ", message.getMessageText());
			}
		}
	}

}
