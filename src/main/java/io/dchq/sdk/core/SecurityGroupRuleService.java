package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.Rule;
import com.dchq.schema.beans.one.vpc.SecurityGroup;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

public interface SecurityGroupRuleService extends GenericService<Rule, ResponseEntity<List<SecurityGroup>>, ResponseEntity<SecurityGroup>> {
	
	ResponseEntity<SecurityGroup> createRule(Rule rule, String securityGroupId);
	ResponseEntity<SecurityGroup> updateRule(Rule rule, String securityGroupId);
	ResponseEntity<SecurityGroup> deleteRule(String ruleId, String securityGroupId);
}
