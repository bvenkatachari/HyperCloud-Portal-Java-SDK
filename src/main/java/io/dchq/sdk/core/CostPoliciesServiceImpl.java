package io.dchq.sdk.core;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.price.PriceProfile;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

/**
 * Created by Saurabh Bhatia on 4/3/2017.
 */
public class CostPoliciesServiceImpl extends GenericServiceImpl<PriceProfile, ResponseEntity<List<PriceProfile>>, ResponseEntity<PriceProfile>>
        implements CostPoliciesService  {

    public static final ParameterizedTypeReference<ResponseEntity<List<PriceProfile>>> listTypeReference = new ParameterizedTypeReference<ResponseEntity<List<PriceProfile>>>() {
    };
    public static final ParameterizedTypeReference<ResponseEntity<PriceProfile>> singleTypeReference = new ParameterizedTypeReference<ResponseEntity<PriceProfile>>() {
    };

    public static final String ENDPOINT = "priceprofiles/";

    /**
     * @param baseURI  - e.g. https://dchq.io/api/1.0/
     * @param username - registered username with DCHQ.io
     * @param password - password used with the username
     */
    public CostPoliciesServiceImpl(String baseURI, String username, String password) {
        super(baseURI, ENDPOINT, username, password,
                new ParameterizedTypeReference<ResponseEntity<List<PriceProfile>>>() {
                },
                new ParameterizedTypeReference<ResponseEntity<PriceProfile>>() {
                }
        );
    }
}
