package io.dchq.sdk.core.subnet;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

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
public class SubnetFindAllServiceTest extends SubnetUtil {

	@org.junit.Before
	public void setUp() throws Exception {
		subnetService = ServiceFactory.buildSubnetService(rootUrl1, cloudadminusername, cloudadminpassword);
		securityGroupService = ServiceFactory.buildSecurityGroupService(rootUrl1, cloudadminusername, cloudadminpassword);
		networkACLService = ServiceFactory.buildNetworkACLService(rootUrl1, cloudadminusername, cloudadminpassword);
	}


	private int countBeforeCreate = 0, countAfterCreate = 0;
	
	public SubnetFindAllServiceTest(String subnetName, String ipv4Cidr, String dhcp, String fromIpRange,
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
			
			 { "subnet", "10.50.5.1/24", "true", "10.50.5.2", "10.50.5.254", "8.8.8.8", EntitlementType.OWNER, true },
			 { "subnet", "10.50.5.1/24", "true", "10.50.5.2", "10.50.5.254", "8.8.8.8", EntitlementType.PUBLIC, true }
			});
	}
	
	public int testSubnetPosition(String id) {
		ResponseEntity<List<Subnet>> response = subnetService.findAll(0, 500);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}]  " + message.getMessageText());
		}
		assertNotNull(response);
		assertNotNull(response.isErrors());
		assertEquals(false, response.isErrors());
		int position = 0;
		if (id != null) {
			for (Subnet obj : response.getResults()) {
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
	public void testFindAll() {
		try {
			
			countBeforeCreate = testSubnetPosition(null);
			
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

				
				logger.info("FindAll Subnet by Id [{}]", this.subnetCreated.getId());
				this.countAfterCreate = testSubnetPosition(this.subnetCreated.getId());
				assertEquals(countBeforeCreate + 1, countAfterCreate);
				
				
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
