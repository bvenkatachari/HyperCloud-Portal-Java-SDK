package io.dchq.sdk.core;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.provision.App;
import com.dchq.schema.beans.one.provision.AppLifecycleProfile;
import com.dchq.schema.beans.one.provision.AppScaleInProfile;
import com.dchq.schema.beans.one.provision.AppScaleOutProfile;

import java.util.List;

/**
 * <code>App</code> endpoint API calls.
 *
 * @author Atef Ahmed
 * @author Intesar Mohammed
 * @contributor Saurabh B.
 * @since 1.0
 */
public interface AppService extends GenericService<App, ResponseEntity<List<App>>, ResponseEntity<App>> {

    /**
     * Find active apps entitled <code>App</code>
     *
     * @return ResponseEntity, list of live and active apps by the logged in user.
     */
    ResponseEntity<List<App>> findActive();

    /**
     * Find destroyed apps entitled <code>App</code>
     *
     * @return ResponseEntity, list of destroyed apps by the logged in user.
     */
    ResponseEntity<List<App>> findDestroyed();

    /**
     * Find deployed apps entitled <code>App</code>
     *
     * @return ResponseEntity, list of deployed apps by the logged in user.
     */
    ResponseEntity<List<App>> findDeployed();

    /**
     * Find backed up apps entitled <code>App</code>
     *
     * @return ResponseEntity, specific backed-up app by ID.
     */
    ResponseEntity<App> findBackedupById(String id);

    /**
     * Find live container plugin.
     *
     * @return ResponseEntity, specific plugin created for a live app by ID.
     */
    ResponseEntity<App> findPluginById(String id);

    /**
     * Find rolledback apps
     *
     * @return ResponseEntity, specific rolledback app by ID.
     */
    ResponseEntity<App> findRolledback(String id);


    /**
     * Find scaled-out app
     *
     * @return ResponseEntity, specific scaled-out response for a live app by ID.
     */
    ResponseEntity<AppScaleOutProfile> findScaleOutCreate(String id);

    /**
     * Find scaled-in app
     *
     * @return ResponseEntity, specific scaled-in response for a live app by ID.
     */
    ResponseEntity<AppScaleInProfile> findScaleIn(String id);

    /**
     * Find historical cpu, memory utilization/monitoring data for the app by ID.
     *
     * @return ResponseEntity, specific statistics for an app.
     */
    ResponseEntity<List<App>> findMonitored(String id);

    /**
     * Deploy's blueprint by id into user default cluster.
     *
     * @param blueprintId
     * @return
     */
    ResponseEntity<App> deploy(String blueprintId);

    /**
     * Deploy's blueprint. Customize name, env, reason, cluster and compose properties.
     *
     * @param blueprintId
     * @return
     */
    ResponseEntity<App> deploy(Blueprint blueprintId);

    /**
     * Destroy's App.
     * @param appId
     * @return
     */
    ResponseEntity<App> destroy(AppLifecycleProfile profile, String appId);

    /**
     * Stop App Service.
     *
   //  * @param container
     * @param appId
     * @return
     */
   ResponseEntity<App> stop(AppLifecycleProfile appLifecycleProfile, String appId);

    /**
     * Start App Service.
     *
     * @param appId
     * @return
     */

    ResponseEntity<App> start(AppLifecycleProfile appLifecycleProfile, String appId);

    /**
     * Create Scale Out Service.
     * @param appScaleOutProfile
     * @param appId
     * @return
     */

    ResponseEntity<App> postScaleOutCreateNow(AppScaleOutProfile appScaleOutProfile, String appId);

    /**
     * Scale In Remove Service.
     * @param appScaleInProfile
     * @param appId
     * @return
     */
    ResponseEntity<App> postScaleInRemoveNow(AppScaleInProfile appScaleInProfile, String appId);
//    ResponseEntity<App> redeploy(String appId);


}
