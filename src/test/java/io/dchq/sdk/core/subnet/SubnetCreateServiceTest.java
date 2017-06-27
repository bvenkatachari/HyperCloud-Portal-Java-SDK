package io.dchq.sdk.core.subnet;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

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
public class SubnetCreateServiceTest extends SubnetTest {


	@org.junit.Before
	public void setUp() throws Exception {
		subnetService = ServiceFactory.buildSubnetService(rootUrl, cloudadminusername, cloudadminpassword);
	}
	

	public SubnetCreateServiceTest(String subnetName, String vlanId, String ipv4Cidr, 
			String dhcp, String fromIpRange, String toIpRange, String dnsServers, boolean success) 
	{
		String postfix = RandomStringUtils.randomAlphabetic(3);
		
		subnetName = subnetName + postfix;
		subnet = new Subnet();
		subnet.setName(subnetName);
		
		//Will get VPC object from getVPC();
		NameEntityBase vpc = new NameEntityBase();
		vpc.setId(vpcId);
		subnet.setVpc(vpc);
		
		VirtualNetwork virtualNetwork = new VirtualNetwork();
		virtualNetwork.setId(vlanId);
		subnet.setVirtualNetwork(virtualNetwork);
		
		subnet.setIpv4Cidr(ipv4Cidr);
		
		if("true" == dhcp){
			subnet.setDhcp(dhcp);
			subnet.setFromIpRange(fromIpRange);
			subnet.setToIpRange(toIpRange);
			subnet.setDnsServers(dnsServers);
		}
		
		this.success = success;
	}

	
	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] {
			{"subnet", "402881875cd3e674015cd4ca484501b4", "10.0.0.0/24", "true", "10.0.0.2", "10.0.0.254", "8.8.8.8", true }
		});
	}

	
	@Test
	public void createTest() {

		try {
			logger.info("Create Subnet name as [{}] ", subnet.getName());
			ResponseEntity<Subnet> response = subnetService.create(subnet);
			if(success)
			{
				for (Message message : response.getMessages()) {
					logger.warn("Error while Create request  [{}] ", message.getMessageText());
				}
				
				assertNotNull(response);
				assertEquals(false, response.isErrors());

				if (response.getResults() != null && !response.isErrors()) {
					this.subnetCreated = response.getResults();
					logger.info("Create Subnet service Successful..");
				}

				
				assertNotNull(response);
				assertNotNull(response.isErrors());
				if (this.subnetCreated != null) {
					assertNotNull(response.getResults().getId());
					assertNotNull(subnetCreated.getId());
					assertNotNull("It shloud not be null or empty", subnetCreated.getName());
					assertEquals(subnet.getName(), subnetCreated.getName());
				}
				
			}
			else
			{
				assertEquals(null, response.getResults());
				assertEquals(true, response.isErrors());
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		
		/*if (this.createdVPC != null) {
			logger.info("cleaning up VPC ...");
			ResponseEntity<?> response = vpcService.delete(this.createdVPC.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error VPC deletion: [{}] ", message.getMessageText());
			}
		}*/
	}
}
