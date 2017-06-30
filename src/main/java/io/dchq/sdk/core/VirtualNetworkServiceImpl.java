package io.dchq.sdk.core;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vlan.VirtualNetwork;

public class VirtualNetworkServiceImpl
		extends GenericServiceImpl<VirtualNetwork, ResponseEntity<List<VirtualNetwork>>, ResponseEntity<VirtualNetwork>>
		implements VirtualNetworkService {
	
	public static final String ENDPOINT = "vlan/";

	protected VirtualNetworkServiceImpl(String baseURI, String username, String password) {

		super(baseURI, ENDPOINT, username, password,
				new ParameterizedTypeReference<ResponseEntity<List<VirtualNetwork>>>() {
				}, new ParameterizedTypeReference<ResponseEntity<VirtualNetwork>>() {
				});
	}

}
