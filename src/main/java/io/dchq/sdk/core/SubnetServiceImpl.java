package io.dchq.sdk.core;

import java.util.List;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/

import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.Subnet;

public class SubnetServiceImpl extends
		GenericServiceImpl<Subnet, ResponseEntity<List<Subnet>>, ResponseEntity<Subnet>>
		implements SubnetService {
	
	public static final String ENDPOINT = "subnets/";

	    /**
	     * @param baseURI  - e.g. https://dchq.io/api/1.0/
	     * @param username - registered username with DCHQ.io
	     * @param password - password used with the username
	     */

	public SubnetServiceImpl(String baseURI, String username, String password) {

		super(baseURI, ENDPOINT, username, password,
				new ParameterizedTypeReference<ResponseEntity<List<Subnet>>>() {
				}, new ParameterizedTypeReference<ResponseEntity<Subnet>>() {
				});
	}

}
