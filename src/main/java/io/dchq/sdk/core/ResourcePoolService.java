package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.provider.ResourcePool;

public interface ResourcePoolService
  extends GenericService<ResourcePool, ResponseEntity<List<ResourcePool>>, ResponseEntity<ResourcePool>> {
	
	ResponseEntity<List<ResourcePool>> findAllTenantQuotas(int page, int size);
	ResponseEntity<List<ResourcePool>> findAllTenantResourcePool(int page, int size);
}