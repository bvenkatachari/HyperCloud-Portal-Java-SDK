package io.dchq.sdk.core;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.*;

import java.util.List;

public interface VPCService
  extends GenericService<VirtualPrivateCloud, ResponseEntity<List<VirtualPrivateCloud>>, ResponseEntity<VirtualPrivateCloud>> {
}