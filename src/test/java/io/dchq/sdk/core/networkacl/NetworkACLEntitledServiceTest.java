package io.dchq.sdk.core.networkacl;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
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
import com.dchq.schema.beans.one.vpc.NetworkACL;

import io.dchq.sdk.core.NetworkACLService;
import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class NetworkACLEntitledServiceTest extends NetworkACLTest {

	NetworkACLService networkACLService2;

	@org.junit.Before
	public void setUp() throws Exception {
		networkACLService = ServiceFactory.buildNetworkACLService(rootUrl, username, password);
		networkACLService2 = ServiceFactory.buildNetworkACLService(rootUrl, username2, password2);
	}

	public NetworkACLEntitledServiceTest(String networkACLName, EntitlementType entitlementType, boolean isEntitlementTypeUser, String entitledUserId,
			String subnetName,String vlanId, String ipv4Cidr, String dhcp, String fromIpRange, String toIpRange, String dnsServers,
			String vpcName, String providerId, boolean success) {

		String postfix = RandomStringUtils.randomAlphabetic(3);
		networkACLName = networkACLName + postfix;

		networkACL = new NetworkACL();
		networkACL.setName(networkACLName);
		networkACL.setEntitlementType(entitlementType);
		
		createdSubnet = getSubnet(subnetName, vlanId, ipv4Cidr, dhcp, fromIpRange, toIpRange, dnsServers, vpcName, providerId);
		NameEntityBase subnet = new NameEntityBase();
		subnet.setId(createdSubnet.getId());
		//subnet.setId(subnetId);
		
		networkACL.setSubnet(subnet);
		
		if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
			UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
			List<UsernameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			networkACL.setEntitledUsers(entiledUsers);
		} else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
			NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
			List<NameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			networkACL.setEntitledUserGroups(entiledUsers);
		}

		this.success = success;

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			{ "networkACL", EntitlementType.OWNER, false, null, "subnet","402881875cd3e674015cd4ca484501b4", "10.0.0.0/24", "true", 
				"10.0.0.2", "10.0.0.254", "8.8.8.8", "vpc", "8a818a105c83f42a015c83fd71240014", true },
			{ "networkACL", EntitlementType.PUBLIC, false, null, "subnet","402881875cd3e674015cd4ca484501b4", "10.0.0.0/24", "true", 
					"10.0.0.2", "10.0.0.254", "8.8.8.8", "vpc", "8a818a105c83f42a015c83fd71240014", true },
			{ "networkACL", EntitlementType.CUSTOM, true, userId2, "subnet","402881875cd3e674015cd4ca484501b4", "10.0.0.0/24", "true", 
					"10.0.0.2", "10.0.0.254", "8.8.8.8", "vpc", "8a818a105c83f42a015c83fd71240014", true },
			{ "networkACL", EntitlementType.CUSTOM, true, USER_GROUP, "subnet","402881875cd3e674015cd4ca484501b4", "10.0.0.0/24", "true", 
					"10.0.0.2", "10.0.0.254", "8.8.8.8", "vpc", "8a818a105c83f42a015c83fd71240014", true } 
			});
	}


	
    @Ignore
	@Test
	public void testEntitledSearch() {
		try {

			logger.info("Create Network ACL name as [{}] ", networkACL.getName());
			ResponseEntity<NetworkACL> response = networkACLService.create(networkACL);
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			if (success) {


				if (response.getResults() != null && !response.isErrors()) {
					this.networkACLCreated = response.getResults();
					logger.info("Create Network ACL service Successful..");
				}

				if (this.networkACLCreated.getEntitlementType().equals(EntitlementType.OWNER)) {
					ResponseEntity<List<NetworkACL>> subnetSearchResponseEntity = networkACLService2.search(networkACL.getName(), 0,1);
					for (Message message : subnetSearchResponseEntity.getMessages()) {
						logger.warn("Error while Search request  [{}] ", message.getMessageText());
					}
					assertNotNull(subnetSearchResponseEntity);
					assertNotNull(subnetSearchResponseEntity.isErrors());
					assertNotNull(subnetSearchResponseEntity.getResults());
					assertEquals(0, subnetSearchResponseEntity.getResults().size());
				}
				if (this.networkACLCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
					ResponseEntity<List<NetworkACL>> subnetSearchResponseEntity = networkACLService2.search(networkACL.getName(), 0,1);
					for (Message message : subnetSearchResponseEntity.getMessages()) {
						logger.warn("Error while Search request  [{}] ", message.getMessageText());
					}
					assertNotNull(subnetSearchResponseEntity);
					assertNotNull(subnetSearchResponseEntity.isErrors());
					assertNotNull(subnetSearchResponseEntity.getResults());
					assertEquals(1, subnetSearchResponseEntity.getResults().size());
				}
				if (this.networkACLCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
					ResponseEntity<List<NetworkACL>> subnetSearchResponseEntity = networkACLService2.search(networkACL.getName(), 0,1);
					for (Message message : subnetSearchResponseEntity.getMessages()) {
						logger.warn("Error while Search request  [{}] ", message.getMessageText());
					}
					assertNotNull(subnetSearchResponseEntity);
					assertNotNull(subnetSearchResponseEntity.isErrors());
					assertNotNull(subnetSearchResponseEntity.getResults());
					assertEquals(1, subnetSearchResponseEntity.getResults().size());
				}

			} else {
				assertEquals(null, response.getResults());
				assertEquals(true, response.isErrors());
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	@After
	public void cleanUp() {

		if (this.networkACLCreated != null) {
			logger.info("cleaning up Network ACL...");
			ResponseEntity<?> response = networkACLService.delete(this.networkACLCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error Network ACL deletion: [{}] ", message.getMessageText());
			}
		}

		if (this.createdSubnet != null) {
			logger.info("cleaning up Subnet...");
			ResponseEntity<?> response = subnetService.delete(this.createdSubnet.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error Subnet deletion: [{}] ", message.getMessageText());
			}
		}

		if (this.createdVPC != null) {
			logger.info("cleaning up VPC ...");
			ResponseEntity<?> response = vpcService.delete(this.createdVPC.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error VPC deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
