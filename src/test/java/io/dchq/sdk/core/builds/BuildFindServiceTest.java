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
import com.dchq.schema.beans.one.build.BuildType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.BuildService;
import io.dchq.sdk.core.ServiceFactory;

/**
*
* @author Santosh Kumar.
* @since 1.0
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class BuildFindServiceTest extends AbstractServiceTest {

    private BuildService buildService;

    @org.junit.Before
    public void setUp() throws Exception {
        buildService = ServiceFactory.buildBuildService(rootUrl, cloudadminusername, cloudadminpassword);
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
   



    public BuildFindServiceTest(String imageName, BuildType buildType,String gitURL,String clusterId,String pustToRepository,String tag,String registryAccountId, boolean success)  throws Exception {
     
        this.build = new Build()
                .withBuildType(buildType);
        build.setCluster(clusterId);
        build.setName(imageName);
        build.setTag(tag);
        build.setGitCloneUrl(gitURL);
        build.setRepository(pustToRepository);
        NameEntityBase neb = new NameEntityBase();
        neb.setId(registryAccountId);
        build.setRegistryAccount(neb);
        this.success = success;


    }

    
    @org.junit.Test
    public void testCreate() throws Exception {

        ResponseEntity<Build> response = buildService.create(build);

      if (success) {
	        String errorMessage = "";
	        for (Message message : response.getMessages()) {
	            logger.warn("Error while Create request  [{}] ", message.getMessageText());
	            errorMessage += ("Error while Create request  [{}] " + message.getMessageText());
	        }
	        
	        Assert.assertNotNull(errorMessage,response.getResults());
	
	        if (response.getResults()!=null) {
	
	            assertNotNull(response.getResults());
	            assertNotNull(response.getResults().getId());
	
	            buildCreated = response.getResults();
	                
	                
                response = buildService.findById(this.buildCreated.getId());

				for (Message message : response.getMessages()) {
					logger.warn("Error while find Build by Id request  [{}] ", message.getMessageText());
				}
				
				assertNotNull(response);
				assertEquals(false,response.isErrors());
				assertNotNull(response.getResults().getId());
				assertEquals(this.buildCreated.getRepository(), response.getResults().getRepository());
				assertEquals(this.buildCreated.getGitCloneUrl(), response.getResults().getGitCloneUrl());
				assertEquals(this.buildCreated.getTag(), response.getResults().getTag());
	
	        }
        
       } else {
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
		}
    }
    
	
	
    @After
    public void cleanUp() throws Exception  {
        logger.info("cleaning up...");

        if(buildCreated!=null) 
        	buildService.delete(buildCreated.getId());


    }

}

