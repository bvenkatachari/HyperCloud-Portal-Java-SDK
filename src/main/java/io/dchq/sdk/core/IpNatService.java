package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.VpcIpPool;

public interface IpNatService extends GenericService<VpcIpPool, ResponseEntity<List<VpcIpPool>>, ResponseEntity<VpcIpPool>>{
}
