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

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.common.TerminationProtection;
import com.dchq.schema.beans.one.inbox.EntityType;
import com.dchq.schema.beans.one.inbox.MessageResolution;
import com.dchq.schema.beans.one.inbox.MessageStatus;
import com.dchq.schema.beans.one.provider.DockerServer;

import io.dchq.sdk.core.MessageService;
import io.dchq.sdk.core.ServiceFactory;

/**
 * Santosh Kumar
 * 
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerServerTerminationServiceTest extends DockerServerTest {

	private MessageService messageService;
	
    @org.junit.Before
    public void setUp() {
        dockerServerService = ServiceFactory.buildDockerServerService(rootUrl, cloudadminusername, cloudadminpassword);
        messageService = ServiceFactory.buildMessageService(rootUrl1, cloudadminusername, cloudadminpassword);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
        	//CentOS7HFT
        	{"vmterminationtest", Boolean.FALSE, "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub1604HFT_DCHQ_Docker.vhdx", "Compute vmSwitch", 1, "2c9180865d312fc4015d3160f518008e", 300000, false, false}
        });
    }


    public DockerServerTerminationServiceTest(String serverName, Boolean activeFlag, String hardwareID, String image, String networkId, int size, String endpoint, int tinout, boolean success, boolean installAgent) {
    	String postfix = RandomStringUtils.randomAlphabetic(3);
    	
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
    	
    	this.dockerServer.setTerminationProtection(TerminationProtection.ENABLED);
    	
        maxWaitTime = tinout;
        this.createError = success;
    }


    @Test
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

			}
			
			dockerServerService.delete(dockerServerCreated.getId(), true);
			
			@SuppressWarnings("unchecked")
			ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>> list = (ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>>) messageService
					.find("open",
							new ParameterizedTypeReference<ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>>>() {
							});

			
			for (com.dchq.schema.beans.one.inbox.Message message : list.getResults()) {
				if(message.getEntityType().equals(EntityType.DOCKER_SERVER_DESTROY)){
					message.setMessageStatus(MessageStatus.READ);
					message.setMessageResolution(MessageResolution.APPROVED);
					ResponseEntity<com.dchq.schema.beans.one.inbox.Message> re = messageService.update(message);
					logger.info("Message approved {}", re.getResults().getBody());
					break;
				}
			}
			
			//Wait for 60 seconds for Docker server to be deleted completely
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			response = dockerServerService.findById(dockerServerCreated.getId());
			
			assertNotNull(response);
			assertNotNull(response.isErrors());
			Assert.assertTrue(response.getResults().getInactive());
			Assert.assertTrue(response.getResults().getDeleted());
			
			
		} else {

			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
		}

	}

   

    @After
    public void cleanUp() {/*
    	
    	logger.info("cleaning up...");
    	//Docker server is should be deleted by approving message. In case it's not deleted then retry
        if (dockerServerCreated != null) {
            logger.info("Deleting Machine ");
            dockerServerService.delete(dockerServerCreated.getId(), true);
            validateProvision(dockerServerCreated, "DESTROYING");

        }
    */}


}
