package io.dchq.sdk.core;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.network.DockerNetwork;

import java.util.List;

/**
 *
 * @author Saurabh B.
 * @since 1.0
 *
 */
public interface NetworkService extends GenericService<DockerNetwork, ResponseEntity<List<DockerNetwork>>,
        ResponseEntity<DockerNetwork>> {

}