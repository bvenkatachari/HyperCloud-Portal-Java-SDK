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

package io.dchq.sdk.core.blueprints;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.Visibility;
import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.blueprint.BlueprintType;
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.BlueprintService;
import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Abedeen.
 * @updater Jagdeep Jain
 * @since 1.0
 */

/**
 * Blueprint: FindAll
 * App & Machine Blueprint
 *
 */


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class BlueprintFindAllServiceTest extends AbstractServiceTest {

	private BlueprintService blueprintService;
	private Blueprint bluePrint;
	private Blueprint bluePrintCreated;
	private int countBeforeCreate = 0, countAfterCreate = 0, countAfterDelete = 0;
	private boolean error;
    
    @Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{"Docker Blueprint", BlueprintType.DOCKER_COMPOSE, "2.0",	"LB:\n image: nginx:latest\n", Visibility.EDITABLE, "", EntitlementType.NONE, false},
				{"Docker Blueprint", BlueprintType.DOCKER_COMPOSE, "7.0", "LB:\n image: nginx:latest\n", Visibility.READABLE, "", EntitlementType.NONE, false},
				{"Docker Blueprint", BlueprintType.DOCKER_COMPOSE, "7.0", "LB:\n image: nginx:latest\n", Visibility.EDITABLE, "", null , false},
				{"Docker Blueprint", BlueprintType.DOCKER_COMPOSE, "7.0", " ", Visibility.EDITABLE, "", EntitlementType.NONE, false},
				{"Docker Blueprint", BlueprintType.DOCKER_COMPOSE, "7.0", "LB:\n image: nginx:latest\n", Visibility.READABLE, "", EntitlementType.NONE, false},
				// TODO yml file should not be null
				{"Docker Blueprint", BlueprintType.DOCKER_COMPOSE, "10.0", null, Visibility.EDITABLE, "", EntitlementType.NONE, true},
				{"Docker Blueprint", BlueprintType.DOCKER_COMPOSE, "7.0", "LB:\n image: nginx:latest\n", Visibility.READABLE, "", null, false},
				// TODO blueprint type should not be null
				{"Docker Blueprint", null, "10.0", "LB:\n image: nginx:latest\n", Visibility.EDITABLE, "", EntitlementType.NONE, true},
				// TODO version name should not be alphabet 
				//{"Docker-Blueprint", BlueprintType.DOCKER_COMPOSE, "ABC", "LB:\n image: nginx:latest\n", Visibility.READABLE, "", EntitlementType.NONE, true},
				
				// TODO blueprint name should not be null 
				{null, BlueprintType.DOCKER_COMPOSE, "7.0", "LB:\n image: nginx:latest\n", Visibility.EDITABLE, "", EntitlementType.NONE, false},
				
				//
				{"Docker Blueprint",    BlueprintType.DOCKER_COMPOSE, "7.0", "LB:\n image: nginx:latest\n", Visibility.READABLE, "", EntitlementType.NONE, false},
				{"@@DockerBlueprint@@", BlueprintType.DOCKER_COMPOSE, "7.0", "LB:\n image: nginx:latest\n", Visibility.EDITABLE, "", EntitlementType.NONE, false},
				//TODO yml file should not be blank
				//{"DockerBlueprint1234", BlueprintType.DOCKER_COMPOSE, "7.0", " ", Visibility.READABLE, "", EntitlementType.NONE, true},
				{"12345", BlueprintType.DOCKER_COMPOSE, "7.0", "LB:\n image: nginx:latest\n", Visibility.READABLE, "", EntitlementType.NONE, false},
				
				//{ "Docker Blueprint", null, " ", "LB:\n image: nginx:latest\n", Visibility.READABLE, "", EntitlementType.NONE, false },
				//{ "Docker Blueprint", null, "7.0", "LB:\n image: nginx:latest\n", null, "", EntitlementType.NONE, false },
				//{ null, null, "7.0", "LB:\n image: nginx:latest\n", Visibility.READABLE, "", EntitlementType.NONE, false },
				//{ null, BlueprintType.DOCKER_COMPOSE, null, "LB:\n image: nginx:latest\n", Visibility.READABLE, "", EntitlementType.NONE, false},
				//{ null, BlueprintType.DOCKER_COMPOSE, "7.0", null, Visibility.READABLE, "", EntitlementType.NONE, false },
				//{ null, BlueprintType.DOCKER_COMPOSE, "7.0", "LB:\n image: nginx:latest\n", null, "", EntitlementType.NONE, false},
				//{ null, BlueprintType.DOCKER_COMPOSE, "7.0", "LB:\n image: nginx:latest\n", Visibility.READABLE, "", null, false},
				//{ "Docker Blueprint", null, "7.0", "LB:\n image: nginx:latest\n", Visibility.READABLE, "", null, false},

		});
	}

    public BlueprintFindAllServiceTest(
    		String blueprintName, 
    		BlueprintType blueprintType, 
    		String version, 
    		String yaml, 
    		Visibility visible,
    		String modifiedBluePrint,
    		EntitlementType entitlementType,
    		boolean error
    		) 
    {
    	String suffix = RandomStringUtils.randomAlphabetic(3);
     	if(blueprintName!=null && !blueprintName.isEmpty())
     	{
     		blueprintName = blueprintName+""+suffix;
     	}
        this.bluePrint = new Blueprint().withName(blueprintName).withBlueprintType(blueprintType).withVersion(version).withVisibility(visible).withUserName(username);
        this.bluePrint.setYml(yaml);
        this.bluePrint.setEntitlementType(entitlementType);
        this.error = error;
    }    
    
    public int testBlueprintPosition(String id) {
		ResponseEntity<List<Blueprint>> response = blueprintService.findAll(0, 500);
        Assert.assertNotNull(response.getTotalElements());
        for (Message message : response.getMessages()) {
            logger.warn("Error [{}]" + message.getMessageText());
        }
        assertNotNull(response);
        assertNotNull(response.isErrors());
        assertThat(false, is(equals(response.isErrors())));
		int position = 0;
		if (id != null) {
			for (Blueprint obj : response.getResults()) {
				position++;
				if (obj.getId().equals(id)) {
					logger.info("Blueprint Object Matched in FindAll [{}]  at Position : [{}]", id, position);
					// Assert.assertEquals("Recently Created Blueprint is not at Position 1 :" + obj.getId(), 1, position);
				}
			}
		}
		logger.info(" Total Number of Blueprints :{}", response.getResults().size());
		return response.getResults().size();
    }
    
    @Before
    public void setUp() throws Exception {
        blueprintService = ServiceFactory.buildBlueprintService(rootUrl, username, password);
    }
    
    @Test
    public void testFind() throws Exception {
        // getting Count of profile before Create
		countBeforeCreate = testBlueprintPosition(null);
		logger.info("Create Bluepring [{}]", bluePrint.getName());
		ResponseEntity<Blueprint> response = blueprintService.create(bluePrint);
		
		for (Message message : response.getMessages()) {
            logger.warn("Error [{}] ", message.getMessageText());
        }
		if(!error)
		{
			assertNotNull(response);
			assertNotNull(response.isErrors());
			if (!response.isErrors() && response.getResults() != null) {
				this.bluePrintCreated = response.getResults();
				assertNotNull(response.getResults());
				assertNotNull(response.getResults().getId());
				Assert.assertNotNull(bluePrint.getName(), bluePrintCreated.getName());
				Assert.assertNotNull(bluePrint.getBlueprintType().toString(),
						bluePrintCreated.getBlueprintType().toString());
				Assert.assertNotNull(bluePrint.getVersion(), bluePrintCreated.getVersion());
				Assert.assertNotNull(bluePrint.getVisibility().toString(), bluePrintCreated.getVisibility().toString());
				Assert.assertNotNull(bluePrint.getUserName(), bluePrintCreated.getUserName());
				// get the count of total blueprints after recently created
				// Blueprint
				logger.info("FindAll Blueprint Position by Id [{}]", bluePrintCreated.getId());
				this.countAfterCreate = testBlueprintPosition(bluePrintCreated.getId());
				Assert.assertEquals(
						"Count of Find all Blueprints between before and after create does not have diffrence of 1 for UserId :"
								+ bluePrintCreated.getId(),
						countBeforeCreate + 1, countAfterCreate);
			}
		}
		else
		{
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
		}
    }

    @After
    public void cleanUp() {
		if (bluePrintCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = blueprintService.delete(bluePrintCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error blueprint deletion: [{}] ", message.getMessageText());
			}
			logger.info("Find All Blueprint After Delete User by Id {}", bluePrintCreated.getId());
			countAfterDelete = testBlueprintPosition(null);
			assertEquals("Count of Find all Blueprints between before and after delete are not same for blueprint :"
					+ bluePrintCreated.getId(), countBeforeCreate, countAfterDelete);
			for (Message message : response.getMessages()) {
				logger.warn("Error user deletion: [{}] ", message.getMessageText());
			}
		}
    }
}