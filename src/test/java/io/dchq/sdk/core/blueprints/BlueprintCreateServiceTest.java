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
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
 * Blueprint: Create
 * App & Machine Blueprint
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class BlueprintCreateServiceTest extends AbstractServiceTest {

    private BlueprintService blueprintService;
    private Blueprint bluePrint;
    private boolean error;
    private Blueprint bluePrintCreated;
    private String errorMessage;
    
    public  BlueprintCreateServiceTest (
    		String blueprintName, 
    		BlueprintType blueprintType,
    		String version, 
    		String description,
    		String externalLink, 
    		Visibility visible, 
    		String yaml, 
			Map<String, String> customMap,
    		EntitlementType entitlementType,
    		/*
    		String errorMessage,                          
    		Boolean isInactive, 
    		String username, 
    		String customText,
            String leaseTime, 
            String shortDescription, 
            String tags,
            Boolean editable,
            */
    		boolean success
            )
    {
        this.bluePrint = new Blueprint().withName(blueprintName).withBlueprintType(blueprintType).withVersion(version).withDescription(description).withVisibility(visible).withUserName(username);
        this.bluePrint.setYml(yaml);
        this.bluePrint.setEntitlementType(entitlementType);
        this.bluePrint.setExternalLink(externalLink);
        this.bluePrint.setCustomizationsMap(customMap);
        /*
        this.bluePrint.setInactive(isInactive);
        this.errorMessage = errorMessage;
        this.bluePrint.setCustomizationsText(customText);
        this.bluePrint.setLeaseTime(leaseTime);
        this.bluePrint.setShortDescription(shortDescription);
        this.bluePrint.setTags(tags);
        this.bluePrint.setEditable(editable);
        */
        this.error = success;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Map<String, String> customizationsMap = new HashMap<String, String>();
        //customizationsMap.put("name", "Bernie");
        
        return Arrays.asList(new Object[][]{

<<<<<<< HEAD
				{ "App & Machines Blueprints Test", BlueprintType.DOCKER_COMPOSE, null, "description",
=======
				{ "App & Machines Blueprints Test", BlueprintType.DOCKER_COMPOSE, "6.0", "description",
>>>>>>> 48144376bd3abf6f0bdb8f102a60b2cd60a10a08
						"https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, EntitlementType.NONE,
						false },
				{ "Blueprint", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io",
						Visibility.EDITABLE, "LB:\n image: nginx:latest", null, EntitlementType.NONE, false },
				{ "App & Machines Blueprints Test", BlueprintType.DOCKER_COMPOSE, "6.0", "description",
						"https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, EntitlementType.NONE,
						false },
				{ "Blueprint", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io",
						Visibility.READABLE, "LB:\n image: nginx:latest", null, EntitlementType.NONE, false },
				{ "App & Machines Blueprints Test", BlueprintType.DOCKER_COMPOSE, "AB", "description",
						"https://dchq.io", Visibility.READABLE, "LB:\n image: nginx:latest", null, EntitlementType.NONE,
						false },
				{ "App & Machines Blueprints Test", BlueprintType.DOCKER_COMPOSE, "6.0", "description",
						"https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, EntitlementType.NONE,
						false },
				{ "App & Machines Blueprints Test", BlueprintType.DOCKER_COMPOSE, "6.0", "description",
						"https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, EntitlementType.NONE,
						false },
				{ "App & Machines Blueprints Test", BlueprintType.DOCKER_COMPOSE, "6.0", "description",
						"https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, EntitlementType.NONE,
						false },
				{ "App & Machines Blueprints Test", BlueprintType.DOCKER_COMPOSE, "6.0", "description",
						"https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, EntitlementType.NONE,
						false },
				/*
				{ "Blueprint-version", BlueprintType.DOCKER_COMPOSE, null, "description",
						"https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, EntitlementType.NONE,
						false },*/

        });
    }

    @Before
    public void setUp() throws Exception {
        blueprintService = ServiceFactory.buildBlueprintService(rootUrl, username, password);
    }

    @Test
    public void testCreate() throws Exception {
		logger.info("Create Blueprint [{}] "
				+ "having details as follows:\n [{}]\n [{}]\n [{}]\n [{}]\n [{}]\n [{}]\n [{}]", 
				bluePrint.getName(),
				bluePrint.getBlueprintType(), 
				bluePrint.getVersion(), 
				bluePrint.getDescription(),
				bluePrint.getExternalLink(), 
				bluePrint.getVisibility(), 
				bluePrint.getYml(),
				bluePrint.getCustomizationsMap(), 
				bluePrint.getEntitlementType());
	
	  	ResponseEntity<Blueprint> response = blueprintService.create(bluePrint);    
        for (Message m : response.getMessages()) {
            logger.warn("[{}]", m.getMessageText());
        }
        if (response.getResults() != null) {
        	bluePrintCreated = response.getResults();
        }
        /* check response:
         * 1. is not null
         * 2. has no errors
         * 3. has user entity with ID
         * 4. all data sent
         */
        assertNotNull(response);
        assertNotNull(response.isErrors());
        if (!error) {
            assertNotNull(response.getResults());
            assertNotNull(response.getResults().getId());
            Assert.assertEquals("Empty Blueprint Name ", bluePrint.getName(), bluePrintCreated.getName());
            Assert.assertEquals("Image ", bluePrint.getImages(), bluePrintCreated.getImages());
            Assert.assertEquals("YAML Error ", bluePrint.getYml(), bluePrintCreated.getYml());
            if (isNullOrEmpty(bluePrint.getTenantPk())  ) {
				Assert.assertEquals(" Tenant ", "402881834d9ee4d1014d9ee5d73f0010", bluePrintCreated.getTenantPk());
			} else {
				Assert.assertEquals(" Tenant ", bluePrint.getTenantPk(), bluePrintCreated.getTenantPk());
			}
            
			if (bluePrint.getOwnerId() != null) {
				Assert.assertEquals(" Tenant ", bluePrint.getOwnerId(), bluePrintCreated.getOwnerId());
			} else {
				Assert.assertNull(bluePrintCreated.getOwnerId());				
			}
			
            Assert.assertEquals("Owner ID ", bluePrint.getOwnerId(), bluePrintCreated.getOwnerId());
            Assert.assertEquals("Total Stars", new Integer(1), bluePrintCreated.getTotalStars());
            Assert.assertEquals("Total Run", new Integer(0), bluePrintCreated.getTotalRun());
            Assert.assertEquals("User Name ", userId, bluePrintCreated.getUserName());
            
			if (isNullOrEmpty(bluePrint.getVersion())) {
				Assert.assertEquals(" Version ", "1.0", bluePrintCreated.getVersion());
			} else {
				Assert.assertEquals(" Version  ", bluePrint.getVersion(), bluePrintCreated.getVersion());
			}

			if (isNullOrEmpty(bluePrint.getVisibility())) {
				Assert.assertEquals(" Visibility  ", Visibility.READABLE, bluePrintCreated.getVisibility());
			} else {
				Assert.assertEquals(" Visibility  ", bluePrint.getVisibility(), bluePrintCreated.getVisibility());
			}

			if (isNullOrEmpty(bluePrint.getDeleted())) {
				Assert.assertEquals(" Deleted ", false, bluePrintCreated.getDeleted());
			} else {
				Assert.assertEquals(" Deleted ", bluePrint.getDeleted(), bluePrintCreated.getDeleted());
			}

			if (isNullOrEmpty(bluePrint.getEntitlementType())) {
				Assert.assertEquals(" EntitlementType ", EntitlementType.ALL_BLUEPRINTS.OWNER, bluePrintCreated.getEntitlementType());
			} else {
				Assert.assertEquals(" EntitlementType ", bluePrint.getEntitlementType(), bluePrintCreated.getEntitlementType());
			}

			if (isNullOrEmpty(bluePrint.getInactive())) {
				Assert.assertEquals(" EntitlementType ", false, bluePrintCreated.getInactive());
			} else {
				Assert.assertEquals(" EntitlementType ", bluePrint.getInactive(), bluePrintCreated.getInactive());
			}

			if (isNullOrEmpty(bluePrint.getEditable())) {
				Assert.assertEquals(" Editable ", true, bluePrintCreated.getEditable());
			} else {
				Assert.assertEquals(" Editable ", bluePrint.getEditable(), bluePrintCreated.getEditable());
			}

            Assert.assertEquals(" CustomizationsText ", bluePrint.getCustomizationsText(), bluePrintCreated.getCustomizationsText());
            Assert.assertEquals(" ExternalLink", bluePrint.getExternalLink(), bluePrintCreated.getExternalLink());
            Assert.assertEquals(" LeaseTime", bluePrint.getLeaseTime(), bluePrintCreated.getLeaseTime());
            Assert.assertEquals(" ShortDescription", bluePrint.getShortDescription(), bluePrintCreated.getShortDescription());
            Assert.assertEquals(" Tags", bluePrint.getTags(), bluePrintCreated.getTags());
            Assert.assertEquals(" BlueprintType", bluePrint.getBlueprintType(), bluePrintCreated.getBlueprintType());
            Assert.assertEquals(" CustomizationsMap", bluePrint.getCustomizationsMap(), bluePrintCreated.getCustomizationsMap());
            Assert.assertEquals(" Datacenter", bluePrint.getDatacenter(), bluePrintCreated.getDatacenter());
            Assert.assertEquals(" Gist", bluePrint.getGist(), bluePrintCreated.getGist());
            Assert.assertEquals(" Image", bluePrint.getImage(), bluePrintCreated.getImage());
            Assert.assertEquals(" Images", bluePrint.getImages(), bluePrintCreated.getImages());
            Assert.assertEquals(" ServiceTypes", bluePrint.getServiceTypes(), bluePrintCreated.getServiceTypes());
            Assert.assertEquals(" UserStarred", bluePrint.getUserStarred(), bluePrintCreated.getUserStarred());
            Assert.assertEquals(" DynamicAttributes", bluePrint.getDynamicAttributes(), bluePrintCreated.getDynamicAttributes());
            Assert.assertEquals(" EntitledGroupsPks", bluePrint.getEntitledGroupsPks(), bluePrintCreated.getEntitledGroupsPks());
            Assert.assertEquals(" EntitledUserGroups", bluePrint.getEntitledUserGroups(), bluePrintCreated.getEntitledUserGroups());
            Assert.assertEquals(" EntitledUsers", bluePrint.getEntitledUsers(), bluePrintCreated.getEntitledUsers());
            Assert.assertEquals(" Blueprint name", bluePrint.getName(), bluePrintCreated.getName());
            Assert.assertEquals(" Blueprint type", bluePrint.getBlueprintType().toString(), bluePrintCreated.getBlueprintType().toString());
            Assert.assertEquals(" Blueprint version", bluePrint.getVersion(), bluePrintCreated.getVersion());
            Assert.assertEquals(" Blueprint visiblity",bluePrint.getVisibility().toString(), bluePrintCreated.getVisibility().toString());
            // Assert.assertEquals(" Blueprint created by user", bluePrint.getUserName(), bluePrintCreated.getUserName());
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
        }
    }
}