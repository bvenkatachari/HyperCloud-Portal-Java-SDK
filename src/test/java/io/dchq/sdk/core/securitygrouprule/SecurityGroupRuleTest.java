package io.dchq.sdk.core.securitygrouprule;

import org.apache.commons.lang3.RandomStringUtils;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vpc.Rule;
import com.dchq.schema.beans.one.vpc.SecurityGroup;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.SecurityGroupRuleService;
import io.dchq.sdk.core.SecurityGroupService;
import io.dchq.sdk.core.ServiceFactory;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/

public class SecurityGroupRuleTest extends AbstractServiceTest {

	// Create Security Group
	SecurityGroupService securityGroupService;
	SecurityGroup securityGroup;

	// Create Security Group Rule
	SecurityGroupRuleService ruleService;
	Rule rule;
	Rule ruleCreated;
	boolean success;

	public SecurityGroup getSecurityGroup() {

		securityGroupService = ServiceFactory.buildSecurityGroupService(rootUrl, username, password);

		String postfix = RandomStringUtils.randomAlphabetic(3);
		String name = "securityGroup" + postfix;

		SecurityGroup securityGroup = new SecurityGroup();
		securityGroup.setName(name);
		securityGroup.setEntitlementType(EntitlementType.PUBLIC);
		
		NameEntityBase subnet = new NameEntityBase();
		subnet.setId(subnetId);
		
		securityGroup.setSubnet(subnet);

		ResponseEntity<SecurityGroup> responseEntity = securityGroupService.create(securityGroup);
		if (responseEntity.isErrors())
			logger.warn("Message from Server... {}", responseEntity.getMessages().get(0).getMessageText());

		return responseEntity.getResults();

	}
}