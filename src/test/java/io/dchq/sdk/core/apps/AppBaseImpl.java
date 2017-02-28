package io.dchq.sdk.core.apps;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.PkEntityBase;
import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.provision.App;
import com.dchq.schema.beans.one.provision.ProvisionState;
import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.AppService;
import io.dchq.sdk.core.BlueprintService;
import io.dchq.sdk.core.ServiceFactory;
import org.junit.Assert;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by Saurabh Bhatia on 2/27/2017.
 */

public class AppBaseImpl extends AbstractServiceTest implements AppBase {

    protected AppService appService;
    protected BlueprintService blueprintService;


    @org.junit.Before
    public void setUp() throws Exception {
        appService = ServiceFactory.buildAppService(rootUrl, username, password);
        blueprintService = ServiceFactory.buildBlueprintService(rootUrl, username, password);
    }

    //private Blueprint blueprint;
    //private App app;
    long startTime = System.currentTimeMillis();
    long endTime = startTime + (60 * 60 * 50); // this is for 3 mins

    public App deployAndWait(Blueprint blueprint) {

         blueprint.setName("Override Existing");
  //      blueprint.setYml("LB:\n image: nginx:latest");

        //Set Cluster ID
        PkEntityBase dc = new PkEntityBase();
        dc.setId(clusterID);
        blueprint.setDatacenter(dc);

        // Deploying using blueprint object
        ResponseEntity<App> appResponseEntity = appService.deploy(blueprint);
        App app = appResponseEntity.getResults();

        assertNotNull(appResponseEntity.getResults());

        if (app != null) {
            while ((app.getProvisionState() != ProvisionState.RUNNING) && (app.getProvisionState() != ProvisionState.PROVISIONING_FAILED) && (System.currentTimeMillis() < endTime)) {
                logger.info("Found app [{}] with status [{}]", app.getName(), app.getProvisionState());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    logger.warn(e.getLocalizedMessage(), e);
                }
                app = appService.findById(app.getId()).getResults();
            }
            logger.info("Fished provisioning app [{}] with status [{}]", app.getName(), app.getProvisionState());

            assertNotNull(app.getId());
            Assert.assertEquals("App Name Mismatch", blueprint.getName(), app.getName());

        }
        if (app.getProvisionState() != ProvisionState.RUNNING) {
            Assert.fail("App Status doesn't get changed to RUNNING, still showing : " + app.getProvisionState());
        }
        appResponseEntity = appService.findById(app.getId());

        return app;
    }

    public void destroyAndWait(App app) {

        System.out.println("Print Before  App ID:  " + app.getId());

        ResponseEntity<App> response = null;
        if (app != null) {

            response = appService.destroy(app.getId());
            System.out.println("Print App ID:  " + app.getId());

            app = appService.findById(app.getId()).getResults();
            while ((app.getProvisionState() != ProvisionState.DESTROYED) && (System.currentTimeMillis() < endTime)) {
                logger.info("Destroying app [{}] with status [{}]", app.getName(), app.getProvisionState());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    logger.warn(e.getLocalizedMessage(), e);
                }
                for (Message message : response.getMessages()) {
                    logger.warn("Error App deletion: [{}] ", message.getMessageText());
                }
                app = appService.findById(app.getId()).getResults();
            }

            if (app.getProvisionState() != ProvisionState.DESTROYED) {
                Assert.fail("App Status doesn't get changed to DESTROYED, still showing : " + app.getProvisionState());
            }
            logger.info("Fished Destroying provisioning app [{}] with status [{}]", app.getName(), app.getProvisionState());
        }
       // response = appService.findById(app.getId());

    }

}
