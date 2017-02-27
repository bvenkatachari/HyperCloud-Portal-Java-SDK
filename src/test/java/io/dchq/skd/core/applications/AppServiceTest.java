package io.dchq.skd.core.applications;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.provision.App;

import io.dchq.sdk.core.AppService;
import io.dchq.sdk.core.BlueprintService;
import io.dchq.sdk.core.ServiceFactory;

import org.junit.After;
import org.junit.Test;

public class AppServiceTest extends AppBaseTestDeployDestroyImpl {

    private AppService appService;
    private BlueprintService blueprintService;
    private App app;


    @org.junit.Before
    public void setUp() throws Exception {
        appService = ServiceFactory.buildAppService(rootUrl, username, password);
        blueprintService = ServiceFactory.buildBlueprintService(rootUrl, username, password);
    }

    //Deploy Blueprint Successfully
    @Test
    public void testDeployAppAndWait() {

        ResponseEntity<App> appResponseEntity = deployAndWait(appService, blueprintService);
        app = appResponseEntity.getResults();
        System.out.println("App Provision State we get it : " +app.getProvisionState());
    }

    // Destroy above created app
    @After
    public void testDestroyAppAndWait() {

        ResponseEntity<App> appResponseEntity = destroyAndWait(appService);
        app = appResponseEntity.getResults();
        System.out.println("App Provision State after Destroying is : " +app.getProvisionState());
        logger.info("App Destroyed Successfully, current State :  " +app.getProvisionState());

    }




}
