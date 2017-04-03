package io.dchq.sdk.core;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.quotapolicy.QuotaPolicy;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

/**
 * Created by Saurabh Bhatia on 4/3/2017.
 */

public class QuotaPolicyServiceImpl extends GenericServiceImpl<QuotaPolicy, ResponseEntity<List<QuotaPolicy>>, ResponseEntity<QuotaPolicy>>
        implements QuotaPolicyService  {

    public static final ParameterizedTypeReference<ResponseEntity<List<QuotaPolicy>>> listTypeReference = new ParameterizedTypeReference<ResponseEntity<List<QuotaPolicy>>>() {
    };
    public static final ParameterizedTypeReference<ResponseEntity<QuotaPolicy>> singleTypeReference = new ParameterizedTypeReference<ResponseEntity<QuotaPolicy>>() {
    };

    public static final String ENDPOINT = "quotapolicies/";

    /**
     * @param baseURI  - e.g. https://dchq.io/api/1.0/
     * @param username - registered username with DCHQ.io
     * @param password - password used with the username
     */
    public QuotaPolicyServiceImpl(String baseURI, String username, String password) {
        super(baseURI, ENDPOINT, username, password,
                new ParameterizedTypeReference<ResponseEntity<List<QuotaPolicy>>>() {
                },
                new ParameterizedTypeReference<ResponseEntity<QuotaPolicy>>() {
                }
        );
    }
}
