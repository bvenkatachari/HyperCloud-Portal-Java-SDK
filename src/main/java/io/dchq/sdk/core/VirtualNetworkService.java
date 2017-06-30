package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vlan.VirtualNetwork;

public interface VirtualNetworkService
		extends GenericService<VirtualNetwork, ResponseEntity<List<VirtualNetwork>>, ResponseEntity<VirtualNetwork>> {

}
