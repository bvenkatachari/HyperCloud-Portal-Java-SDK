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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.springframework.util.StringUtils;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
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
 * Blueprint: Entitlement
 * App & Machine Blueprint
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class BlueprintEntitledServiceTest extends AbstractServiceTest {
			 
    private BlueprintService blueprintService, blueprintService2, blueprintService3;
    private Blueprint bluePrint;
    private boolean error;
    private Blueprint bluePrintCreated;
    
    public  BlueprintEntitledServiceTest (
    		String blueprintName, 
    		BlueprintType blueprintType,
    		String version, 
    		String description,
    		String externalLink, 
    		Visibility visible, 
    		String yaml, 
			Map<String, String> customMap,
			Boolean isInactive,
    		EntitlementType entitlementType,
    		String entitledUserId,
    		boolean isEntitlementTypeUser,
    		boolean success
            )
    {
		this.bluePrint = new Blueprint().withName(blueprintName).withBlueprintType(blueprintType).withVersion(version)
				.withDescription(description).withVisibility(visible).withUserName(username);
		this.bluePrint.setYml(yaml);
		this.bluePrint.setEntitlementType(entitlementType);
		this.bluePrint.setExternalLink(externalLink);
		this.bluePrint.setCustomizationsMap(customMap);
        this.error = success;
        
		if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
			UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
			List<UsernameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			this.bluePrint.setEntitledUsers(entiledUsers);
		} else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
			NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
			List<NameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			this.bluePrint.setEntitledUserGroups(entiledUsers);
		}
    }

    @Parameterized.Parameters
	public static Collection<Object[]> data() {

		return Arrays.asList(new Object[][] {

				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io",
						Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.OWNER, null,
						false, false },
				
				//{ "User Visiblity By PUBLIC", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io",
				//		Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.PUBLIC, null,
				//		false, false },
				
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io",
						Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, userId2,
						true, false },
				
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io",
						Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM,
						USER_GROUP, false, false },

				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.OWNER, null, false, false },
				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.READABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.OWNER, null, false, false },
				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.OWNER, " ", false, false },
				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, "6.0", "description", " ", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.OWNER, null, false, false },
				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.EDITABLE, " ", null, false, EntitlementType.OWNER, null, false, false },
				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, " ", "description", "https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.OWNER, null, false, false },

				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, userId2, true, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.READABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, userId2, true, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.EDITABLE, " ", null, false, EntitlementType.CUSTOM, userId2, true, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", " ", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, userId2, true, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", " ", "https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, userId2, true, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, " ", "description", "https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, userId2, true, false },

				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, USER_GROUP, false, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.READABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, USER_GROUP, false, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.EDITABLE, " ", null, false, EntitlementType.CUSTOM, USER_GROUP, false, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", " ", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, USER_GROUP, false, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", " ", "https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, USER_GROUP, false, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, " ", "description", "https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, USER_GROUP, false, false },

				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, " ", "description", "https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.OWNER, null, false, false },
				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, "6.0", null, "https://dchq.io", Visibility.READABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.OWNER, null, false, false },
				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, "6.0", "description", " ", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.OWNER, " ", false, false },
				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, "6.0", "description", " ", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.OWNER, null, false, false },
				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, "ABC", "description", "https://dchq.io", Visibility.READABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.OWNER, null, false, false },
				{ "User Visiblity By Owner", BlueprintType.DOCKER_COMPOSE, " ", "description", "https://dchq.io", Visibility.READABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.OWNER, null, false, false },

				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.READABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, userId2, true, false },
				{ "ABC", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.READABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, userId2, true, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", "https://dchq.io", Visibility.EDITABLE, " ", null, false, EntitlementType.CUSTOM, userId2, true, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, "6.0", "description", " ", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, null, userId2, true, false },
				{ "123", BlueprintType.DOCKER_COMPOSE, "6.0", " ", "https://dchq.io", Visibility.EDITABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, userId2, true, false },
				{ "User Visiblity By CUSTOM", BlueprintType.DOCKER_COMPOSE, " ", "description", "https://dchq.io", Visibility.READABLE, "LB:\n image: nginx:latest", null, false, EntitlementType.CUSTOM, userId2, true, false },


		});
	}

    @Before
    public void setUp() throws Exception {
        blueprintService = ServiceFactory.buildBlueprintService(rootUrl, username, password);
        blueprintService2 = ServiceFactory.buildBlueprintService(rootUrl, username2, password2);
        blueprintService3 = ServiceFactory.buildBlueprintService(rootUrl, username3, password3);
    }

    @Test
	public void testEntitledSearch() throws Exception {
		logger.info("Create Blueprint [{}]", bluePrint.getName());
		ResponseEntity<Blueprint> response = blueprintService.create(bluePrint);
		for (Message m : response.getMessages()) {
			logger.warn("[{}]", m.getMessageText());
		}
		if (response.getResults() != null) {
			bluePrintCreated = response.getResults();
		}
		if (!error) {
			if (bluePrintCreated.getEntitlementType().equals(EntitlementType.OWNER)) {
				ResponseEntity<List<Blueprint>> blueprintSearchResponseEntity1 = blueprintService2
						.search(bluePrint.getName(), 0, 1);
				for (Message message : blueprintSearchResponseEntity1.getMessages()) {
					logger.warn("Error while Search request  [{}] ", message.getMessageText());
				}
				assertNotNull(blueprintSearchResponseEntity1);
				assertNotNull(blueprintSearchResponseEntity1.isErrors());
				// TODO: add tests for testing error message
				assertNotNull(blueprintSearchResponseEntity1.getResults());
				assertEquals(0, blueprintSearchResponseEntity1.getResults().size());
			}
			if (bluePrintCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				ResponseEntity<List<Blueprint>> blueprintSearchResponseEntity = blueprintService2
						.searchEntitled(bluePrint.getName(), 0, 1);
				for (Message message : blueprintSearchResponseEntity.getMessages()) {
					logger.warn("Error while Search request  [{}] ", message.getMessageText());
				}
				assertNotNull(blueprintSearchResponseEntity);
				assertNotNull(blueprintSearchResponseEntity.isErrors());
				// TODO: add tests for testing error message
				assertNotNull(blueprintSearchResponseEntity.getResults());
				assertEquals(1, blueprintSearchResponseEntity.getResults().size());
			}
			if (bluePrintCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
				ResponseEntity<List<Blueprint>> blueprintSearchResponseEntity = blueprintService2
						.searchEntitled(bluePrintCreated.getName(), 0, 1);
				for (Message message : blueprintSearchResponseEntity.getMessages()) {
					logger.warn("Error while Search request  [{}] ", message.getMessageText());
				}
				assertNotNull(blueprintSearchResponseEntity);
				assertNotNull(blueprintSearchResponseEntity.isErrors());
				// TODO: add tests for testing error message
				assertNotNull(blueprintSearchResponseEntity.getResults());
				assertEquals(1, blueprintSearchResponseEntity.getResults().size());
			}

			if (bluePrintCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				ResponseEntity<List<Blueprint>> blueprintSearchResponseEntity = blueprintService3
						.searchEntitled(bluePrint.getName(), 0, 1);
				for (Message message : blueprintSearchResponseEntity.getMessages()) {
					logger.warn("Error while Search request  [{}] ", message.getMessageText());
				}
				assertNotNull(blueprintSearchResponseEntity);
				assertNotNull(blueprintSearchResponseEntity.isErrors());
				// TODO: add tests for testing error message
				assertNotNull(blueprintSearchResponseEntity.getResults());
				assertEquals(0, blueprintSearchResponseEntity.getResults().size());
			}
		}
    }

	@Test
	public void testEntitledFindById() throws Exception {
		logger.info("Create Blueprint [{}]", bluePrint.getName());
		ResponseEntity<Blueprint> response = blueprintService.create(bluePrint);
		for (Message m : response.getMessages()) {
			logger.warn("[{}]", m.getMessageText());
		}
		if (response.getResults() != null) {
			bluePrintCreated = response.getResults();
		}
		if (!error) {
			if (bluePrintCreated.getEntitlementType().equals(EntitlementType.OWNER)) {
				ResponseEntity<Blueprint> findbyIdResponse = blueprintService2.findById(bluePrint.getId());
				for (Message message : findbyIdResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
				assertNotNull(findbyIdResponse);
				assertNotNull(findbyIdResponse.isErrors());
				assertEquals(findbyIdResponse.getResults(), null);
			}
			if (bluePrintCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				ResponseEntity<Blueprint> findbyIdResponse = blueprintService2.findById(bluePrintCreated.getId());
				for (Message message : findbyIdResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
				assertNotNull(findbyIdResponse.getResults());
				assertNotNull(findbyIdResponse.getResults().getId());
				assertEquals(bluePrintCreated.getId(), findbyIdResponse.getResults().getId());
			}
			if (bluePrintCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
				ResponseEntity<Blueprint> findbyIdResponse = blueprintService2.findById(bluePrintCreated.getId());
				for (Message message : findbyIdResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
				assertNotNull(findbyIdResponse.getResults());
				assertNotNull(findbyIdResponse.getResults().getId());
				assertEquals(bluePrintCreated.getId(), findbyIdResponse.getResults().getId());
			}
			if (bluePrintCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				ResponseEntity<Blueprint> findbyIdResponse = blueprintService3.findById(bluePrintCreated.getId());
				for (Message message : findbyIdResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
				assertNotNull(findbyIdResponse);
				assertNotNull(findbyIdResponse.isErrors());
				assertEquals(findbyIdResponse.getResults(), null);
			}
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