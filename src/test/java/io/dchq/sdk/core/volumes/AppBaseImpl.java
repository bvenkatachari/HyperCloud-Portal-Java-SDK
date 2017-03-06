package io.dchq.sdk.core.volumes;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.PkEntityBase;
import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.container.Container;
import com.dchq.schema.beans.one.container.ContainerStatus;
import com.dchq.schema.beans.one.provision.*;
import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.AppService;
import io.dchq.sdk.core.BlueprintService;
import io.dchq.sdk.core.ServiceFactory;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
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
            Assert.assertEquals(validationMessage, error, appResponseEntity.isErrors());
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

        logger.info("Running App: " + app.getName() + "  , with no. of containers :  " + app.getContainers().size());
        logger.info("Process to Stop App ID : " + app.getId() + " started");

        // Stop above deployed App
        List<Container> con = app.getContainers();

        ResponseEntity<App> appStopResponseEntity = appService.stop(app.getId());

        if (appStopResponseEntity.isErrors()) {
            for (Message m : appStopResponseEntity.getMessages()) {
                logger.warn("[{}]", m.getMessageText());
                validationMessage = m.getMessageText();
            }
            //check for errors
            Assert.assertEquals(validationMessage, error, appStopResponseEntity.isErrors());
        }

        app = appStopResponseEntity.getResults();

        assertNotNull(appStopResponseEntity.getResults());

        return app;
    }

    public App scaleOutCreateService(Blueprint blueprint, boolean error, String validationMessage) {

        //First Deploy the App and Check Status
        App app = deployAndWait(blueprint, error, validationMessage);

        logger.info("Running App: " + app.getName() + "  , with no. of containers :  " + app.getContainers().size());
        logger.info("Process to Scale-Out for App ID : " + app.getId() + " started");

        //Execute GET request for Above to get Scale out response
        ResponseEntity<AppScaleOutProfile> appServiceScaleOutResponseEntity = appService.findScaleOutCreate(app.getId());

        assertNotNull(appServiceScaleOutResponseEntity.getResults());

        AppScaleOutProfile scaleOutProfile = new AppScaleOutProfile();
        //Scale Out Profile Results
        scaleOutProfile = appServiceScaleOutResponseEntity.getResults();

        //Retrieve Cluster Profile in List
        List<ClusterProfile> clusterProfiles = scaleOutProfile.getClusterProfiles();
        int k = 0;

        //Create new array list to initialize and add cluster profile object.
        ArrayList<ClusterProfile> clusterProfileModified = new ArrayList<ClusterProfile>();

        // ClusterProfile setClusterProfileValues = new ClusterProfile();

        for (ClusterProfile setClusterProfileValues : clusterProfiles) {
            if (k < clusterProfiles.size()) {

                logger.info("Total Active Nodes: " + setClusterProfileValues.getTotalActive());

                //Store total Active node value
                int totalActive = setClusterProfileValues.getTotalActive();

                //Set NewActive Value
                setClusterProfileValues.setNewActive(totalActive + 1);
                logger.info("Set Cluster New Active to: " + setClusterProfileValues.getNewActive());

                logger.info("Check Cluster Active : " + setClusterProfileValues.getActive());
                setClusterProfileValues.setActive(true);

                //Add ClusterProfile Object in new Arraylist of ClusterProfile
                clusterProfileModified.add(setClusterProfileValues);
                k++;
            }
            ;

        }

        //Set ScaleOutProfile value to Create new Scale out
        scaleOutProfile.setNote("New Note Added");
        scaleOutProfile.setClusterProfiles(clusterProfileModified);

        logger.info("Current Cluster Profile Size before Scaleout is : " + scaleOutProfile.getClusterProfiles().size());

        ResponseEntity<App> appScaleOutCreateResponseEntity = appService.postScaleOutCreateNow(scaleOutProfile, app.getId());

        if (appScaleOutCreateResponseEntity.isErrors()) {
            for (Message m : appScaleOutCreateResponseEntity.getMessages()) {
                logger.warn("[{}]", m.getMessageText());
                validationMessage = m.getMessageText();
            }
            //check for errors
            Assert.assertEquals(validationMessage, error, appScaleOutCreateResponseEntity.isErrors());
        }

        //  scaleOutResult = appScaleOutCreateResponseEntity.getResults();
        assertNotNull(appScaleOutCreateResponseEntity.getResults());

        //Validate Scale Out Create New Size
        assertEquals(Math.toIntExact(clusterProfileModified.get(0).getNewActive()), appScaleOutCreateResponseEntity.getResults().getContainers().size());

        logger.info("New Cluster Profile Size after Scale Out displaying now : " + appScaleOutCreateResponseEntity.getResults().getContainers().size());

        app = appService.findById(app.getId()).getResults();

        int j = app.getContainers().size();

        //Validate Both Scale Out Container Status should be in Running State
       for (int i=0; i<j ;i++) {

            if (app != null) {

                ContainerStatus status = app.getContainers().get(i).getContainerStatus();

                while ((status != ContainerStatus.RUNNING) && (System.currentTimeMillis() < endTime)) {

                    logger.info("Found Container {" +i+ "}  with status: ", status);

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        logger.warn(e.getLocalizedMessage(), e);
                    }
                    app = appService.findById(app.getId()).getResults();
                    status = app.getContainers().get(i).getContainerStatus();
                }

            }
        }

        logger.info("Scale-Out Create Successfully Completed");


        app = appService.findById(app.getId()).getResults();

        return app;
    }

    public App scaleInRemoveService(Blueprint blueprint, boolean error, String validationMessage) throws InterruptedException {

        // First Deploy the App and Check Status and Call Scale Out to create extra node
        App app = scaleOutCreateService(blueprint, error, validationMessage);

        logger.info("Running App: " + app.getName() + "  , with no. of containers :  " + app.getContainers().size());
        logger.info("Process to Scale-In-Remove for App ID : " + app.getId() + " started");

        Thread.sleep(10000);

        //Execute GET request for Above to get Scale In response
        ResponseEntity<AppScaleInProfile> appServiceScaleInResponseEntity = appService.findScaleIn(app.getId());

        assertNotNull(appServiceScaleInResponseEntity.getResults());

        AppScaleInProfile scaleInProfile = new AppScaleInProfile();
        //Scale In Profile Results
        scaleInProfile = appServiceScaleInResponseEntity.getResults();

        Thread.sleep(5000);

        //Retrieve Cluster Profile in List
        List<ClusterProfile> clusterProfilesForScaleIN = scaleInProfile.getClusterProfiles();
        int k = 0;

        //Create new array list to initialize and add cluster profile object.
        ArrayList<ClusterProfile> clusterProfileForScaleInModified = new ArrayList<ClusterProfile>();

        // ClusterProfile setClusterProfileValues = new ClusterProfile();

        for (ClusterProfile setClusterProfileforScaleInValues : clusterProfilesForScaleIN) {
            if (k < clusterProfilesForScaleIN.size()) {

                logger.info("Total Active Nodes before Scale-In: " + setClusterProfileforScaleInValues.getTotalActive());

                //Store total Active node value
                int totalActive = setClusterProfileforScaleInValues.getTotalActive();

                //Set NewActive Value
                setClusterProfileforScaleInValues.setNewActive(totalActive - 1);
                logger.info("Set Cluster New Active node to: " + setClusterProfileforScaleInValues.getNewActive());

                logger.info("Check Cluster Active : " + setClusterProfileforScaleInValues.getActive());
                setClusterProfileforScaleInValues.setActive(true);

                //Add ClusterProfile Object in new Arraylist of ClusterProfile
                clusterProfileForScaleInModified.add(setClusterProfileforScaleInValues);
                k++;
            }
            ;

        }

        //Set ScaleInProfile value to Create new Scale out
        scaleInProfile.setNote("Note Added");
        scaleInProfile.setClusterProfiles(clusterProfileForScaleInModified);

        logger.info("Current Cluster Profile Size before Scale-In is : " + scaleInProfile.getClusterProfiles().size());

        ResponseEntity<App> appScaleInRemoveResponseEntity = appService.postScaleInRemoveNow(scaleInProfile, app.getId());

        if (appScaleInRemoveResponseEntity.isErrors()) {
            for (Message m : appScaleInRemoveResponseEntity.getMessages()) {
                logger.warn("[{}]", m.getMessageText());
                validationMessage = m.getMessageText();
            }
            //check for errors
            Assert.assertEquals(validationMessage, error, appScaleInRemoveResponseEntity.isErrors());
        }

        assertNotNull(appScaleInRemoveResponseEntity.getResults());

        app = appService.findById(app.getId()).getResults();

        //Validate Scale In Remove Container Status
        ContainerStatus status = app.getContainers().get(0).getContainerStatus();

        if (app != null) {

            while ((status != ContainerStatus.DESTROYED) && (System.currentTimeMillis() < endTime)) {
                logger.info("Found Container with status [{}]", status);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    logger.warn(e.getLocalizedMessage(), e);
                }
                app = appService.findById(app.getId()).getResults();
                status = app.getContainers().get(0).getContainerStatus();
            }

            logger.info("Finished provisioning for Container Removed Status after Scale-In is  : " + status);
            logger.info("Scale-In Remove Successfully Completed");
        }
        app = appService.findById(app.getId()).getResults();
        return app;
    }
}

