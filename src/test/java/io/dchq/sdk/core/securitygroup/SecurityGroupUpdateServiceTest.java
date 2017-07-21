package io.dchq.sdk.core.securitygroup;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vpc.SecurityGroup;

import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SecurityGroupUpdateServiceTest extends SecurityGroupUtil {

	@org.junit.Before
	public void setUp() throws Exception {
		securityGroupService = ServiceFactory.buildSecurityGroupService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	
	public SecurityGroupUpdateServiceTest(String securityGroupName, String subnet_Id, EntitlementType entitlementType, boolean isprifix, boolean success) {

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

		this.success = success;

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			{ "securityGroup", subnetId, EntitlementType.OWNER, true, true },
			{ "securityGroup", subnetId, EntitlementType.PUBLIC, true, true },
			{ "securityGroup", subnetId, EntitlementType.CUSTOM, true, true },
			{ "securityGroup", "", EntitlementType.OWNER, true, false },
			{ "", "", EntitlementType.OWNER, false, false },
			{ "securityGroup", null, EntitlementType.OWNER, true, false },
			/*
			 * Security Group gets created for the blank value & special character, but didn't list/search on UI/API.
			 * */
			//{ "@@@^%%*&*^securityGroup", subnetId, EntitlementType.OWNER, true, false },
			//{ null, subnetId, EntitlementType.OWNER, false, false },
			//{ "", subnetId, EntitlementType.OWNER, false, false },
			//{ "@@@@@@@@@@@@@@@@@@@@@@@@", subnetId, EntitlementType.OWNER, false, false },
			{ "securityGroup", "ssssssssssssssssssssssssss", EntitlementType.OWNER, true, false },
			});
	}

	
	@Test
	public void testUpdate() {
		try {

			logger.info("Create Security Group name as [{}] ", securityGroup.getName());
			ResponseEntity<SecurityGroup> response = securityGroupService.create(securityGroup);
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			if (success) {
				
				assertNotNull(response);
				assertEquals(false, response.isErrors());
				
				if (response.getResults() != null && !response.isErrors()) {
					this.securityGroupCreated = response.getResults();
					logger.info("Create Security Group sccessful..");
				}

				
				String updatedName = this.securityGroupCreated.getName() + "_updated";
				this.securityGroupCreated.setName(updatedName);

				// Updating Security Group Name
				logger.info("Updating Security Group name with [{}]", updatedName);
				response = securityGroupService.update(this.securityGroupCreated);

				for (Message message : response.getMessages()) {
					logger.warn("Error while Update request  [{}] ", message.getMessageText());

				}

				assertNotNull(response);

				if (!response.isErrors()) {
					assertNotNull(response.getResults());
					assertEquals(response.getResults().getName(), updatedName);
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
