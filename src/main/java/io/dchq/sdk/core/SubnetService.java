package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.Subnet;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/
public interface SubnetService
  extends GenericService<Subnet, ResponseEntity<List<Subnet>>, ResponseEntity<Subnet>> {
}