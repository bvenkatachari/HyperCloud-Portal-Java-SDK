/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dchq.sdk.core.machines;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.provider.DockerServer;
import com.dchq.schema.beans.one.security.EntitlementType;
import io.dchq.sdk.core.ServiceFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by Abedeen on 04/05/16.
 */

/**
 * Abstracts class for holding test credentials.
 *
 * @author Abedeen.
 * @since 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerServerFindServiceTest extends DockerServerTest {


    @org.junit.Before
    public void setUp() throws Exception {
        dockerServerService = ServiceFactory.buildDockerServerService(rootUrl, username, password);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
        	{"test", Boolean.FALSE, "VHG01-N03", "cpu=1,memory=1GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\CentOS7HFTemplate.vhdx", "Compute vmSwitch", 1, "2c9180865bb2559a015bd998188e4457", 300000, "", false},

        });
    }


    public DockerServerFindServiceTest(String serverName, Boolean activeFlag, String region, String hardwareID, String image, String networkId, int size, String endpoint, int tinout, String clusterName, boolean success) {
    	String postfix = RandomStringUtils.randomAlphabetic(3);
    	if(clusterName !=null && !clusterName.isEmpty())
		{
			clusterName = clusterName + "-" + postfix;
			datacenterCreated = getDataCenter(clusterName, Boolean.FALSE, EntitlementType.ALL_BLUEPRINTS);
			
			Assert.assertNotNull(datacenterCreated);
			// TODO removed withEndpointType and added network id, it is required to have network ID
			this.dockerServer = new DockerServer().withDatacenter(datacenterCreated).withName(serverName)
					.withInactive(activeFlag).withRegion(region).withImageId(image).withSize(size)
					.withEndpoint(endpoint).withHardwareId(hardwareID).withNetworkId(networkId);
		}
    	else
    	{
    		// TODO cluster not mandatory field 
    		if(serverName!=null && !serverName.isEmpty())
    		{
    			serverName = serverName +"-"+ postfix;
    		}
    		
    		this.dockerServer = new DockerServer().withName(serverName)
					.withInactive(activeFlag).withRegion(region).withImageId(image).withSize(size)
					.withEndpoint(endpoint).withHardwareId(hardwareID).withNetworkId(networkId);
    		
    	}
        maxWaitTime = tinout;
        this.createError = success;
    }

    DockerServer dockerServerFindById;

    @Ignore
    @org.junit.Test
    public void testFind() throws Exception {
        logger.info("Create Machine with Name [{}]", dockerServer.getName());
        ResponseEntity<DockerServer> response = dockerServerService.create(dockerServer);
        String errorMessage = "";
        for (Message message : response.getMessages()) {
            logger.warn("Error while Create request  [{}] ", message.getMessageText());
            errorMessage += ("Error while Create request  [{}] " + message.getMessageText());
        }
        Assert.assertFalse("Machine Creation Replied with Error." + errorMessage, response.isErrors());

        if (response.getTotalElements() == null) {
            logger.info("Expecting No Response for  Machine Create [{}]", dockerServer.getName());

//            assertNotNull(response.getTotalElements());
            Assert.assertEquals("", 1, wait(7000));
            dockerServerResponseEntity = dockerServerService.search(dockerServer.getName(), 0, 1);
            errorMessage = "";
            for (Message message : dockerServerResponseEntity.getMessages()) {
                logger.warn("Error while Create request  [{}] ", message.getMessageText());
                errorMessage += message.getMessageText() + "\n";
            }

            assertNotNull(errorMessage, dockerServerResponseEntity.getResults());
            assertFalse(dockerServerResponseEntity.isErrors());

            if (dockerServerResponseEntity.getResults() != null) {

                String serverStatus = "";
                for (DockerServer searchDocker : dockerServerResponseEntity.getResults()) {
                    dockerServerProvisioning = searchDocker;
                }
                Assert.assertNotNull("Machine Not created...", dockerServerProvisioning.getId());
                dockerServerCreated = validateProvision(dockerServerProvisioning, "PROVISIONING");
                if (dockerServerCreated != null) {

                    Assert.assertEquals(dockerServer.getInactive(), dockerServerCreated.getInactive());
                    Assert.assertEquals(dockerServer.getRegion(), dockerServerCreated.getRegion());
                    //     Assert.assertEquals(dockerServer.getSize(), dockerServerCreated.getSize());
                    Assert.assertEquals(dockerServer.getEndpoint(), dockerServerCreated.getEndpoint());
                    Assert.assertEquals(dockerServer.getEndpointType(), dockerServerCreated.getEndpointType());

                    logger.info("Find Machine with ID [{}]", dockerServerCreated.getId());
                    response = dockerServerService.findById(dockerServerCreated.getId()
                    );
                    errorMessage = "";
                    for (Message message : response.getMessages()) {
                        logger.warn("Error while FindById request  [{}] ", message.getMessageText());
                        errorMessage += ("Error while FindById request  [{}] " + message.getMessageText());
                    }
                    assertNotNull(response);
                    assertNotNull(response.isErrors());

                    Assert.assertFalse("Machine FindById Replied with Error." + errorMessage, response.isErrors());

                    if (response.getResults() != null) {


                        assertNotNull(response.getResults());
                        assertNotNull(response.getResults().getId());
                        dockerServerFindById = response.getResults();
                        logger.info("Find Machine with ID [{}] complete", dockerServerFindById.getId());
                        Assert.assertEquals(dockerServerCreated.getName(), dockerServerFindById.getName());
                        Assert.assertEquals(dockerServerCreated.getInactive(), dockerServerFindById.getInactive());
                        Assert.assertEquals(dockerServerCreated.getRegion(), dockerServerFindById.getRegion());
                        Assert.assertEquals(dockerServerCreated.getSize(), dockerServerFindById.getSize());
                        Assert.assertEquals(dockerServerCreated.getEndpoint(), dockerServerFindById.getEndpoint());
                        Assert.assertEquals(dockerServerCreated.getEndpointType(), dockerServerFindById.getEndpointType());


                    }


                }

            }


        }


    }

    @After
    public void cleanUp() throws Exception {
        logger.info("cleaning up...");


        if (dockerServerProvisioning != null) {
            logger.info("Deleting Machine ");
            dockerServerService.delete(dockerServerProvisioning.getId(), true);
            assertEquals("Unable to Destroy Object after Test ", "DESTROYED", validateProvision(dockerServerProvisioning, "DESTROYING").getDockerServerStatus().name());
        }
        if (datacenterCreated != null) {
            logger.info("Deleting Cluster ");
            assertFalse(dataCenterService.delete(datacenterCreated.getId()).isErrors());
        }

    }


}
