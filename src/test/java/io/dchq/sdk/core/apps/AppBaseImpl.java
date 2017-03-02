package io.dchq.sdk.core.apps;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.PkEntityBase;
import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.container.Container;
import com.dchq.schema.beans.one.container.ContainerState;
import com.dchq.schema.beans.one.container.ContainerStatus;
import com.dchq.schema.beans.one.provision.*;
import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.AppService;
import io.dchq.sdk.core.BlueprintService;
import io.dchq.sdk.core.ServiceFactory;
import org.junit.Assert;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by Saurabh Bhatia on 2/27/2017.
 */

public class AppBaseImpl extends AbstractServiceTest implements AppBase {

    protected AppService appService;
    protected BlueprintService blueprintService;
//    protected  AppScaleOutProfile scaleOutProfile;

    @org.junit.Before
    public void setUp() throws Exception {
        appService = ServiceFactory.buildAppService(rootUrl, username, password);
        blueprintService = ServiceFactory.buildBlueprintService(rootUrl, username, password);
    }

    //private Blueprint blueprint;
    //private App app;
    long startTime = System.currentTimeMillis();
    long endTime = startTime + (60 * 60 * 50); // this is for 3 mins

    public App deployAndWait(Blueprint blueprint, boolean error, String validationMessage) {

        //Set Cluster ID
        PkEntityBase dc = new PkEntityBase();
        dc.setId(clusterID);
        blueprint.setDatacenter(dc);

        // Deploying using blueprint object
        ResponseEntity<App> appResponseEntity = appService.deploy(blueprint);

        if (appResponseEntity.isErrors()) {
            for (Message m : appResponseEntity.getMessages()) {
                logger.warn("[{}]", m.getMessageText());
                validationMessage = m.getMessageText();
            }
            //check for errors
            Assert.assertEquals(validationMessage ,error, appResponseEntity.isErrors());
        }
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

            //    Assert.assertEquals(1,app.getContainers().size() );

            for (Container m : app.getContainers()) {
                logger.info("No. of Container : " + app.getContainers().size());
                //    System.out.println("Display Container ID: " +m.getContainerId() + "    & Status : " + m.getContainerStatus());
            }
        }
        if (app.getProvisionState() != ProvisionState.RUNNING) {
            Assert.fail("App Status doesn't get changed to RUNNING, still showing : " + app.getProvisionState());
        }
        app = appService.findById(app.getId()).getResults();

        return app;
    }

    public void destroyAndWait(App app) {

        System.out.println("Destroy Process Started for App ID:  " + app.getId());

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

    public App stopAppServiceAndWait(Blueprint blueprint, boolean error, String validationMessage) {

        //First Deploy the App and Check Status
        App app = deployAndWait(blueprint, error, validationMessage);

        logger.info("Running App: " +app.getName()+ "  , with no. of containers :  " +app.getContainers().size());
        logger.info("Process to Stop App ID : " +app.getId()+ " started");

        // Stop above deployed App
     List <Container> con = app.getContainers();

        ResponseEntity<App> appStopResponseEntity = appService.stop(app.getId());

        if (appStopResponseEntity.isErrors()) {
            for (Message m : appStopResponseEntity.getMessages()) {
                logger.warn("[{}]", m.getMessageText());
                validationMessage = m.getMessageText();
            }
            //check for errors
            Assert.assertEquals(validationMessage ,error, appStopResponseEntity.isErrors());
        }

         app = appStopResponseEntity.getResults();

        assertNotNull(appStopResponseEntity.getResults());

        return  app;
    }

    public App scaleOutCreateService(Blueprint blueprint, boolean error, String validationMessage) {

        //First Deploy the App and Check Status
        App app = deployAndWait(blueprint, error, validationMessage);

        logger.info("Running App: " +app.getName()+ "  , with no. of containers :  " +app.getContainers().size());
        logger.info("Process to Scale-Out App ID : " +app.getId()+ " started");

        ResponseEntity <App> appServiceScaleOutResponseEntity = appService.findScaleOutCreate(app.getId());

        assertNotNull(appServiceScaleOutResponseEntity.getResults());

    //    App  scaleOutResult = appService.findScaleOutCreate(app.getId()).getResults();

        AppScaleOutProfile scaleOutProfile = new AppScaleOutProfile();
        scaleOutProfile.setNote("ABC");

        // To Do Set   ClusterProfile active / inactive & new node value;
        //   scaleOutProfile.getClusterProfiles();

        ResponseEntity<App> appScaleOutCreateResponseEntity = appService.postScaleOutCreateNow(scaleOutProfile,app.getId());

        if (appScaleOutCreateResponseEntity.isErrors()) {
            for (Message m : appScaleOutCreateResponseEntity.getMessages()) {
                logger.warn("[{}]", m.getMessageText());
                validationMessage = m.getMessageText();
            }
            //check for errors
            Assert.assertEquals(validationMessage ,error, appScaleOutCreateResponseEntity.isErrors());
        }

      //  scaleOutResult = appScaleOutCreateResponseEntity.getResults();

        assertNotNull(appScaleOutCreateResponseEntity.getResults());

        return  app;
    }

    public App scaleInRemoveService(Blueprint blueprint, boolean error, String validationMessage) {

        //First Deploy the App and Check Status and Call Scale Out to create extra node
     //   App app = scaleOutCreateService(blueprint, error, validationMessage);

        App app = deployAndWait(blueprint, error, validationMessage);

        logger.info("Running App: " +app.getName()+ "  , with no. of containers :  " +app.getContainers().size());
        logger.info("Process to Scale-In-Remove for App ID : " +app.getId()+ " started");

        ResponseEntity <App> appServiceScaleInResponseEntity = appService.findScaleIn(app.getId());

        assertNotNull(appServiceScaleInResponseEntity.getResults());

        //    App  scaleOutResult = appService.findScaleOutCreate(app.getId()).getResults();

        AppScaleInProfile scaleInProfile = new AppScaleInProfile();
        scaleInProfile.setNote("ABC");

        // To Do Set   ClusterProfile active / inactive & new node value;
        //   scaleInProfile.getClusterProfiles();

        ResponseEntity<App> appScaleInRemoveResponseEntity = appService.postScaleInRemoveNow(scaleInProfile,app.getId());

        if (appScaleInRemoveResponseEntity.isErrors()) {
            for (Message m : appScaleInRemoveResponseEntity.getMessages()) {
                logger.warn("[{}]", m.getMessageText());
                validationMessage = m.getMessageText();
            }
            //check for errors
            Assert.assertEquals(validationMessage ,error, appScaleInRemoveResponseEntity.isErrors());
        }

        //  scaleOutResult = appScaleOutCreateResponseEntity.getResults();

        assertNotNull(appScaleInRemoveResponseEntity.getResults());

        return  app;
    }
}
