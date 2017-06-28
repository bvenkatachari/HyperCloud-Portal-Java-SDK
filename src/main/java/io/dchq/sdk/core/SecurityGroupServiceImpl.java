package io.dchq.sdk.core;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.SecurityGroup;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

public class SecurityGroupServiceImpl
		extends GenericServiceImpl<SecurityGroup, ResponseEntity<List<SecurityGroup>>, ResponseEntity<SecurityGroup>>
		implements SecurityGroupService {

	public static final String ENDPOINT = "securitygroup/";

	/**
     * @param baseURI  - e.g. https://dchq.io/api/1.0/
     * @param username - registered username with DCHQ.io
     * @param password - password used with the username
     */

	public SecurityGroupServiceImpl(String baseURI, String username, String password) {

		super(baseURI, ENDPOINT, username, password,
				new ParameterizedTypeReference<ResponseEntity<List<SecurityGroup>>>() {
				}, new ParameterizedTypeReference<ResponseEntity<SecurityGroup>>() {
				});
	}

}
