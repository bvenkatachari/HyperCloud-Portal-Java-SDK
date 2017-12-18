package io.dchq.sdk.core.vpc;

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
public class VPCCreateServiceTest extends AbstractServiceTest {
	
	private VPCService vpcService;
	
	VirtualPrivateCloud createVPC;
	VirtualPrivateCloud createdVPC;
	boolean sussess;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for aprox 10 mints
	
	public VPCCreateServiceTest(String vpcName, String providerId, EntitlementType entitlementType, String ipv4Cidr, String description, 
			String firewallIp, String firewallUsername, String firewallPassword, boolean isprifix, boolean success)
	{
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
		createVPC.setFirewallIp(firewallIp);
		createVPC.setFirewallUsername(firewallUsername);
		createVPC.setFirewallPassword(firewallPassword);
		this.sussess = success;

	}
	@Before
	public void setUp()
	{
		vpcService = ServiceFactory.buildVPCService(rootUrl1, cloudadminusername, cloudadminpassword);
	}
	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][]{ 
			{"testvpc", "2c9180865d312fc4015d3160f6230092", EntitlementType.OWNER, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden", true, true},
			{"testvpc", "2c9180865d312fc4015d3160f6230092", EntitlementType.PUBLIC, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",true, true},
			{"testvpc", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",true, true},
			// Negative scenario, passing empty/null for name  
			{"", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",false, false},
			{null, "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test", "10.100.15.102","PFFAadmin123","password-hidden",false ,false},
			// Bug in create API accepting special character for VPC name, should accept only alphanumeric
			//{"@@@@@@@@@@@@@@@@@@@@@@@@@@", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",false, false},
			// Negative scenario for provider
			{"testvpccc", "sssssssssssssssssssss", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",true, false},
			{"testvpccc", "", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden", true, false},
			{"testvpccc", null, EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden", true, false},
			// Bug in create API accepting invalid IP address
			//{"testvpccc", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",true, false},
			//{"testvpccc", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",true, false},
			// Bug in create API accepting null EntitledType
			//{"testvpccc", "2c9180865d312fc4015d314da1ca006a", null, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",true, false},
		});
	}

	@Ignore
	@Test
	public void createTest()
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
				logger.info("Create VPC Successfully..");
			}
			logger.info("VPC state [{}]", createdVPC.getState().name());
			while(createdVPC.getState().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime))
			{
				try {
					// Sleep for some time
					Thread.sleep(10000);
					logger.info("VPC state[{}]", createdVPC.getState().name());
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
			Assert.assertEquals(createVPC.getName(), createdVPC.getName());
			Assert.assertEquals(createVPC.getProvider().getId(), createdVPC.getProvider().getId());
			Assert.assertEquals(createVPC.getIpv4Cidr(), createdVPC.getIpv4Cidr());
			Assert.assertEquals(createVPC.getFirewallIp(), createdVPC.getFirewallIp());
			Assert.assertEquals(createVPC.getFirewallUsername(), createdVPC.getFirewallUsername());
			Assert.assertEquals(createVPC.getFirewallPassword(), createdVPC.getFirewallPassword());

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
			//Assert.assertEquals(false, responseDelete.isErrors());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error VPC deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
