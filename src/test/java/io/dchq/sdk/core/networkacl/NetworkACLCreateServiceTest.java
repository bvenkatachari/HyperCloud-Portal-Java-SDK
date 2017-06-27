package io.dchq.sdk.core.networkacl;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.NetworkACL;

import io.dchq.sdk.core.ServiceFactory;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class NetworkACLCreateServiceTest extends NetworkACLTest {


	@org.junit.Before
	public void setUp() throws Exception {
		networkACLService = ServiceFactory.buildNetworkACLService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	

	public NetworkACLCreateServiceTest(String networkACLName, String subnetName, String vpcName, boolean success) 
	{

		String postfix = RandomStringUtils.randomAlphabetic(3);
		
		vpcName = vpcName + "-" + postfix;
		createdVPC = getVPC(vpcName, true);
		
		Assert.assertNotNull(createdVPC);
		
		subnetName = subnetName + "-" + postfix;
		createdSubnet = getSubnet(subnetName, true);
		
		networkACL = new NetworkACL();
		networkACL.setName(networkACLName);
		
		this.success = success;
		
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] {
			{"networkACL1","vpc1","subnet1", true}
		});
	}

	@Ignore
	@Test
	public void createTest() {
		try {
			logger.info("Create Network ACL name as [{}] ", networkACL.getName());
			ResponseEntity<NetworkACL> response = networkACLService.create(networkACL);
			if(success)
			{
				for (Message message : response.getMessages()) {
					logger.warn("Error while Create request  [{}] ", message.getMessageText());
				}
				
				assertNotNull(response);
				assertEquals(false, response.isErrors());

				if (response.getResults() != null && !response.isErrors()) {
					this.networkACLCreated = response.getResults();
					logger.info("Create Network ACL service Successful..");
				}

				
				assertNotNull(response);
				assertNotNull(response.isErrors());
				if (this.networkACLCreated != null) {
					assertNotNull(response.getResults().getId());
					assertNotNull(networkACLCreated.getId());
					assertNotNull("It shloud not be null or empty", networkACLCreated.getName());
					assertEquals(networkACL.getName(), networkACLCreated.getName());
				}
				
			}
			else
			{
				assertEquals(null, response.getResults());
				assertEquals(true, response.isErrors());
			}
		} catch (Exception e) {
			// ignore
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
