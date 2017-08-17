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
public class DockerServerCreateServiceTest extends DockerServerTest {


    @org.junit.Before
    public void setUp() {
        dockerServerService = ServiceFactory.buildDockerServerService(rootUrl, cloudadminusername, cloudadminpassword);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
        	
        	//CentOS7HFT
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\CentOS7HFT_DCHQ_Agent.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, true},
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\CentOS7HFT_DCHQ_Docker.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, true},
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\CentOS7_NoDCHQ_NoDocker.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, false},
        	//Ub1604HFT
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub1604HFT_DCHQ_Agent.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, true},
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub1604HFT_DCHQ_Docker.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, true},
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub1604HFT_NoDCHQ_NoDocker.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, false},
        	//Ub14HFT
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub14HFT_DCHQ_Agent.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, true},
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub14HFT_DCHQ_Docker.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, true},
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub14HFT_NoDCHQ_NoDocker.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, false},
        	//Rhel73
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Rhel73_DCHQ_Docker.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, true},
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Rhel73_NoDCHQ_NoDocker.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, false},
        	//Server 2012
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Server2012R2HFTemplate.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, true},
        	//Server 2016
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\2016HFTWDockerV2.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, true},
        	
        	{"automationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\PFSTemplate.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, "", false, true},
        	
        });
    }


    public DockerServerCreateServiceTest(String serverName, Boolean activeFlag, String hardwareID, String image, String networkId, int size, String endpoint, int tinout, String clusterName, boolean success, boolean isConnected) {
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
			this.dockerServer.setSkipAgentInstall("true");
			this.dockerServer.setOperatingSystem("LINUX");
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
    			this.dockerServer.setSkipAgentInstall("true");
    			this.dockerServer.setOperatingSystem("LINUX");
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
					/*Noticed, sometimes system takes time to change the status from ‘Provisioned’ to ‘Connected’ 
					so we don’t have any exact number to wait. 
					Our script will wait for 2-3 mins and if in that time status won’t change than test will fail. */
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
