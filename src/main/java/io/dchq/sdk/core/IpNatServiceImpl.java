package io.dchq.sdk.core;

import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.network.IpPool;

/**
 * 
 * @author msys
 *
 */

public class IpNatServiceImpl extends GenericServiceImpl<IpPool, ResponseEntity<List<IpPool>>, ResponseEntity<IpPool>>
		implements IpNatService {

	public static final ParameterizedTypeReference<ResponseEntity<List<IpPool>>> listTypeReference = new ParameterizedTypeReference<ResponseEntity<List<IpPool>>>() {
	};
	public static final ParameterizedTypeReference<ResponseEntity<IpPool>> singleTypeReference = new ParameterizedTypeReference<ResponseEntity<IpPool>>() {
	};

	public static final String ENDPOINT = "vpcippool/";

	public IpNatServiceImpl(String baseURI, String username, String password) {
		super(baseURI, ENDPOINT, username, password, new ParameterizedTypeReference<ResponseEntity<List<IpPool>>>() {
		}, new ParameterizedTypeReference<ResponseEntity<IpPool>>() {
		});
	}

}
