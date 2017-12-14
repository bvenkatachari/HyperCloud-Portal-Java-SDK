package io.dchq.sdk.core.networkacl;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
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
public class NetworkACLUpdateServiceTest extends NetworkACLUtil {

	@org.junit.Before
	public void setUp() throws Exception {
		networkACLService = ServiceFactory.buildNetworkACLService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	
	public NetworkACLUpdateServiceTest(String networkACLName, String subnet_Id, EntitlementType entitlementType, boolean isprifix, boolean success) {

		String postfix = RandomStringUtils.randomAlphabetic(3);
		if(isprifix){
		    networkACLName = networkACLName + postfix;
		}

		networkACL = new NetworkACL();
		networkACL.setName(networkACLName);
		networkACL.setEntitlementType(entitlementType);
		
		NameEntityBase subnet = new NameEntityBase();
		subnet.setId(subnet_Id);
		
		networkACL.setSubnet(subnet);

		this.success = success;

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			{ "networkACL", subnetId, EntitlementType.OWNER, true, true },
			{ "", "", EntitlementType.OWNER, false, false },
			{ "networkACL", "ssssssssssssssssssssssssss", EntitlementType.OWNER, true, false }
			});
	}


	@Ignore
	@Test
	public void testUpdate() {
		try {

			logger.info("Create Network ACL name as [{}] ", networkACL.getName());
			ResponseEntity<NetworkACL> response = networkACLService.create(networkACL);
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			if (success) {
				
				assertNotNull(response);
				assertEquals(false, response.isErrors());
				
				if (response.getResults() != null && !response.isErrors()) {
					this.networkACLCreated = response.getResults();
					logger.info("Create Network ACL sccessful..");
				}

				
				String updatedName = this.networkACLCreated.getName() + "_updated";
				this.networkACLCreated.setName(updatedName);
				this.networkACLCreated.setEntitlementType(EntitlementType.PUBLIC);

				// Updating Network ACL Name
				logger.info("Updating Network ACL name with [{}]", updatedName);
				response = networkACLService.update(this.networkACLCreated);

				for (Message message : response.getMessages()) {
					logger.warn("Error while Update request  [{}] ", message.getMessageText());

				}

				assertNotNull(response);

				if (!response.isErrors()) {
					assertNotNull(response.getResults());
					assertEquals(response.getResults().getName(), this.networkACLCreated.getName());
					assertEquals(response.getResults().getEntitlementType(), this.networkACLCreated.getEntitlementType());
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

		if (this.networkACLCreated != null) {
			logger.info("cleaning up Network ACL...");
			ResponseEntity<?> response = networkACLService.delete(this.networkACLCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error Network ACL deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
