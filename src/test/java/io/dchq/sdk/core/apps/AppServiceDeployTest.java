package io.dchq.sdk.core.apps;

import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.provision.App;
import org.junit.After;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

public class AppServiceDeployTest extends AppBaseImplTest {

    private App app;
    private Blueprint blueprint;


    public AppServiceDeployTest(
            String blueprintId
    ) {
        // random user name
        blueprint = blueprintService.findById(blueprintId).getResults();

        // TODO
        //this.errorMessage = message;
        //this.success = success;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][]{
                {"2c9180865a4a48d9015a52b610c8080c" /* TODO - pass name, entitlement, success, errorMessages etc */}
        });
    }


    //Deploy Blueprint Successfully
    @Test
    public void testDeployAppAndWait() {

        Blueprint blueprint = null;
        app = deployAndWait(blueprint /* TODO - pass errorMessage, success */);
        System.out.println("App Provision State we get it : " + app.getProvisionState());
    }

    // Destroy above created app
    @After
    public void testDestroyAppAndWait() {

        destroyAndWait(app);

    }


}
