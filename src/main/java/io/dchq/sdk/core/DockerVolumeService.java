package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;
import com.dchq.schema.beans.one.dockervolume.SDVolumeRequest;

/**
 *
 * @author Saurabh B.
 * @since 1.0
 *
 */
public interface DockerVolumeService extends GenericService<DockerVolume, ResponseEntity<List<DockerVolume>>,
        ResponseEntity<DockerVolume>> {

	public ResponseEntity<DockerVolume> attachVolume(String volumeId, String machineId);
	
	public ResponseEntity<DockerVolume> detachVolume(String volumeId, String machineId);
	
	public ResponseEntity<DockerVolume> createBlueprintVolume(SDVolumeRequest blueprint);
}