package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.provider.DockerServer;
import com.dchq.schema.beans.one.provider.SDIRequest;

/**
 * <code>DockerServer</code> endpoint API calls.
 *
 * @author Atef Ahmed
 * @since 1.0
 */
public interface DockerServerService extends GenericService<DockerServer, ResponseEntity<List<DockerServer>>, ResponseEntity<DockerServer>> {

    /**
     * Find the status of a <code>DockerServer</code> by id.
     *
     * @return Specific DockerServer response
     */
    ResponseEntity<DockerServer> findStatusById(String id);

    /**
     * Ping  a <code>DockerServer</code> by id.
     *
     * @return Specific DockerServer response
     */
    ResponseEntity<DockerServer> pingServerById(String id);

    /**
     * Deploy's blueprint. Customize name, env, reason, cluster and compose properties.
     *
     * @param blueprintId
     * @return
     */
    ResponseEntity<List<DockerServer>> deploy(SDIRequest request);


    /**
     * Find historical CPU, memory utilization, monitoring data of a <code>DockerServer</code> by id.
     *
     * @return Specific DockerServer response
     */
    ResponseEntity<DockerServer> findMonitoredDataById(String id);
}
