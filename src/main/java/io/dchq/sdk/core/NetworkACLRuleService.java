package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.NetworkACL;
import com.dchq.schema.beans.one.vpc.Rule;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

public interface NetworkACLRuleService extends GenericService<Rule, ResponseEntity<List<NetworkACL>>, ResponseEntity<NetworkACL>> {
	
	ResponseEntity<NetworkACL> createRule(Rule rule, String networkACLId);
	ResponseEntity<NetworkACL> updateRule(Rule rule, String networkACLId);
	ResponseEntity<NetworkACL> deleteRule(String ruleId, String networkACLId);
}
