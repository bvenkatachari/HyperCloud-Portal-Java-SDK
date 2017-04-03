package io.dchq.sdk.core;



import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.quotapolicy.QuotaPolicy;

import java.util.List;
/**
 * Created by Saurabh Bhatia on 4/3/2017.
 */

public interface QuotaPolicyService extends GenericService<QuotaPolicy, ResponseEntity<List<QuotaPolicy>>,
        ResponseEntity<QuotaPolicy>> {

}
