package io.dchq.sdk.core;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.Rule;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

public class NetworkACLRuleServiceImpl extends
		GenericServiceImpl<Rule, ResponseEntity<List<Rule>>, ResponseEntity<Rule>> implements NetworkACLRuleService {

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

		super(baseURI, ENDPOINT, username, password, new ParameterizedTypeReference<ResponseEntity<List<Rule>>>() {
		   }, new ParameterizedTypeReference<ResponseEntity<Rule>>() {
		});
	}

}
