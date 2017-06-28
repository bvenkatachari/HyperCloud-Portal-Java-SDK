package io.dchq.sdk.core.subnet;

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
public class SubnetEntitledServiceTest extends SubnetTest {

	SubnetService subnetService2;
	
	@org.junit.Before
	public void setUp() throws Exception {
		subnetService = ServiceFactory.buildSubnetService(rootUrl, username, password);
		subnetService2 = ServiceFactory.buildSubnetService(rootUrl, username2, password2);
	}


	public SubnetEntitledServiceTest(String subnetName, String vlanId, String ipv4Cidr, String dhcp, String fromIpRange,
			String toIpRange, String dnsServers, EntitlementType entitlementType, 
			boolean isEntitlementTypeUser, String entitledUserId, String vpcName, String providerId, boolean success) {
		
		
		String postfix = RandomStringUtils.randomAlphabetic(3);

		subnetName = subnetName + postfix;
		subnet = new Subnet();
		subnet.setEntitlementType(entitlementType);
		subnet.setName(subnetName);

		//createdVPC = getVPC(vpcName, providerId, ipv4Cidr);
		NameEntityBase vpc = new NameEntityBase();
		//vpc.setId(createdVPC.getId());
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
			 { "subnet", "402881875cd3e674015cd4ca484501b4", "10.0.0.0/24", "true", "10.0.0.2", "10.0.0.254", "8.8.8.8", EntitlementType.OWNER, 
				          false, null, "vpc", "8a818a105c83f42a015c83fd71240014", true },
			 
			 { "subnet", "402881875cd3e674015cd4ca484501b4", "10.0.0.0/24", "true", "10.0.0.2", "10.0.0.254", "8.8.8.8", EntitlementType.PUBLIC, 
				          false, null, "vpc", "8a818a105c83f42a015c83fd71240014", true } ,
			 { "subnet", "402881875cd3e674015cd4ca484501b4", "10.0.0.0/24", "true", "10.0.0.2", "10.0.0.254", "8.8.8.8", EntitlementType.CUSTOM, 
				          true, userId2, "vpc", "8a818a105c83f42a015c83fd71240014", true },
			 { "subnet", "402881875cd3e674015cd4ca484501b4", "10.0.0.0/24", "true", "10.0.0.2", "10.0.0.254", "8.8.8.8", EntitlementType.CUSTOM, 
				          true, USER_GROUP, "vpc", "8a818a105c83f42a015c83fd71240014", true } 
			});
	}

	@Ignore
	@Test
	public void testEntitledSearch() {
		try {

			logger.info("Create Subnet name as [{}] ", subnet.getName());
			ResponseEntity<Subnet> response = subnetService.create(subnet);
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			if (success) {

				assertNotNull(response);
				assertEquals(false, response.isErrors());

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
		}

	}

	@After
	public void cleanUp() {
		if (this.subnetCreated != null) {
			logger.info("cleaning up Subnet...");
			ResponseEntity<?> response = subnetService.delete(this.subnetCreated.getId());
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
