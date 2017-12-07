package io.dchq.sdk.core;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;
import com.dchq.schema.beans.one.dockervolume.SDVolumeRequest;


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
    
    @Override
    public ResponseEntity<DockerVolume> attachVolume(String volumeId, String machineId) {
        return super.doPost(null, volumeId+"/attach/"+machineId);
    }
    
    @Override
    public ResponseEntity<DockerVolume> detachVolume(String volumeId, String machineId) {
        return super.doPost(null, volumeId+"/detach/"+machineId);
    }
    
    @Override
    public ResponseEntity<DockerVolume> createBlueprintVolume(SDVolumeRequest blueprint) {
        return super.doPost(blueprint, "sdv");
    }
}