package io.dchq.sdk.core.subnet;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.FixMethodOrder;
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
import com.dchq.schema.beans.one.vlan.VirtualNetwork;
import com.dchq.schema.beans.one.vpc.Subnet;

import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.SubnetService;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SubnetEntitledServiceTest extends SubnetUtil {

	SubnetService subnetService2;
	
	@org.junit.Before
	public void setUp() throws Exception {
		subnetService = ServiceFactory.buildSubnetService(rootUrl1, cloudadminusername, cloudadminpassword);
		subnetService2 = ServiceFactory.buildSubnetService(rootUrl1, username2, password2);
		securityGroupService = ServiceFactory.buildSecurityGroupService(rootUrl1, cloudadminusername, cloudadminpassword);
		networkACLService = ServiceFactory.buildNetworkACLService(rootUrl1, cloudadminusername, cloudadminpassword);
	}


	public SubnetEntitledServiceTest(String subnetName, String ipv4Cidr, String dhcp, String fromIpRange,
			String toIpRange, String dnsServers, EntitlementType entitlementType, 
			boolean isEntitlementTypeUser, String entitledUserId, boolean success) {
		
		
		String postfix = RandomStringUtils.randomAlphabetic(3);

		subnetName = subnetName + postfix;
		subnet = new Subnet();
		subnet.setEntitlementType(entitlementType);
		subnet.setName(subnetName);

		NameEntityBase vpc = new NameEntityBase();
		vpc.setId(vpcId);
		subnet.setVpc(vpc);

		VirtualNetwork virtualNetwork = new VirtualNetwork();
		virtualNetwork.setId(vlanId);
		subnet.setVirtualNetwork(virtualNetwork);

		subnet.setIpv4Cidr(ipv4Cidr);
		subnet.setDhcp(dhcp);
		subnet.setFromIpRange(fromIpRange);
		subnet.setToIpRange(toIpRange);
		subnet.setDnsServers(dnsServers);
		
		if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
			UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
			List<UsernameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			subnet.setEntitledUsers(entiledUsers);
		} else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
			NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
			List<NameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			subnet.setEntitledUserGroups(entiledUsers);
		}

		this.success = success;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			 { "subnet", "10.50.5.1/24", "true", "10.50.5.2", "10.50.5.254", "8.8.8.8", EntitlementType.OWNER, 
				          false, null, true },	 
			 { "subnet", "10.50.5.1/24", "true", "10.50.5.2", "10.50.5.254", "8.8.8.8", EntitlementType.PUBLIC, 
				          false, null, true } ,
			 { "subnet", "10.50.5.1/24", "true", "10.50.5.2", "10.50.5.254", "8.8.8.8", EntitlementType.CUSTOM, 
				          true, userId2, true },
			 { "subnet", "10.50.5.1/24", "true", "10.50.5.2", "10.50.5.254", "8.8.8.8", EntitlementType.CUSTOM, 
				          false, USER_GROUP, true } 
			});
	}

	
	@Test
	public void testEntitledSearch() {
		try {

			logger.info("Create Subnet name as [{}] ", subnet.getName());
			ResponseEntity<Subnet> response = subnetService.create(subnet);
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			if (success) {

				if (response.getResults() != null && !response.isErrors()) {
					this.subnetCreated = response.getResults();
					logger.info("Create Subnet sccessful..");
				}

				
				if (this.subnetCreated.getEntitlementType().equals(EntitlementType.OWNER)) {
					ResponseEntity<List<Subnet>> subnetSearchResponseEntity = subnetService2.search(subnet.getName(), 0, 1);
					for (Message message : subnetSearchResponseEntity.getMessages()) {
						logger.warn("Error while Search request  [{}] ", message.getMessageText());
					}
					assertNotNull(subnetSearchResponseEntity);
					assertNotNull(subnetSearchResponseEntity.isErrors());
					assertNotNull(subnetSearchResponseEntity.getResults());
					assertEquals(0, subnetSearchResponseEntity.getResults().size());
				}
				if (this.subnetCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
					ResponseEntity<List<Subnet>> subnetSearchResponseEntity = subnetService2.search(subnet.getName(), 0, 1);
					for (Message message : subnetSearchResponseEntity.getMessages()) {
						logger.warn("Error while Search request  [{}] ", message.getMessageText());
					}
					assertNotNull(subnetSearchResponseEntity);
					assertNotNull(subnetSearchResponseEntity.isErrors());
					assertNotNull(subnetSearchResponseEntity.getResults());
					assertEquals(1, subnetSearchResponseEntity.getResults().size());
				}
				if (this.subnetCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
					ResponseEntity<List<Subnet>> subnetSearchResponseEntity = subnetService2.search(subnet.getName(), 0, 1);
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
			fail(e.getMessage());
		}

	}

	@After
	public void cleanUp() {
				
		if (this.subnetCreated != null) {
			deleteNetworkACL();
			deleteSecurityGroup();
			logger.info("cleaning up Subnet...");
			ResponseEntity<?> response = subnetService.delete(this.subnetCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error Subnet deletion: [{}] ", message.getMessageText());
			}
		}

	}
}
