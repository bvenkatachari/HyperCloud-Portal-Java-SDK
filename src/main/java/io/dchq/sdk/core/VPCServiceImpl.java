package io.dchq.sdk.core;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.VirtualPrivateCloud;

public class VPCServiceImpl extends
		GenericServiceImpl<VirtualPrivateCloud, ResponseEntity<List<VirtualPrivateCloud>>, ResponseEntity<VirtualPrivateCloud>>
		implements VPCService {

	protected VPCServiceImpl(String baseURI, String endpoint, String username, String password) {

		super(baseURI, endpoint, username, password,
				new ParameterizedTypeReference<ResponseEntity<List<VirtualPrivateCloud>>>() {
				}, new ParameterizedTypeReference<ResponseEntity<VirtualPrivateCloud>>() {
				});
	}

}
