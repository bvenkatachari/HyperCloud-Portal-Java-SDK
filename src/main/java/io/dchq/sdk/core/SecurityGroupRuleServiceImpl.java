package io.dchq.sdk.core;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.Rule;
import com.dchq.schema.beans.one.vpc.SecurityGroup;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

public class SecurityGroupRuleServiceImpl extends
		GenericServiceImpl<Rule, ResponseEntity<List<SecurityGroup>>, ResponseEntity<SecurityGroup>> implements SecurityGroupRuleService {

	public static final String ENDPOINT = "securitygroup/rule/";

	/**
	 * @param baseURI
	 *            - e.g. https://dchq.io/api/1.0/
	 * @param username
	 *            - registered username with DCHQ.io
	 * @param password
	 *            - password used with the username
	 */

	public SecurityGroupRuleServiceImpl(String baseURI, String username, String password) {

		super(baseURI, ENDPOINT, username, password, new ParameterizedTypeReference<ResponseEntity<List<SecurityGroup>>>() {
		   }, new ParameterizedTypeReference<ResponseEntity<SecurityGroup>>() {
		});
	}

	@Override
	public ResponseEntity<SecurityGroup> createRule(Rule rule, String securityGroupId) {
		 return super.doPost(rule, securityGroupId);
	}
	
	public ResponseEntity<SecurityGroup> updateRule(Rule rule, String securityGroupId) {
		 
		 String urlPostfix = securityGroupId+"?ruleid=";
		 return super.update(rule, rule.getId(), urlPostfix);
	}
	
	public ResponseEntity<SecurityGroup> deleteRule(String ruleId, String securityGroupId) {
		
		 String urlPostfix = securityGroupId+"?ruleid=";
		 return super.delete(ruleId, urlPostfix);
	}

}
