package io.dchq.sdk.core.securitygrouprule;

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
import com.dchq.schema.beans.one.vpc.Rule;
import com.dchq.schema.beans.one.vpc.RuleAction;
import com.dchq.schema.beans.one.vpc.RuleBoundType;
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
public class SecurityGroupRuleUpdateServiceTest extends SecurityGroupRuleUtil {

	@org.junit.Before
	public void setUp() throws Exception {
		ruleService = ServiceFactory.buildSecurityGroupRuleService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	
	public SecurityGroupRuleUpdateServiceTest(String ruleName, RuleBoundType bound, String protocol, String ip, String portRange,
			RuleAction action, boolean success) {

		//Create Security Group
		securityGroup = getSecurityGroup();
		
		String postfix = RandomStringUtils.randomAlphabetic(3);
		ruleName = ruleName + postfix;

		rule = new Rule();
		rule.setName(ruleName);
		rule.setBound(bound);
		rule.setProtocol(protocol);
		rule.setIp(ip);
		rule.setPort(portRange);
		rule.setAction(action);
		
		this.success = success;

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
				{ "rule", RuleBoundType.in, "tcp", "10.0.0.0/24", "0-4500", RuleAction.pass, true }
				/*
				 * Security Group Rule gets created for the blank values, special character & invalid values.
				 * */
				//{ "", RuleBoundType.in, "tcp", "abcd", "1234", RuleAction.pass, false, false }
			});
	}

	
	@Test
	public void testUpdate() {
		try {
			logger.info("Create Security Group Rule name as [{}] ", rule.getName());
			ResponseEntity<SecurityGroup> response = ruleService.createRule(rule, securityGroup.getId());
			
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}
			
			if (success) {

				assertNotNull(response);
				assertEquals(false, response.isErrors());

				if (response.getResults() != null && !response.getResults().getRules().isEmpty() 
						    && !response.isErrors()) {
					this.ruleCreated = response.getResults().getRules().iterator().next();
					logger.info("Create Security Group Rule Successful..");
				}

				
				String updatedName = this.ruleCreated.getName() + "_updated";
				this.ruleCreated.setName(updatedName);
				this.ruleCreated.setProtocol("any");
				this.ruleCreated.setIp("any");
				this.ruleCreated.setPort("");
				this.ruleCreated.setAction(RuleAction.block);

				// Updating Rule Name
				logger.info("Updating Security Group name with [{}]", updatedName);
				response = ruleService.updateRule(this.ruleCreated, securityGroup.getId());

				for (Message message : response.getMessages()) {
					logger.warn("Error while Update request  [{}] ", message.getMessageText());

				}

				assertNotNull(response);

				if (!response.isErrors()) {
					assertNotNull(response.getResults());
					Rule updatedRule = response.getResults().getRules().iterator().next();
					assertEquals(updatedRule.getName(), this.ruleCreated.getName());
					assertEquals(updatedRule.getProtocol(), this.ruleCreated.getProtocol());
					assertEquals(updatedRule.getIp(), this.ruleCreated.getIp());
					assertEquals(updatedRule.getPort(), this.ruleCreated.getPort());
					assertEquals(updatedRule.getAction(), this.ruleCreated.getAction());
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
		
		if (this.ruleCreated != null) {
			logger.info("cleaning up Security Group Rule...");
			ResponseEntity<?> response = ruleService.deleteRule(this.ruleCreated.getId(), this.securityGroup.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error Security Group Rule deletion: [{}] ", message.getMessageText());
			}
		}

		if (this.securityGroup != null) {
			logger.info("cleaning up Security Group...");
			ResponseEntity<?> response = securityGroupService.delete(this.securityGroup.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error Security Group deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
