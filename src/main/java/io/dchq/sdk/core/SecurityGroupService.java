package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.SecurityGroup;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

public interface SecurityGroupService
		extends GenericService<SecurityGroup, ResponseEntity<List<SecurityGroup>>, ResponseEntity<SecurityGroup>> {

}
