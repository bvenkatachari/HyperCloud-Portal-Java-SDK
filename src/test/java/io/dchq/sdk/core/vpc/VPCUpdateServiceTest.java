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
public class VPCUpdateServiceTest extends AbstractServiceTest {
	
	private VPCService vpcService;

	VirtualPrivateCloud createVPC;
	VirtualPrivateCloud createdVPC;
	VirtualPrivateCloud updatedVpc;
	
	String nameForEdit;
	String ipv4CidrForEdit;
	boolean sussess;
	long startTime = System.currentTimeMillis();
	long endTime = startTime +(60 * 60 * 160); // this is for aprox 10 mints
	
	public VPCUpdateServiceTest(String vpcName, String nameForEdit, String providerId, EntitlementType entitlementType, String ipv4Cidr, String ipv4CidrForEdit, String description,
			String firewallIp, String firewallUsername, String firewallPassword,boolean isprifix, boolean success) {
		String prifix = RandomStringUtils.randomAlphabetic(3);

		if (vpcName != null && !vpcName.isEmpty() && isprifix) {
			vpcName = (vpcName + prifix).toLowerCase();
			this.nameForEdit = (nameForEdit + prifix).toLowerCase();
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
		this.ipv4CidrForEdit = ipv4CidrForEdit;
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
			// provider id "8a818a105c83f42a015c83fd71240014" Intesar's machine
			{"testvpc","testvpcupdate", "2c9180865d312fc4015d314da1ca006a", EntitlementType.OWNER, "10.0.0.0/24","10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",true, true},
			{"testvpc", "testvpcupdated", "2c9180865d312fc4015d314da1ca006a", EntitlementType.PUBLIC, "10.0.0.0/24","10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",true, true},
			{"testvpc","testvpctest", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0.0.0/24","10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",true, true},
			// Bug in system IPV4 CIDR field should not be editing
			//{"testvpc","testvpctest", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0.0.0/24","10.0.0.0/20", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",true, true},
			// Negative scenario, passing empty/null for name
			{"","", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0.0.0/24", "", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",false, false},
			{null, "", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0.0.0/24", "", "descriptions test", "10.100.15.102","PFFAadmin123","password-hidden",false ,false},
			{"testvpccc", "sadsadad", null, EntitlementType.CUSTOM, "10.0.0.0/24","10.0.0.0/22", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",true, false},
		});
	}
	
	@Ignore
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
			//*********************** Edit vpc name **********************
			createdVPC.setName(this.nameForEdit);
			ResponseEntity<VirtualPrivateCloud> resultFindResponse = vpcService.update(createdVPC);
			
			Assert.assertNotNull(resultFindResponse);
			Assert.assertEquals(false, resultFindResponse.isErrors());
			
			if(resultFindResponse.getResults() != null && !resultFindResponse.isErrors())
			{
				this.updatedVpc = resultFindResponse.getResults();
				logger.info("Create VPC Successful..");
			}
			Assert.assertEquals(updatedVpc.getName(), createdVPC.getName());
			//********************** End edit vpc ***************************
			//***********************Edit IPv4 ******************************
			if(this.ipv4CidrForEdit != null && !this.ipv4CidrForEdit.isEmpty() && !this.ipv4CidrForEdit.equals(createdVPC.getIpv4Cidr()))
			{
				createdVPC.setIpv4Cidr(this.ipv4CidrForEdit);
				resultFindResponse = vpcService.update(createdVPC);
				Assert.assertNotNull(resultFindResponse);
				Assert.assertEquals(true, resultFindResponse.isErrors());
				
			}
			//**************************** End edit ipv4 ********************
			

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
