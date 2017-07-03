package io.dchq.sdk.core.networkacl;

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
public class NetworkACLSearchServiceTest extends NetworkACLTest {

	@org.junit.Before
	public void setUp() throws Exception {
		networkACLService = ServiceFactory.buildNetworkACLService(rootUrl, username, password);
	}

	
	public NetworkACLSearchServiceTest(String networkACLName, EntitlementType entitlementType, String subnetId,boolean success) {

		String postfix = RandomStringUtils.randomAlphabetic(3);
		networkACLName = networkACLName + postfix;

		networkACL = new NetworkACL();
		networkACL.setName(networkACLName);
		networkACL.setEntitlementType(entitlementType);
		
		NameEntityBase subnet = new NameEntityBase();
		subnet.setId(subnetId);
		
		networkACL.setSubnet(subnet);

		this.success = success;

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			{ "networkACL", EntitlementType.OWNER, "402881875d06a531015d06c2d0c20033", true },
			{ "networkACL", EntitlementType.PUBLIC, "402881875d06a531015d06c2d0c20033", true }
			});
	}



	@Test
	public void testSearch() {
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

				
				ResponseEntity<List<NetworkACL>> searchResponse = networkACLService.search(this.networkACLCreated.getName(), 0, 1);

				for (Message message : searchResponse.getMessages()) {
					logger.warn("Error while Network ACL request  [{}] ", message.getMessageText());
				}
				
				assertNotNull(searchResponse);
				assertEquals(false,searchResponse.isErrors());
				assertNotNull(searchResponse.getResults().get(0).getId());
				assertEquals(this.networkACLCreated.getName(), searchResponse.getResults().get(0).getName());
				
				
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
	}
}
