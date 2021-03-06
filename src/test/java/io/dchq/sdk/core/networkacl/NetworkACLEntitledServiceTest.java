package io.dchq.sdk.core.networkacl;

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
public class NetworkACLEntitledServiceTest extends NetworkACLUtil {

	NetworkACLService networkACLService2;

	@org.junit.Before
	public void setUp() throws Exception {
		networkACLService = ServiceFactory.buildNetworkACLService(rootUrl1, cloudadminusername, cloudadminpassword);
		networkACLService2 = ServiceFactory.buildNetworkACLService(rootUrl1, username2, password2);
	}

	public NetworkACLEntitledServiceTest(String networkACLName, String subnet_Id, EntitlementType entitlementType,
			boolean isEntitlementTypeUser, String entitledUserId, boolean isprifix, boolean success) {

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
				{ "networkACL", subnetId, EntitlementType.OWNER, false, null, true,true },
				{ "networkACL", subnetId, EntitlementType.PUBLIC, false, null, true, true },
				{ "networkACL", subnetId, EntitlementType.CUSTOM, true, userId2, true, true },
				{ "networkACL", subnetId, EntitlementType.CUSTOM, false, USER_GROUP, true, true },
				{ "networkACL", null, EntitlementType.OWNER, false, null, true,false },
				
				/*N/W ACL gets created for the blank value & special character, but didn't list/search on UI/API.*/	
				
				//{ "@@@^%%*&*^networkACL", subnetId, EntitlementType.OWNER, true, false },
				//{ "", subnetId, EntitlementType.PUBLIC, false, null, true, false },
				//{ null, subnetId, EntitlementType.CUSTOM, true, userId2, true, false },
				{ "", "", EntitlementType.CUSTOM, false, USER_GROUP, false, false },
				{ "networkACL", "ssssssssssssssssssssssssss", EntitlementType.OWNER, false, null, true,false }
			});
	}


	//Will work as per Vlan Entitlement
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
