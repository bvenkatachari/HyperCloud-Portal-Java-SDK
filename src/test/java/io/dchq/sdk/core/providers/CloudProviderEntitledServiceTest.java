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
package io.dchq.sdk.core.providers;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
import com.dchq.schema.beans.one.blueprint.AccountType;
import com.dchq.schema.beans.one.blueprint.RegistryAccount;
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.RegistryAccountService;
import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Abedeen.
 * @updater Jagdeep Jain
 * @since 1.0
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class CloudProviderEntitledServiceTest extends AbstractServiceTest {
	
    private RegistryAccountService registryAccountService, registryAccountService2, registryAccountService3;
    private RegistryAccount registryAccount;
    private boolean error;
    private RegistryAccount registryAccountCreated;
 
    public CloudProviderEntitledServiceTest (
    		AccountType accountType,
    		String accountName,
    		String testUsername,
    		Boolean isActive,
    		String password,    	
    		String domainName,
    		String vmDestination,
    		String template,
    		EntitlementType entitlementType, 
    		boolean isEntitlementTypeUser, 
    		String entitledUserId,
    		boolean success
    		) 
    {
		this.registryAccount = new RegistryAccount().withName(accountName).withUsername(testUsername).withPassword(password).withAccountType(accountType).withInactive(isActive);
		this.registryAccount.setEntitlementType(entitlementType);

		if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
			UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
			List<UsernameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			this.registryAccount.setEntitledUsers(entiledUsers);
		} else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
			NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
			List<NameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			this.registryAccount.setEntitledUserGroups(entiledUsers);
		}
		
        this.error = success;
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{

				{ AccountType.HYPER_V, "Microsoft 1 Hyper-V testAccount", "dchqinc", false, "password",
						"http://dchq.co.in", "hardwareId", "templateId", EntitlementType.OWNER, true, userId2, false },

				{ AccountType.HYPER_V, "Microsoft 2 Hyper-V testAccount", "dchqinc", false, "password",
						"http://dchq.co.in", "hardwareId", "templateId", EntitlementType.PUBLIC, true, userId2, false },

				{ AccountType.HYPER_V, "Microsoft 3 Hyper-V testAccount", "dchqinc", false, "password",
						"http://dchq.co.in", "hardwareId", "templateId", EntitlementType.CUSTOM, true, userId2, false },

				{ AccountType.HYPER_V, "Microsoft 4 Hyper-V testAccount", "dchqinc", false, "password",
						"http://dchq.co.in", "hardwareId", "templateId", EntitlementType.CUSTOM, false, USER_GROUP,
						false }
        });
    }

    @Before
    public void setUp() throws Exception {
        registryAccountService = ServiceFactory.buildRegistryAccountService(rootUrl, username, password);
        registryAccountService2 = ServiceFactory.buildRegistryAccountService(rootUrl, username2, password2);
        registryAccountService3 = ServiceFactory.buildRegistryAccountService(rootUrl, username3, password3);
    }
    
	@Test
	public void testEntitledSearch() {
		ResponseEntity<RegistryAccount> response = registryAccountService.create(registryAccount);
		logger.info("Create Registry Account with Name [{}]", registryAccount.getName());

		for (Message m : response.getMessages()) {
			logger.warn("[{}]", m.getMessageText());
		}
		if (response.getResults() != null) {
			registryAccountCreated = response.getResults();
		}
		if (!error) {
			if (registryAccountCreated.getEntitlementType().equals(EntitlementType.OWNER)) {
				ResponseEntity<List<RegistryAccount>> registryAccountSearchResponseEntity1 = registryAccountService2
						.search(registryAccount.getName(), 0, 1);
				for (Message message : registryAccountSearchResponseEntity1.getMessages()) {
					logger.warn("Error while Search request  [{}] ", message.getMessageText());
				}
				assertNotNull(registryAccountSearchResponseEntity1);
				assertNotNull(registryAccountSearchResponseEntity1.isErrors());
				// TODO: add tests for testing error message
				assertNotNull(registryAccountSearchResponseEntity1.getResults());
				assertEquals(1, registryAccountSearchResponseEntity1.getResults().size());
			}

			if (registryAccountCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				ResponseEntity<List<RegistryAccount>> RegistryAccountSearchResponseEntity = registryAccountService2
						.search(registryAccountCreated.getName(), 0, 1);
				for (Message message : RegistryAccountSearchResponseEntity.getMessages()) {
					logger.warn("Error while Search request  [{}] ", message.getMessageText());
				}
				assertNotNull(RegistryAccountSearchResponseEntity);
				assertNotNull(RegistryAccountSearchResponseEntity.isErrors());
				// TODO: add tests for testing error message
				assertNotNull(RegistryAccountSearchResponseEntity.getResults());
				assertEquals(1, RegistryAccountSearchResponseEntity.getResults().size());
			}
			if (registryAccountCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
				ResponseEntity<List<RegistryAccount>> registryAccountSearchResponseEntity = registryAccountService2
						.search(registryAccountCreated.getName(), 0, 1);
				for (Message message : registryAccountSearchResponseEntity.getMessages()) {
					logger.warn("Error while Search request  [{}] ", message.getMessageText());
				}
				assertNotNull(registryAccountSearchResponseEntity);
				assertNotNull(registryAccountSearchResponseEntity.isErrors());
				// TODO: add tests for testing error message
				assertNotNull(registryAccountSearchResponseEntity.getResults());
				assertEquals(1, registryAccountSearchResponseEntity.getResults().size());
			}
			if (registryAccountCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				ResponseEntity<List<RegistryAccount>> registryAccountSearchResponseEntity = registryAccountService3
						.search(registryAccount.getName(), 0, 1);
				for (Message message : registryAccountSearchResponseEntity.getMessages()) {
					logger.warn("Error while Search request  [{}] ", message.getMessageText());
				}
				assertNotNull(registryAccountSearchResponseEntity);
				assertNotNull(registryAccountSearchResponseEntity.isErrors());
				// TODO: add tests for testing error message
				assertNotNull(registryAccountSearchResponseEntity.getResults());
				assertEquals(0, registryAccountSearchResponseEntity.getResults().size());
			}
		}
	}

	@Test
	public void testEntitledFindById() {
		logger.info("Create Blueprint [{}]", registryAccount.getName());
		ResponseEntity<RegistryAccount> response = registryAccountService.create(registryAccount);
		for (Message m : response.getMessages()) {
			logger.warn("[{}]", m.getMessageText());
		}
		if (response.getResults() != null) {
			registryAccountCreated = response.getResults();
		}
		if (!error) {
			if (registryAccountCreated.getEntitlementType().equals(EntitlementType.OWNER)) {
				ResponseEntity<RegistryAccount> findbyIdResponse = registryAccountService2
						.findById(registryAccountCreated.getId());
				for (Message message : findbyIdResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
				assertNotNull(findbyIdResponse);
				assertNotNull(findbyIdResponse.isErrors());
				assertEquals(findbyIdResponse.getResults(), null);
			}

			if (registryAccountCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				logger.info("Created ID is [{}]", registryAccountCreated.getId());
				ResponseEntity<RegistryAccount> findbyIdResponse = registryAccountService2
						.findById(registryAccountCreated.getId());
				for (Message message : findbyIdResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
				assertNotNull(findbyIdResponse.getResults());
				assertNotNull(findbyIdResponse.getResults().getId());
				assertEquals(registryAccountCreated.getId(), findbyIdResponse.getResults().getId());
			}
			if (registryAccountCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
				ResponseEntity<RegistryAccount> findbyIdResponse = registryAccountService2
						.findById(registryAccountCreated.getId());
				for (Message message : findbyIdResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
				assertNotNull(findbyIdResponse.getResults());
				assertNotNull(findbyIdResponse.getResults().getId());
				assertEquals(registryAccountCreated.getId(), findbyIdResponse.getResults().getId());
			}
			if (registryAccountCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				ResponseEntity<RegistryAccount> findbyIdResponse = registryAccountService3
						.findById(registryAccountCreated.getId());
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
        if (registryAccountCreated != null) {
            logger.info("cleaning up...");
            ResponseEntity<?> response = registryAccountService.delete(registryAccountCreated.getId());
            for (Message message : response.getMessages()) {
                logger.warn("Error user deletion: [{}] ", message.getMessageText());
            }
        }
    }
}
