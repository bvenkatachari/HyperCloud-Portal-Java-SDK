package io.dchq.sdk.core;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.NetworkACL;
import com.dchq.schema.beans.one.vpc.Rule;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

public class NetworkACLRuleServiceImpl extends
		GenericServiceImpl<Rule, ResponseEntity<List<NetworkACL>>, ResponseEntity<NetworkACL>> implements NetworkACLRuleService {

	public static final String ENDPOINT = "networkacl/rule/";

	/**
	 * @param baseURI
	 *            - e.g. https://dchq.io/api/1.0/
	 * @param username
	 *            - registered username with DCHQ.io
	 * @param password
	 *            - password used with the username
	 */

	public NetworkACLRuleServiceImpl(String baseURI, String username, String password) {

		super(baseURI, ENDPOINT, username, password, new ParameterizedTypeReference<ResponseEntity<List<NetworkACL>>>() {
		   }, new ParameterizedTypeReference<ResponseEntity<NetworkACL>>() {
		});
	}

	@Override
	public ResponseEntity<NetworkACL> createRule(Rule rule, String networkACLId) {
		 return super.doPost(rule, networkACLId);
	}
	
	public ResponseEntity<NetworkACL> updateRule(Rule rule, String networkACLId) {
		 
		 String urlPostfix = networkACLId+"?ruleid=";
		 return super.update(rule, rule.getId(), urlPostfix);
	}
	
	public ResponseEntity<NetworkACL> deleteRule(String ruleId, String networkACLId) {
		
		 String urlPostfix = networkACLId+"?ruleid=";
		 return super.delete(ruleId, urlPostfix);
	}

}
