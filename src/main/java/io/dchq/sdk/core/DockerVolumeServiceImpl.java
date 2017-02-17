package io.dchq.sdk.core;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

/**
 * @Author Saurabh Bhatia on 2/17/2017.
 *
 */
public class DockerVolumeServiceImpl extends GenericServiceImpl<DockerVolume, ResponseEntity<List<DockerVolume>>, ResponseEntity<DockerVolume>>
        implements DockerVolumeService  {

    public static final ParameterizedTypeReference<ResponseEntity<List<DockerVolume>>> listTypeReference = new ParameterizedTypeReference<ResponseEntity<List<DockerVolume>>>() {
    };
    public static final ParameterizedTypeReference<ResponseEntity<DockerVolume>> singleTypeReference = new ParameterizedTypeReference<ResponseEntity<DockerVolume>>() {
    };

    public static final String ENDPOINT = "dockervolumes/";

    /**
     * @param baseURI  - e.g. https://dchq.io/api/1.0/
     * @param username - registered username with DCHQ.io
     * @param password - password used with the username
     */
    public DockerVolumeServiceImpl(String baseURI, String username, String password) {
        super(baseURI, ENDPOINT, username, password,
                new ParameterizedTypeReference<ResponseEntity<List<DockerVolume>>>() {
                },
                new ParameterizedTypeReference<ResponseEntity<DockerVolume>>() {
                }
        );
    }
}