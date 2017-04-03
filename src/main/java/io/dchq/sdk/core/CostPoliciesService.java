package io.dchq.sdk.core;



import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.price.PriceProfile;

import java.util.List;
/**
 * Created by Saurabh Bhatia on 4/3/2017.
 */
public interface CostPoliciesService extends GenericService<PriceProfile, ResponseEntity<List<PriceProfile>>,
        ResponseEntity<PriceProfile>> {
}
