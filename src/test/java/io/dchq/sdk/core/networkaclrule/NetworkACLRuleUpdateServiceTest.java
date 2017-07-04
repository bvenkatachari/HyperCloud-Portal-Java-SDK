package io.dchq.sdk.core.networkaclrule;

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
import com.dchq.schema.beans.one.vpc.Rule;
import com.dchq.schema.beans.one.vpc.RuleAction;
import com.dchq.schema.beans.one.vpc.RuleBoundType;

import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class NetworkACLRuleUpdateServiceTest extends NetworkACLRuleTest {

	@org.junit.Before
	public void setUp() throws Exception {
		ruleService = ServiceFactory.buildNetworkACLRuleService(rootUrl, username, password);
	}

	
	public NetworkACLRuleUpdateServiceTest(String ruleName, RuleBoundType bound, String protocol, String ip, String portRange,
			RuleAction action, boolean success) {

		//Create Network ACL
		networkACL = getNetworkACL();
		
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
			});
	}

	@Ignore
	@Test
	public void testUpdate() {
		try {
			logger.info("Create Network ACL Rule name as [{}] ", rule.getName());
			ResponseEntity<NetworkACL> response = ruleService.createRule(rule, networkACL.getId());
			
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}
			
			if (success) {

				assertNotNull(response);
				assertEquals(false, response.isErrors());

				if (response.getResults() != null && !response.getResults().getRules().isEmpty() 
						    && !response.isErrors()) {
					this.ruleCreated = response.getResults().getRules().iterator().next();
					logger.info("Create Network ACL Rule Successful..");
				}

				
				String updatedName = this.ruleCreated.getName() + "_updated";
				this.ruleCreated.setName(updatedName);

				// Updating Rule Name
				logger.info("Updating Network ACL Rule name with [{}]", updatedName);
				response = ruleService.updateRule(this.ruleCreated, networkACL.getId());

				for (Message message : response.getMessages()) {
					logger.warn("Error while Update request  [{}] ", message.getMessageText());

				}

				assertNotNull(response);

				if (!response.isErrors()) {
					Assert.assertNotNull(response.getResults());
					Assert.assertNotNull(response.getResults().getRules().iterator().next().getName(), updatedName);
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
			logger.info("cleaning up Network ACL Rule...");
			ResponseEntity<?> response = ruleService.deleteRule(this.ruleCreated.getId(), this.networkACL.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error Network ACL Rule deletion: [{}] ", message.getMessageText());
			}
		}

		if (this.networkACL != null) {
			logger.info("cleaning up Network ACL...");
			ResponseEntity<?> response = networkACLService.delete(this.networkACL.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error Network ACL deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
