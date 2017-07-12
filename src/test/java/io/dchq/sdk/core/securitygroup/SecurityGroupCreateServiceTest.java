package io.dchq.sdk.core.securitygroup;

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
public class SecurityGroupCreateServiceTest extends SecurityGroupUtil {

	@org.junit.Before
	public void setUp() throws Exception {
		securityGroupService = ServiceFactory.buildSecurityGroupService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	public SecurityGroupCreateServiceTest(String securityGroupName, EntitlementType entitlementType, boolean success) {

		String postfix = RandomStringUtils.randomAlphabetic(3);
		securityGroupName = securityGroupName + postfix;

		securityGroup = new SecurityGroup();
		securityGroup.setName(securityGroupName);
		securityGroup.setEntitlementType(entitlementType);
		
		NameEntityBase subnet = new NameEntityBase();
		subnet.setId(subnetId);
		
		securityGroup.setSubnet(subnet);

		this.success = success;

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			 { "securityGroup", EntitlementType.OWNER, true },
			 { "securityGroup", EntitlementType.PUBLIC, true } 
			});
	}

	
	@Test
	public void createTest() {
		try {
			logger.info("Create Security Group name as [{}] ", securityGroup.getName());
			ResponseEntity<SecurityGroup> response = securityGroupService.create(securityGroup);
			if (success) {
				for (Message message : response.getMessages()) {
					logger.warn("Error while Create request  [{}] ", message.getMessageText());
				}

				assertNotNull(response);
				assertEquals(false, response.isErrors());

				if (response.getResults() != null && !response.isErrors()) {
					this.securityGroupCreated = response.getResults();
					logger.info("Create Security Group service Successful..");
				}

				
				if (this.securityGroupCreated != null) {
					assertNotNull(securityGroupCreated.getId());
					assertNotNull("It should not be null or empty", securityGroupCreated.getName());
					assertEquals(securityGroup.getName(), securityGroupCreated.getName());
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

		if (this.securityGroupCreated != null) {
			logger.info("cleaning up Security Group...");
			ResponseEntity<?> response = securityGroupService.delete(this.securityGroupCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error Security Group deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
