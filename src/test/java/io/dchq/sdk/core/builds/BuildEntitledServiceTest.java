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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.springframework.util.StringUtils;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.build.Build;
import com.dchq.schema.beans.one.build.BuildType;
import com.dchq.schema.beans.one.security.EntitlementType;

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
public class BuildEntitledServiceTest extends AbstractServiceTest {

    private BuildService buildService;
    private BuildService buildService2;

    @org.junit.Before
    public void setUp() throws Exception {
        buildService = ServiceFactory.buildBuildService(rootUrl, cloudadminusername, cloudadminpassword);
        buildService2 = ServiceFactory.buildBuildService(rootUrl, username2, password2);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","2c9180875e9da16c015e9dd2d2ca008c","1679/sam", "latestmine","2c9180875e987035015e993d8b860119",EntitlementType.OWNER, false, null, true},
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","2c9180875e9da16c015e9dd2d2ca008c","1679/sam", "latestmine","2c9180875e987035015e993d8b860119",EntitlementType.PUBLIC, false, null, true},
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","2c9180875e9da16c015e9dd2d2ca008c","1679/sam", "latestmine","2c9180875e987035015e993d8b860119",EntitlementType.CUSTOM, true, userId2,true},
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","2c9180875e9da16c015e9dd2d2ca008c","1679/sam", "latestmine","2c9180875e987035015e993d8b860119",EntitlementType.CUSTOM, false, USER_GROUP,true},
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","2c9180875e9da16c015e9dd2d2ca008c","1679/sam", "","2c9180875e987035015e993d8b860119",EntitlementType.OWNER, false, null,false},
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","2c9180875e9da16c015e9dd2d2ca008c","1679/sam", "latestmine","",EntitlementType.OWNER, false, null,false},
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","2c9180875e9da16c015e9dd2d2ca008c","", "latestmine","2c9180875e987035015e993d8b860119",EntitlementType.OWNER, false, null,false},
                {"TestImage", BuildType.GITHUB_PUBLIC_REPO,"","2c9180875e9da16c015e9dd2d2ca008c","1679/sam", "latestmine","2c9180875e987035015e993d8b860119",EntitlementType.OWNER, false, null,false}
                
        });
    }

    private Build build;
    private boolean success;
    private Build buildCreated;
   



    public BuildEntitledServiceTest(String imageName, BuildType buildType,String gitURL,String clusterId,String pustToRepository,
    		String tag,String registryAccountId, EntitlementType entitlementType, boolean isEntitlementTypeUser, String entitledUserId, boolean success)  throws Exception {
     
        this.build = new Build().withBuildType(buildType);
        build.setCluster(clusterId);
        build.setName(imageName);
        build.setTag(tag);
        build.setGitCloneUrl(gitURL);
        build.setRepository(pustToRepository);
        NameEntityBase neb = new NameEntityBase();
        neb.setId(registryAccountId);
        build.setRegistryAccount(neb);
        
        if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
			UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
			List<UsernameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			build.setEntitledUsers(entiledUsers);
		} else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
			NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
			List<NameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			build.setEntitledUserGroups(entiledUsers);
		}
        
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
	                
	                
	            if (this.buildCreated.getEntitlementType().equals(EntitlementType.OWNER)) {
					ResponseEntity<List<Build>> subnetSearchResponseEntity = buildService2.search(this.buildCreated.getName(), 0,1);
					for (Message message : subnetSearchResponseEntity.getMessages()) {
						logger.warn("Error while Search request  [{}] ", message.getMessageText());
					}
					assertNotNull(subnetSearchResponseEntity);
					assertNotNull(subnetSearchResponseEntity.isErrors());
					assertNotNull(subnetSearchResponseEntity.getResults());
					assertEquals(0, subnetSearchResponseEntity.getResults().size());
				}
				if (this.buildCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
					ResponseEntity<List<Build>> subnetSearchResponseEntity = buildService2.search(this.buildCreated.getName(), 0,1);
					for (Message message : subnetSearchResponseEntity.getMessages()) {
						logger.warn("Error while Search request  [{}] ", message.getMessageText());
					}
					assertNotNull(subnetSearchResponseEntity);
					assertNotNull(subnetSearchResponseEntity.isErrors());
					assertNotNull(subnetSearchResponseEntity.getResults());
					assertEquals(1, subnetSearchResponseEntity.getResults().size());
				}
				if (this.buildCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
					ResponseEntity<List<Build>> subnetSearchResponseEntity = buildService2.search(this.buildCreated.getName(), 0,1);
					for (Message message : subnetSearchResponseEntity.getMessages()) {
						logger.warn("Error while Search request  [{}] ", message.getMessageText());
					}
					assertNotNull(subnetSearchResponseEntity);
					assertNotNull(subnetSearchResponseEntity.isErrors());
					assertNotNull(subnetSearchResponseEntity.getResults());
					assertEquals(1, subnetSearchResponseEntity.getResults().size());
				}
	
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

