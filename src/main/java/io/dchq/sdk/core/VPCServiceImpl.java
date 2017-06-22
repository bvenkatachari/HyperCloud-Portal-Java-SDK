package io.dchq.sdk.core;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.VirtualPrivateCloud;

public class VPCServiceImpl extends
		GenericServiceImpl<VirtualPrivateCloud, ResponseEntity<List<VirtualPrivateCloud>>, ResponseEntity<VirtualPrivateCloud>>
		implements VPCService {
	
	public static final String ENDPOINT = "virtualprivatecloud/";

	    /**
	     * @param baseURI  - e.g. https://dchq.io/api/1.0/
	     * @param username - registered username with DCHQ.io
	     * @param password - password used with the username
	     */

	protected VPCServiceImpl(String baseURI, String username, String password) {

		super(baseURI, ENDPOINT, username, password,
				new ParameterizedTypeReference<ResponseEntity<List<VirtualPrivateCloud>>>() {
				}, new ParameterizedTypeReference<ResponseEntity<VirtualPrivateCloud>>() {
				});
	}

}
