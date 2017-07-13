package io.dchq.sdk.core.machines;

import static junit.framework.TestCase.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.PkEntityBase;
import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.provider.DockerServer;
import com.dchq.schema.beans.one.provider.DockerServerStatus;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dchq.sdk.core.BlueprintService;
import io.dchq.sdk.core.DockerServerService;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.apps.AppBaseImpl;

/**
 * Created by Saurabh Bhatia on 3/16/2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerServerDeployTest extends AppBaseImpl{

    protected DockerServerService vmServer;
    protected BlueprintService blueprintService;
//    protected  AppScaleOutProfile scaleOutProfile;

    @org.junit.Before
    public void setUp() throws Exception {
        vmServer = ServiceFactory.buildDockerServerService(rootUrl, cloudadminusername, cloudadminpassword);
        blueprintService = ServiceFactory.buildBlueprintService(rootUrl, cloudadminusername, cloudadminpassword);
    }

        private DockerServer vm;
        private Blueprint blueprint;
        private String blueprintId;
        private String blueprintName;
        boolean error;
        private String validationMessage;

        public DockerServerDeployTest(
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

            this.blueprintName = blueprintName;
//       this.blueprint.setName(blueprintName);
            this.blueprintId = blueprintId;
            this.validationMessage=validationMessage;
            this.error = error;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() throws Exception {
            return Arrays.asList(new Object[][]{
                    {"2c9180865d312fc4015d3157aa95006e", "Override", EntitlementType.OWNER, "\nAll Input Values are normal. Malfunction in SDK", false}
            });
        }

        //Deploy Blueprint Successfully
    
        @Ignore
        @Test
        public void testDeployAppAndWait() {

            ResponseEntity<Blueprint> blueprintResponseEntity = blueprintService.findById(blueprintId);
            blueprint = blueprintResponseEntity.getResults();

            //Override existing blueprint name
            blueprint.setName(blueprintName);

            //  app = deployAndWait(blueprint, error, validationMessage);

            //Set Cluster ID
            PkEntityBase dc = new PkEntityBase();
            dc.setId(clusterID);
            blueprint.setDatacenter(dc);

           //Converting Blueprint Object to JSON Object
           /* try {
            	 ObjectMapper mapper = new ObjectMapper();
				String blueprintJson = mapper.writeValueAsString(blueprint);
				logger.info("BluePrint JSON : " + blueprintJson);
			} catch (JsonProcessingException ex) {
				logger.error(ex.getMessage());
			}*/
            
            // Deploying using blueprint object
            ResponseEntity<DockerServer> vmResponseEntity = vmServer.deploy(blueprint);

            if (vmResponseEntity.isErrors()) {
                for (Message m : vmResponseEntity.getMessages()) {
                    logger.warn("[{}]", m.getMessageText());
                    validationMessage = m.getMessageText();
                }
                //check for errors
                Assert.assertEquals(validationMessage, error, vmResponseEntity.isErrors());
            }
            DockerServer server = vmResponseEntity.getResults();

            assertNotNull(vmResponseEntity.getResults());

            if (server != null) {

                while ((server.getDockerServerStatus() != DockerServerStatus.PROVISIONED) && server.getDockerServerStatus() != DockerServerStatus.PROVISIONING_FAILED) {
                    logger.info("Found app [{}] with status [{}]", server.getName(), server.getDockerServerStatus());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        logger.warn(e.getLocalizedMessage(), e);
                    }
                    server = vmServer.findById(server.getId()).getResults();
                }
               // logger.info("Fished provisioning app [{}] with status [{}]", server.getName(), app.getProvisionState());

                assertNotNull(server.getId());
                Assert.assertEquals("App Name Mismatch", blueprint.getName(), server.getName());

                //    Assert.assertEquals(1,app.getContainers().size() );

                }
            if (server.getDockerServerStatus() != DockerServerStatus.PROVISIONED) {
                Assert.fail("App Status doesn't get changed to RUNNING, still showing : " + server.getDockerServerStatus());
            }
            server = vmServer.findById(server.getId()).getResults();
            System.out.println("App Provision State we get it : " + server.getDockerServerStatus());
        }

   /*     // Destroy above created app
        @After
        public void testDestroyAppAndWait() {
            destroyAndWait(app);
        }*/

    }

