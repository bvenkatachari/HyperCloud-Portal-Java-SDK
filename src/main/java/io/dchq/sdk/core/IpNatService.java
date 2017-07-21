package io.dchq.sdk.core;

import java.util.List;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.network.IpPool;

public interface IpNatService extends GenericService<IpPool, ResponseEntity<List<IpPool>>, ResponseEntity<IpPool>>{
}
