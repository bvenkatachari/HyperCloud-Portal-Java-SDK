package io.dchq.sdk.core.securitygroup;

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
import com.dchq.schema.beans.one.vpc.SecurityGroup;

import io.dchq.sdk.core.SecurityGroupService;
import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SecurityGroupEntitledServiceTest extends SecurityGroupUtil {

	SecurityGroupService securityGroupService2;

	@org.junit.Before
	public void setUp() throws Exception {
		securityGroupService = ServiceFactory.buildSecurityGroupService(rootUrl1, cloudadminusername, cloudadminpassword);
		securityGroupService2 = ServiceFactory.buildSecurityGroupService(rootUrl1, username2, password2);
	}

	public SecurityGroupEntitledServiceTest(String securityGroupName, String subnet_Id, EntitlementType entitlementType,
			boolean isEntitlementTypeUser, String entitledUserId, boolean isprifix, boolean success) {

		String postfix = RandomStringUtils.randomAlphabetic(3);
		if(isprifix){
		   securityGroupName = securityGroupName + postfix;
		}

		securityGroup = new SecurityGroup();
		securityGroup.setName(securityGroupName);
		securityGroup.setEntitlementType(entitlementType);
		
		NameEntityBase subnet = new NameEntityBase();
		subnet.setId(subnet_Id);
		
		securityGroup.setSubnet(subnet);
		
		if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
			UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
			List<UsernameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			securityGroup.setEntitledUsers(entiledUsers);
		} else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
			NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
			List<NameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			securityGroup.setEntitledUserGroups(entiledUsers);
		}

		this.success = success;

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
				{ "securityGroup", subnetId, EntitlementType.OWNER, false, null, true,true },
				//{ "securityGroup", subnetId, EntitlementType.PUBLIC, false, null, true, true },
				{ "securityGroup", subnetId, EntitlementType.CUSTOM, true, userId2, true, true },
				{ "securityGroup", subnetId, EntitlementType.CUSTOM, false, USER_GROUP, true, true },
				{ "securityGroup", null, EntitlementType.OWNER, false, null, true,false },
				/*
				 * Security Group gets created for the blank value & special character, but didn't list/search on UI/API.
				 * */
				//{ "@@@^%%*&*^securityGroup", subnetId, EntitlementType.OWNER, true, false },
				//{ "", subnetId, EntitlementType.PUBLIC, false, null, true, false },
				//{ null, subnetId, EntitlementType.CUSTOM, true, userId2, true, false },
				{ "", "", EntitlementType.CUSTOM, false, USER_GROUP, false, false },
				{ "securityGroup", "ssssssssssssssssssssssssss", EntitlementType.OWNER, false, null, true,false }
			});
	}


	
	
	@Test
	public void testEntitledSearch() {
		try {

			logger.info("Create Security Group name as [{}] ", securityGroup.getName());
			ResponseEntity<SecurityGroup> response = securityGroupService.create(securityGroup);
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			if (success) {


				if (response.getResults() != null && !response.isErrors()) {
					this.securityGroupCreated = response.getResults();
					logger.info("Create Security Group service Successful..");
				}

				if (this.securityGroupCreated.getEntitlementType().equals(EntitlementType.OWNER)) {
					ResponseEntity<List<SecurityGroup>> subnetSearchResponseEntity = securityGroupService2.search(securityGroup.getName(), 0,1);
					for (Message message : subnetSearchResponseEntity.getMessages()) {
						logger.warn("Error while Search request  [{}] ", message.getMessageText());
					}
					assertNotNull(subnetSearchResponseEntity);
					assertNotNull(subnetSearchResponseEntity.isErrors());
					assertNotNull(subnetSearchResponseEntity.getResults());
					assertEquals(0, subnetSearchResponseEntity.getResults().size());
				}
				if (this.securityGroupCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
					ResponseEntity<List<SecurityGroup>> subnetSearchResponseEntity = securityGroupService2.search(securityGroup.getName(), 0,1);
					for (Message message : subnetSearchResponseEntity.getMessages()) {
						logger.warn("Error while Search request  [{}] ", message.getMessageText());
					}
					assertNotNull(subnetSearchResponseEntity);
					assertNotNull(subnetSearchResponseEntity.isErrors());
					assertNotNull(subnetSearchResponseEntity.getResults());
					assertEquals(1, subnetSearchResponseEntity.getResults().size());
				}
				if (this.securityGroupCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
					ResponseEntity<List<SecurityGroup>> subnetSearchResponseEntity = securityGroupService2.search(securityGroup.getName(), 0,1);
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

		if (this.securityGroupCreated != null) {
			logger.info("cleaning up Security Group...");
			ResponseEntity<?> response = securityGroupService.delete(this.securityGroupCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error Security Group deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
