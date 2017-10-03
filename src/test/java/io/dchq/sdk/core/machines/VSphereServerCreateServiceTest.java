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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.provider.DataCenter;
import com.dchq.schema.beans.one.provider.DockerServer;
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.ServiceFactory;

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
public class VSphereServerCreateServiceTest extends DockerServerTest {


    @org.junit.Before
    public void setUp() {
        dockerServerService = ServiceFactory.buildDockerServerService(rootUrl, cloudadminusername, cloudadminpassword);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
        	{"VSphere", Boolean.FALSE, "cpu=4,memory=1GB,disk=40GB", "VMT-CentOS7", "dvp-vlan-1181", 1, "2c9180875eaa3c49015eb8285b3c6f2d", 300000, "", false, true, true},
        	{"VSphere", Boolean.FALSE, "cpu=4,memory=1GB,disk=40GB", "VMT-W2K16-DC-200v2", "dvp-vlan-1181", 1, "2c9180875eaa3c49015eb8285b3c6f2d", 300000, "", false, false, false}
        	
        });
    }


    public VSphereServerCreateServiceTest(String serverName, Boolean activeFlag, String hardwareID, String image, String networkId, int size, String endpoint, int tinout, String clusterName, boolean success, boolean installAgent, boolean isConnected) {
    	String postfix = RandomStringUtils.randomAlphabetic(3);
    	if(clusterName !=null && !clusterName.isEmpty())
		{
			clusterName = clusterName  + postfix;
			datacenterCreated = getDataCenter(clusterName, Boolean.FALSE, EntitlementType.ALL_BLUEPRINTS);
			
			Assert.assertNotNull(datacenterCreated);
			// TODO removed withEndpointType and added network id, it is required to have network ID
			this.dockerServer = new DockerServer().withDatacenter(datacenterCreated).withName(serverName)
					.withInactive(activeFlag).withImageId(image).withSize(size)
					.withEndpoint(endpoint).withHardwareId(hardwareID).withNetworkId(networkId);
			if(installAgent){
				this.dockerServer.setSkipAgentInstall("false");
				this.dockerServer.setImageUsername("hf");
				this.dockerServer.setImagePassword("HyperGrid123");
			}else {
			    this.dockerServer.setSkipAgentInstall("true");
			}
			
		}
    	else
    	{
    		// TODO cluster not mandatory field 
    		if(serverName!=null && !serverName.isEmpty())
    		{
    			serverName = serverName + postfix;
    			
    			this.dockerServer = new DockerServer().withName(serverName)
					.withInactive(activeFlag).withImageId(image).withSize(size)
					.withEndpoint(endpoint).withHardwareId(hardwareID).withNetworkId(networkId);
    			this.dockerServer.setGroup(serverName);
    			if(installAgent){
    				this.dockerServer.setSkipAgentInstall("false");
    				this.dockerServer.setImageUsername("hf");
    				this.dockerServer.setImagePassword("HyperGrid123");
    			}else {
    			    this.dockerServer.setSkipAgentInstall("true");
    			}
    			
    		}
    		
    	}
        maxWaitTime = tinout;
        this.createError = success;
        this.isConnected = isConnected;
    }


    
    @org.junit.Test
	public void testCreate() throws Exception {

		logger.info("Create Machine with Name [{}]", dockerServer.getName());
		ResponseEntity<DockerServer> response = dockerServerService.create(dockerServer);

		if (!createError) {
			String errorMessage = "";
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
				errorMessage += ("Error while Create request  [{}] " + message.getMessageText());
			}
			Assert.assertFalse("Machine Creation Replied with Error." + errorMessage, response.isErrors());

			if (response.getTotalElements() == null) {
				logger.info("Expecting No Response for  Machine Create [{}]", dockerServer.getName());

				dockerServerCreated = response.getResults();
				String serverStatus = dockerServerCreated.getDockerServerStatus().name();
				
				while(serverStatus.equals("PROVISIONING") && (System.currentTimeMillis() < endTime)){	
					// Wait for some time until state changed from PROVISIONING to CONNECTED/PROVISIONED
					wait(10000); //wait for 10 seconds
					dockerServerCreated = dockerServerService.findById(dockerServerCreated.getId()).getResults();
					serverStatus = dockerServerCreated.getDockerServerStatus().name();
					 logger.info("Current Serverstatus   [{}] ", serverStatus);
			
				}
				
				
				while(isConnected && serverStatus.equals("PROVISIONED") && (System.currentTimeMillis() < endTime)){	
					/*
					 * Noticed, sometimes system takes time to change the status
					 * from "Provisioned" to "Connected" so we don't have any exact
					 * number to wait. Our script will wait for 2-3 mins and if in
					 * that time status won't change than test will fail.
					 */
					wait(10000); //wait for 10 seconds
					dockerServerCreated = dockerServerService.findById(dockerServerCreated.getId()).getResults();
					serverStatus = dockerServerCreated.getDockerServerStatus().name();
					 logger.info("Current Serverstatus   [{}] ", serverStatus);
			
				}
				
				if(isConnected){
					Assert.assertEquals("CONNECTED", dockerServerCreated.getDockerServerStatus().name());
				}else {
					Assert.assertEquals("PROVISIONED", dockerServerCreated.getDockerServerStatus().name());
				}

			}

		} else {

			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
		}

	}

    public DataCenter getDataCenter() {
        setUp();
        DataCenter tempDataCenter = null;
        DockerServer tempDockerServer = createMachine();
        if (tempDockerServer != null) tempDataCenter = tempDockerServer.getDataCenter();
        return tempDataCenter;
    }

    public DockerServer createMachine() {

        logger.info("Create Machine with Name [{}]", dockerServer.getName());
        ResponseEntity<DockerServer> response = dockerServerService.create(dockerServer);

        
        for (Message message : response.getMessages()) {
            logger.warn("Error while Create request  [{}] ", message.getMessageText());
        }


        if (response.getTotalElements() == null) {
            logger.info("Expecting No Response for  Machine Create [{}]", dockerServer.getName());

            dockerServerResponseEntity = dockerServerService.search(dockerServer.getName(), 0, 1);

            for (Message message : dockerServerResponseEntity.getMessages())
                logger.warn("Error while Create request  [{}] ", message.getMessageText());

            if (dockerServerResponseEntity.getResults() != null) {

                for (DockerServer searchDocker : dockerServerResponseEntity.getResults())
                    dockerServerProvisioning = searchDocker;

                dockerServerCreated = validateProvision(dockerServerProvisioning, "PROVISIONING");


            }


        }
        if (dockerServerCreated == null)
            cleanUp();

        return dockerServerCreated;

    }

    @After
    public void cleanUp() {
        logger.info("cleaning up...");


        if (dockerServerCreated != null) {
            logger.info("Deleting Machine ");
            dockerServerService.delete(dockerServerCreated.getId(), true);
            validateProvision(dockerServerCreated, "DESTROYING");

        }
        if (datacenterCreated != null) {
            logger.info("Deleting Cluster ");
            dataCenterService.delete(datacenterCreated.getId());

        }

    }


}
