package io.dchq.sdk.core.subnet;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vlan.VirtualNetwork;
import com.dchq.schema.beans.one.vpc.Subnet;

import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SubnetSearchServiceTest extends SubnetUtil {

	@org.junit.Before
	public void setUp() throws Exception {
		subnetService = ServiceFactory.buildSubnetService(rootUrl1, cloudadminusername, cloudadminpassword);
		securityGroupService = ServiceFactory.buildSecurityGroupService(rootUrl1, cloudadminusername, cloudadminpassword);
		networkACLService = ServiceFactory.buildNetworkACLService(rootUrl1, cloudadminusername, cloudadminpassword);
	}


	public SubnetSearchServiceTest(String subnetName, String ipv4Cidr, String dhcp, String fromIpRange,
			String toIpRange, String dnsServers, EntitlementType entitlementType, boolean success) {
		
		
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
		
		this.success = success;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			
			 { "subnet", "10.50.5.1/24", "true", "10.50.5.2", "10.50.5.254", "8.8.8.8", EntitlementType.OWNER, true }
			});
	}

	
	@Test
	public void testSearch() {
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

				
				ResponseEntity<List<Subnet>> searchResponse = subnetService.search(this.subnetCreated.getName(), 0, 1);

				for (Message message : searchResponse.getMessages()) {
					logger.warn("Error while search Subnet request  [{}] ", message.getMessageText());
				}
				
				assertNotNull(searchResponse);
				assertEquals(false,searchResponse.isErrors());
				assertNotNull(searchResponse.getResults().get(0).getId());
				assertEquals(this.subnetCreated.getName(), searchResponse.getResults().get(0).getName());
				
				
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
