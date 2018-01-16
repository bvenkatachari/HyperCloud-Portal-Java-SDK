package io.dchq.sdk.core;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.provider.ResourcePool;

public class ResourcePoolServiceImpl extends
		GenericServiceImpl<ResourcePool, ResponseEntity<List<ResourcePool>>, ResponseEntity<ResourcePool>>
		implements ResourcePoolService {
	
	public static final String ENDPOINT = "resourcepools/";

	    /**
	     * @param baseURI  - e.g. https://dchq.io/api/1.0/
	     * @param username - registered username with DCHQ.io
	     * @param password - password used with the username
	     */

	protected ResourcePoolServiceImpl(String baseURI, String username, String password) {

		super(baseURI, ENDPOINT, username, password,
				new ParameterizedTypeReference<ResponseEntity<List<ResourcePool>>>() {
				}, new ParameterizedTypeReference<ResponseEntity<ResourcePool>>() {
				});
	}
	
	
	@Override
	public ResponseEntity<List<ResourcePool>> findAllTenantQuotas(int page, int size) {
		return super.findAll(page, size, "tenant/QUOTA", new ParameterizedTypeReference<ResponseEntity<List<ResourcePool>>>() {});
	}
	
	@Override
	public ResponseEntity<List<ResourcePool>> findAllTenantResourcePool(int page, int size) {
		return super.findAll(page, size, "rptype/RESOURCE_POOL", new ParameterizedTypeReference<ResponseEntity<List<ResourcePool>>>() {});
	}

}
