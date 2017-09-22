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
package io.dchq.sdk.core.builds;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.build.Build;
import com.dchq.schema.beans.one.build.BuildTask;
import com.dchq.schema.beans.one.build.BuildType;
import com.dchq.schema.beans.one.provider.DataCenter;
import com.dchq.schema.beans.one.provider.DockerServer;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.BuildService;
import io.dchq.sdk.core.DataCenterService;
import io.dchq.sdk.core.DockerServerService;
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
public class BuildCreateAndDeployServiceTest extends AbstractServiceTest {

    private BuildService buildService;
    DataCenterService dataCenterService;
    DockerServerService dockerServerService;
    DockerServer dockerServerCreated;
    long endTime;
    
    

    @org.junit.Before
    public void setUp() throws Exception {
        buildService = ServiceFactory.buildBuildService(rootUrl, cloudadminusername, cloudadminpassword);
        dataCenterService = ServiceFactory.buildDataCenterService(rootUrl, cloudadminusername, cloudadminpassword);
        dockerServerService = ServiceFactory.buildDockerServerService(rootUrl, cloudadminusername, cloudadminpassword);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","2c9180875e9da16c015e9dd2d2ca008c","1679/sam", "latestmine","2c9180875e987035015e993d8b860119",true},
                {"TestImage1", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","2c9180875e9da16c015e9dd2d2ca008c","1679/sam", "latestmine","2c9180875e987035015e993d8b860119",true},
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","2c9180875e9da16c015e9dd2d2ca008c","1679/sam", "","2c9180875e987035015e993d8b860119",false},
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","2c9180875e9da16c015e9dd2d2ca008c","1679/sam", "latestmine","",false},
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","2c9180875e9da16c015e9dd2d2ca008c","", "latestmine","2c9180875e987035015e993d8b860119",false},
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"","2c9180875e9da16c015e9dd2d2ca008c","1679/sam", "latestmine","2c9180875e987035015e993d8b860119",false}
                
        });
    }

    private Build build;
    private boolean success;
    private Build buildCreated;
   



    public BuildCreateAndDeployServiceTest(String imageName, BuildType buildType,String gitURL,String clusterId,String pustToRepository,String tag,String registryAccountId, boolean success)  throws Exception {
     
        this.build = new Build()
                .withBuildType(buildType);
        this.build.setCluster(clusterId);

        build.setTag(tag);
        build.setGitCloneUrl(gitURL);
        build.setRepository(pustToRepository);
        NameEntityBase neb = new NameEntityBase();
        neb.setId(registryAccountId);
        build.setRegistryAccount(neb);
        this.success = success;


    	endTime = System.currentTimeMillis() + (60 * 60 * 90); // this is for 3 mints
    }

    
    @org.junit.Test
    public void testCreate() throws Exception {
    	
    	//Docker Server required to deploy Image
    	createDockerServer(this.build.getCluster());

        ResponseEntity<Build> response = buildService.create(build);

      if (success) {
	        String errorMessage = "";
	        for (Message message : response.getMessages()) {
	            logger.warn("Error while Create request  [{}] ", message.getMessageText());
	            errorMessage += ("Error while Create request  [{}] " + message.getMessageText());
	        }
	        assertNotNull(response.getResults());
	        assertNotNull(response.getResults().getId());
	        Assert.assertNotNull(errorMessage,response.getResults());
	
	        if (response.getResults()!=null) {
	
	            assertNotNull(response.getResults());
	            assertNotNull(response.getResults().getId());
	
	            buildCreated = response.getResults();
	            ResponseEntity<BuildTask> responseTask  = buildService.buildNow(buildCreated.getId());
	            BuildTask buildTask=getTask(responseTask);
	
	                do{
	                	try {
	                        Thread.sleep(20000);
	                    } catch (Exception e) {
	                        logger.warn("Error @ Wait [{}] ", e.getMessage());
	                    }
	                    responseTask  = buildService.findBuildTaskById(buildTask.getId());
	                    buildTask=getTask(responseTask);
	                    
	                }while(buildTask.getBuildTaskStatus().name().equals("PROCESSING")&& (System.currentTimeMillis() < endTime));
	                
	                
	                assertEquals("SUCCESS", buildTask.getBuildTaskStatus().name());
	
	        }
        
       } else {
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
		}
    }
    
    public void createDockerServer(String clusterId){
    	
    	ResponseEntity<DataCenter> response = dataCenterService.findById(clusterId);
    	
		DockerServer server = new DockerServer().withDatacenter(response.getResults()).withName("Docker_VM")
				.withInactive(Boolean.FALSE).withImageId("C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub14HFT_DCHQ_Docker_Swarm.vhdx").withSize(1)
				.withEndpoint("2c9180865d312fc4015d3160f518008e").withHardwareId("cpu=1,memory=2GB,disk=20GB,generation=1").withNetworkId("Compute vmSwitch");
		server.setGroup("Docker_VM");
		server.setSkipAgentInstall("true");
		
		logger.info("Create Machine with Name [{}]", server.getName());
		ResponseEntity<DockerServer> serverResponse = dockerServerService.create(server);
		
		maxWaitTime = 300000;
		dockerServerCreated = serverResponse.getResults();
		String serverStatus = dockerServerCreated.getDockerServerStatus().name();
		
		while(serverStatus.equals("PROVISIONING") && (System.currentTimeMillis() < endTime)){	
			// Wait for some time until state changed from PROVISIONING to CONNECTED/PROVISIONED
			wait(10000); //wait for 10 seconds
			dockerServerCreated = dockerServerService.findById(dockerServerCreated.getId()).getResults();
			serverStatus = dockerServerCreated.getDockerServerStatus().name();
			 logger.info("Current Serverstatus   [{}] ", serverStatus);
	
		}
		
		while(serverStatus.equals("PROVISIONED") && (System.currentTimeMillis() < endTime)){	
			/*Noticed, sometimes system takes time to change the status from ‘Provisioned’ to ‘Connected’ 
			so we don’t have any exact number to wait. 
			Our script will wait for 2-3 mins and if in that time status won’t change than test will fail. */
			wait(10000); //wait for 10 seconds
			dockerServerCreated = dockerServerService.findById(dockerServerCreated.getId()).getResults();
			serverStatus = dockerServerCreated.getDockerServerStatus().name();
			 logger.info("Current Serverstatus   [{}] ", serverStatus);
	
		}
    }
    
	public BuildTask getTask(ResponseEntity<BuildTask> responseTask) {
	
	    String errorMessage = "";
	    BuildTask buildTask =null;
	    for (Message message : responseTask.getMessages()) {
	        logger.warn("Error while Running Build Task request  [{}] ", message.getMessageText());
	        errorMessage += ("Error while Running Build Task request  " + message.getMessageText());
	    }
	    if (responseTask.getResults() != null) {
	        buildTask = responseTask.getResults();
	        Assert.assertFalse("Machine Creation Replied with Error." + errorMessage, responseTask.isErrors());
	
	
	    }
	    return buildTask;
	}
	
    @After
    public void cleanUp() throws Exception  {
        logger.info("cleaning up...");

        if(buildCreated!=null) {
        	buildService.delete(buildCreated.getId());
        }
        
        if (dockerServerCreated != null) {
            logger.info("Deleting Machine ");
            dockerServerService.delete(dockerServerCreated.getId(), true);

        }


    }

}

