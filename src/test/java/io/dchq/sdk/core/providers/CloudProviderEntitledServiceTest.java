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

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
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
	
    private RegistryAccountService registryAccountService, registryAccountService2;
    private RegistryAccount registryAccount;
    private boolean createError;
    private RegistryAccount registryAccountCreated;
    private String validationMessage;
 
    public CloudProviderEntitledServiceTest (
    		AccountType accountType,
    		String accountName,
    		String testUsername,
    		Boolean isActive,
    		String password,
    	
    		EntitlementType entitlementType, 
    		boolean isEntitlementTypeUser, 
    		String entitledUserId, 
    		String validationMessage, 
    		boolean success
    		) 
    {
		this.registryAccount = new RegistryAccount().withName(accountName).withUsername(testUsername)
				.withPassword(password).withAccountType(accountType).withInactive(isActive);
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
		
        this.createError = success;
        this.validationMessage = validationMessage;
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
        	
				{ AccountType.RACKSPACE, "Rackspace US 1 testAccount", "dchqinc", Boolean.FALSE, "password",
						EntitlementType.CUSTOM, true, userId2, "General Input", false },
				{ AccountType.RACKSPACE, "Rackspace US 1 testAccount", "dchqinc", Boolean.FALSE, "password",
						EntitlementType.CUSTOM, false, USER_GROUP, "General Input", false },
				{ AccountType.RACKSPACE, "Rackspace US 1 testAccount", "dchqinc", Boolean.FALSE, "password",
						EntitlementType.OWNER, false, null, "General Input", false },
				{ AccountType.RACKSPACE, "Rackspace US 1 testAccount", "dchqinc", Boolean.FALSE, "password",
						EntitlementType.OWNER, false, "", "General Input", false }
        });
    }

    @Before
    public void setUp() throws Exception {
        registryAccountService = ServiceFactory.buildRegistryAccountService(rootUrl, username, password);
        registryAccountService2 = ServiceFactory.buildRegistryAccountService(rootUrl, username2, password2);
    }
    
    @Ignore
    @Test
    public void testEntitled() throws Exception {
		logger.info("Create Registry Account with Name [{}], entitlement Type [{}]", registryAccount.getName(),
				registryAccount.getEntitlementType());
		if (createError) {
            logger.info("Expecting Error while Create Registry Account with Name [{}]", registryAccount.getName());
        }
        ResponseEntity<RegistryAccount> response = registryAccountService.create(registryAccount);
        if (response.getResults() != null) {
        	this.registryAccountCreated = response.getResults();
        }
        if (response.isErrors()) {
            logger.warn("Message from Server... {}", response.getMessages().get(0).getMessageText());
        }
        assertNotNull(response);
        assertNotNull(response.isErrors());
        assertEquals(validationMessage, ((Boolean) createError).toString(), ((Boolean) response.isErrors()).toString());
        if (!createError) {
            this.registryAccountCreated = response.getResults();
			logger.info(" Registry Account Created with Name [{}] and ID [{}]", registryAccountCreated.getName(),
					registryAccountCreated.getId());
			assertNotNull(response.getResults());
            assertNotNull(response.getResults().getId());
            assertEquals(registryAccount.getUsername(), registryAccountCreated.getUsername());
            assertEquals(registryAccount.getInactive(), registryAccountCreated.getInactive());
            assertEquals(registryAccount.getAccountType(), registryAccountCreated.getAccountType());
            // password should always be empty
            assertThat("password-hidden", is(registryAccountCreated.getPassword()));
            
            ResponseEntity<RegistryAccount> entitledResponse = registryAccountService2
					.findById(registryAccountCreated.getId());
           
            // valid UserId2 can access plugins
			if (registryAccount.getEntitlementType() == EntitlementType.CUSTOM && !StringUtils.isEmpty(userId2)) {
				logger.info("Response [{}]", entitledResponse.getResults());
				assertNotNull(entitledResponse.getResults());
				assertEquals(registryAccountCreated.getId(), entitledResponse.getResults().getId());
				
			} else if (registryAccount.getEntitlementType() == EntitlementType.OWNER) {
				logger.info("Response [{}]", entitledResponse.getResults());
				assertNotNull(entitledResponse.getResults());
				assertEquals(registryAccountCreated.getId(), entitledResponse.getResults().getId());
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
