package io.dchq.sdk.core.securitygrouprule;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

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
public class SecurityGroupRuleCreateServiceTest extends SecurityGroupRuleTest {

	@org.junit.Before
	public void setUp() throws Exception {
		ruleService = ServiceFactory.buildSecurityGroupRuleService(rootUrl, username, password);
	}

	
	public SecurityGroupRuleCreateServiceTest(String ruleName, RuleBoundType bound, String protocol, String ip, String portRange,
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
				{ "rule", RuleBoundType.in, "any", "any", "", RuleAction.pass, true },
				{ "rule", RuleBoundType.in, "tcp", "10.0.0.0/24", "0-4500", RuleAction.pass, true },
				{ "rule", RuleBoundType.out, "any", "any", "", RuleAction.pass, true },
				{ "rule", RuleBoundType.out, "tcp", "10.0.0.0/24", "0-4500", RuleAction.block, true }
			});
	}

	@Ignore
	@Test
	public void createTest() {
		try {
			logger.info("Create Security Group Rule name as [{}] ", rule.getName());
			ResponseEntity<SecurityGroup> response = ruleService.createRule(rule, securityGroup.getId());
			if (success) {
				for (Message message : response.getMessages()) {
					logger.warn("Error while Create request  [{}] ", message.getMessageText());
				}

				assertNotNull(response);
				assertEquals(false, response.isErrors());

				if (response.getResults() != null && !response.getResults().getRules().isEmpty() 
						    && !response.isErrors()) {
					this.ruleCreated = response.getResults().getRules().iterator().next();
					logger.info("Create Security Group Rule Successful..");
				}

				
				if (this.ruleCreated != null) {
					assertNotNull(ruleCreated.getId());
					assertNotNull("It should not be null or empty", ruleCreated.getName());
					assertEquals(rule.getName(), ruleCreated.getName());
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