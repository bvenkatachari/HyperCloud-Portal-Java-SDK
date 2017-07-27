package io.dchq.sdk.core;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;

import java.util.List;

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
}