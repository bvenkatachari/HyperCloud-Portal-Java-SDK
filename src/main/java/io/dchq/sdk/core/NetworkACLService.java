package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.NetworkACL;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/
public interface NetworkACLService
  extends GenericService<NetworkACL, ResponseEntity<List<NetworkACL>>, ResponseEntity<NetworkACL>> {
}