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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.blueprint.AccountType;
import com.dchq.schema.beans.one.blueprint.RegistryAccount;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.RegistryAccountService;
import io.dchq.sdk.core.ServiceFactory;

/**
 * Abstracts class for holding test credentials.
 *
 * @author Abedeen.
 * @updater Jagdeep Jain
 * @since 1.0
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class CloudProviderSearchServiceTest extends AbstractServiceTest {
	
    private RegistryAccountService registryAccountService;
    private RegistryAccount registryAccount;
    private boolean success;
    private RegistryAccount registryAccountCreated;
    private String validationMssage;
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
				{ "Rackspace US 1 testAccount", "dchqinc", "7b1fa480664b4823b72abed54ebb9b0f", AccountType.RACKSPACE,
						false }
        });
    }

    public CloudProviderSearchServiceTest (
    		String name, 
    		String testUsername,
    		String apiKey,
    		AccountType accountType,
    		/*
    		String rackspaceName, 
    		Boolean isActive, 
    		String Password, 
    		String validationMssage, 
    		*/
    		boolean success
    		) 
	{
		this.registryAccount = new RegistryAccount().withName(name).withUsername(testUsername).withPassword(apiKey)
				.withAccountType(accountType);
		this.success = success;
	}
    
    @Before
    public void setUp() throws Exception {
        registryAccountService = ServiceFactory.buildRegistryAccountService(rootUrl, username, password);
    }

    @Test
    public void testSearch() throws Exception {
		ResponseEntity<RegistryAccount> response = registryAccountService.create(registryAccount);
		logger.info("Create Registry Account with Name [{}]", registryAccount.getName());
		if (success) {
			logger.info("Expecting Error while Create Registry Account with Name [{}]", registryAccount.getName());
		}
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}
		if (!response.isErrors() && response.getResults() != null) {
			this.registryAccountCreated = response.getResults();
		}
		assertNotNull(response);
		assertNotNull(response.isErrors());
		assertEquals(validationMssage, ((Boolean) success).toString(), ((Boolean) response.isErrors()).toString());
		if (!success) {
			logger.info(" Registry Account Created with Name [{}] and ID [{}]", registryAccountCreated.getName(),
					registryAccountCreated.getId());
			assertNotNull(response.getResults());
			assertNotNull(response.getResults().getId());
			assertEquals(registryAccount.getUsername(), registryAccountCreated.getUsername());
			assertEquals(registryAccount.getAccountType(), registryAccountCreated.getAccountType());
			assertEquals(registryAccount.getAccountType(), registryAccountCreated.getAccountType());
			// Password should always be empty
			assertEquals("password-hidden", registryAccountCreated.getPassword());
		}
		ResponseEntity<List<RegistryAccount>> registryAccountResponseEntity = registryAccountService
				.search(registryAccountCreated.getName(), 0, 1);
		
		for (Message message : registryAccountResponseEntity.getMessages()) {
			logger.warn("Error [{}] ", message.getMessageText());
		}
		assertNotNull(registryAccountResponseEntity);
		assertNotNull(registryAccountResponseEntity.getResults());
		assertEquals(1, registryAccountResponseEntity.getResults().size());
		RegistryAccount searchedEntity = registryAccountResponseEntity.getResults().get(0);
		assertEquals(registryAccountCreated.getId(), searchedEntity.getId());
		assertEquals(registryAccountCreated.getName(), searchedEntity.getName());
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
