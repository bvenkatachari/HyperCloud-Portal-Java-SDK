package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.Rule;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

public interface NetworkACLRuleService extends GenericService<Rule, ResponseEntity<List<Rule>>, ResponseEntity<Rule>> {
	
}
