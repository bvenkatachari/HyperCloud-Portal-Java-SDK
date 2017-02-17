package io.dchq.sdk.core;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.network.DockerNetwork;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

/**
 * @Author Saurabh Bhatia on 2/17/2017.
 *
 */
public class NetworkServiceImpl extends GenericServiceImpl<DockerNetwork, ResponseEntity<List<DockerNetwork>>, ResponseEntity<DockerNetwork>>
        implements NetworkService  {

    public static final ParameterizedTypeReference<ResponseEntity<List<DockerNetwork>>> listTypeReference = new ParameterizedTypeReference<ResponseEntity<List<DockerNetwork>>>() {
    };
    public static final ParameterizedTypeReference<ResponseEntity<DockerNetwork>> singleTypeReference = new ParameterizedTypeReference<ResponseEntity<DockerNetwork>>() {
    };

    public static final String ENDPOINT = "networks/";

    /**
     * @param baseURI  - e.g. https://dchq.io/api/1.0/
     * @param username - registered username with DCHQ.io
     * @param password - password used with the username
     */
    public NetworkServiceImpl(String baseURI, String username, String password) {
        super(baseURI, ENDPOINT, username, password,
                new ParameterizedTypeReference<ResponseEntity<List<DockerNetwork>>>() {
                },
                new ParameterizedTypeReference<ResponseEntity<DockerNetwork>>() {
                }
        );
    }
}