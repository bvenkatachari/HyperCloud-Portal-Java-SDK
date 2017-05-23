package io.dchq.sdk.core.apps;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.provision.App;
import com.dchq.schema.beans.one.security.EntitlementType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Saurabh Bhatia on 3/1/2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class AppServiceStartTest extends  AppBaseImpl{

        private App app;
        private Blueprint blueprint;
        private String blueprintId;
        private String bluePrintName;
        boolean error;
        private String validationMessage;

        public AppServiceStartTest(
                String blueprintId,
                String blueprintName,
                EntitlementType entitlementType,
                String validationMessage,
                boolean error

        ) {
            // random user name
            String prefix = RandomStringUtils.randomAlphabetic(3);
            blueprintName = prefix + blueprintName;
            blueprintName = org.apache.commons.lang3.StringUtils.lowerCase(blueprintName);

            this.bluePrintName = blueprintName;
            this.blueprintId = blueprintId;
            this.validationMessage=validationMessage;
            this.error = error;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() throws Exception {
            return Arrays.asList(new Object[][]{
                    {"2c9180865a4a48d9015a52b610c8080c", "Override", EntitlementType.OWNER, "\nAll Input Values are normal. Malfunction in SDK", false},
                   // {"402881864e1a36cc014e1a399cf90108", "2TierApp", EntitlementType.OWNER, "\nAll Input Values are normal. Malfunction in SDK", false},
            });
        }

        //Deploy Blueprint Successfully and then Stop the Services and again Start the Services
        @Test
        public void testStartDeployedAppAndWait() {

            ResponseEntity<Blueprint> blueprintResponseEntity = blueprintService.findById(blueprintId);
            if(blueprintResponseEntity != null && !blueprintResponseEntity.isErrors())
            {
            	blueprint = blueprintResponseEntity.getResults();
            }
            if(blueprint !=null)
            {
            	//Override existing blueprint name
            	blueprint.setName(bluePrintName);
            	app = startAppServiceAndWait(blueprint, error, validationMessage);
            	System.out.println("App Provision State we get it : " + app.getProvisionState());
            }
        }

       // Destroy above created app
        @After
        public void testDestroyAppAndWait() {
        	if(app!=null)
        	{
        		destroyAndWait(app);
        	}
        }

    }
