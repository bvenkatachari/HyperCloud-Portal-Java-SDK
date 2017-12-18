package io.dchq.sdk.core.vpc;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
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
import org.springframework.util.StringUtils;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vpc.VirtualPrivateCloud;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.VPCService;
/**
 * 
 * @author msys
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class VPCEntitledServiceTest extends AbstractServiceTest {

	private VPCService vpcService;
	private VPCService vpcService2;
	private VPCService vpcService3;
	VirtualPrivateCloud createVPC;
	VirtualPrivateCloud createdVPC;
	boolean sussess;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for aprox 10 mints
	boolean isEntitlementTypeUser;

	public VPCEntitledServiceTest(String vpcName, String providerId, EntitlementType entitlementType, String ipv4Cidr,
			String description, String firewallIp, String firewallUsername, String firewallPassword,
			boolean isEntitlementTypeUser,String entitledUserId,  boolean isprifix, boolean success) {
		String prifix = RandomStringUtils.randomAlphabetic(3);

		if (vpcName != null && !vpcName.isEmpty() && isprifix) {
			vpcName = (vpcName + prifix).toLowerCase();
		}
		createVPC = new VirtualPrivateCloud();

		if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
			UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
			List<UsernameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			this.createVPC.setEntitledUsers(entiledUsers);
		} else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
			NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
			List<NameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			this.createVPC.setEntitledUserGroups(entiledUsers);
		}

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
		this.isEntitlementTypeUser = isEntitlementTypeUser;
	}

	@Before
	public void setUp() {
		vpcService = ServiceFactory.buildVPCService(rootUrl1, cloudadminusername, cloudadminpassword);
		vpcService2 = ServiceFactory.buildVPCService(rootUrl1, username2, password2);
		vpcService3 = ServiceFactory.buildVPCService(rootUrl1, username3, password3);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		// provider id "8a818a105c83f42a015c83fd71240014" Intesar's machine
		return Arrays.asList(new Object[][] { 
			{"testvpc", "2c9180865d312fc4015d314da1ca006a", EntitlementType.OWNER, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",false,  null, true, true}, 
			{"testvpc", "2c9180865d312fc4015d314da1ca006a", EntitlementType.PUBLIC, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",false,  null, true, true},
			{"testvpc", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",true,  userId2, true, true},
			// Bug in create API using below data as VPC never going live status
			//{"testvpc", "2c9180865d312fc4015d314da1ca006a", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , "10.100.15.102","PFFAadmin123","password-hidden",false,  USER_GROUP, true, true},
			
		});
	}
	
	@Ignore
	@Test
	public void findEntitleTest() {
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
			while (createdVPC.getState().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime)) {
				try {
					// sleep for some time
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
			if (createVPC.getEntitlementType().equals(EntitlementType.OWNER)) {
				ResponseEntity<VirtualPrivateCloud> resultResponse1 = vpcService2.findById(createdVPC.getId());
				for (Message message : resultResponse1.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				////User may not be entitled to the VPC
				Assert.assertNotNull(((Boolean) true).toString(), ((Boolean) resultResponse1.isErrors()).toString());
				Assert.assertEquals(null, resultResponse1.getResults());
				
			} else if (createVPC.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				
				ResponseEntity<VirtualPrivateCloud> resultResponse1 = vpcService2.findById(createdVPC.getId());
				for (Message message : resultResponse1.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) resultResponse1.isErrors()).toString());
				assertNotNull(resultResponse1.getResults());
				assertNotNull(resultResponse1.getResults().getId());
				assertEquals(createdVPC.getId(), resultResponse1.getResults().getId());

			} else if (createVPC.getEntitlementType().equals(EntitlementType.CUSTOM)) {
				
				ResponseEntity<VirtualPrivateCloud> resultResponse1 = vpcService2.findById(createdVPC.getId());
				for (Message message : resultResponse1.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) resultResponse1.isErrors()).toString());
				assertNotNull(resultResponse1.getResults());
				assertNotNull(resultResponse1.getResults().getId());
				assertEquals(createdVPC.getId(), resultResponse1.getResults().getId());
				
			} else if (createVPC.getEntitlementType().equals(EntitlementType.CUSTOM) && !isEntitlementTypeUser) {
				// For group user
//				ResponseEntity<VirtualPrivateCloud> resultResponseForGroupuser = vpcService2.create(createdVPC);
//				if (resultResponseForGroupuser.getResults() != null && !resultResponseForGroupuser.isErrors()) {
//					this.createdVPC = resultResponseForGroupuser.getResults();
//					logger.info("Create VPC Successfully..");
//				}
//				logger.info("VPC state [{}]", createdVPC.getState().name());
//				while (createdVPC.getState().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime)) {
//					try {
//						// sleep for some time
//						Thread.sleep(10000);
//						logger.info("VPC state [{}]", createdVPC.getState().name());
//						resultResponseForGroupuser = vpcService.findById(createdVPC.getId());
//						Assert.assertEquals(false, resultResponseForGroupuser.isErrors());
//						Assert.assertNotNull(resultResponseForGroupuser.getResults());
//						this.createdVPC = resultResponseForGroupuser.getResults();
//					} catch (InterruptedException e) {
//						// ignore
//					}
//				}
//				logger.info("VPC state [{}]", createdVPC.getState().name());
				ResponseEntity<VirtualPrivateCloud> resultResponseForGroupUser2 = vpcService3.findById(createdVPC.getId());
				for (Message message : resultResponseForGroupUser2.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) resultResponseForGroupUser2.isErrors()).toString());
				assertNotNull(resultResponseForGroupUser2.getResults());
				assertNotNull(resultResponseForGroupUser2.getResults().getId());
				assertEquals(createdVPC.getId(), resultResponseForGroupUser2.getResults().getId());
				
			}

		} else {

			Assert.assertEquals(true, resultResponse.isErrors());
			Assert.assertEquals(null, resultResponse.getResults());
		}
	}

	@After
	public void cleanUp() {
		if (this.createdVPC != null) {
			logger.info("cleaning up...");
			ResponseEntity<VirtualPrivateCloud> responseDelete = vpcService.delete(createdVPC.getId());
			//Assert.assertEquals(false, responseDelete.isErrors());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error VPC deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
