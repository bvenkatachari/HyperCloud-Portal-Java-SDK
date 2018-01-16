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

import org.apache.commons.lang3.RandomStringUtils;
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
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.RegistryAccountService;
import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class CloudProviderUpdateServiceTest extends AbstractServiceTest {

	private RegistryAccountService registryAccountService;
	private RegistryAccount registryAccount;
	private RegistryAccount registryAccountCreated;

	public CloudProviderUpdateServiceTest(String name, AccountType accountType, String url, String userName,
			String password) {

		String prefix = RandomStringUtils.randomAlphabetic(3);
		name = name + " " + prefix;

		this.registryAccount = new RegistryAccount();
		this.registryAccount.setName(name);
		this.registryAccount.setAccountType(accountType);
		this.registryAccount.setUrl(url);
		this.registryAccount.setUsername(userName);
		this.registryAccount.setPassword(password);
		this.registryAccount.setInactive(false);
		this.registryAccount.setBlueprintEntitlementType(EntitlementType.ALL_BLUEPRINTS);
		this.registryAccount.setEntitlementType(EntitlementType.OWNER);
		this.registryAccount.setVmQuota(100);
		this.registryAccount.setFreeFormEntitlement("true");
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {

				{ "Cloud Provider ", AccountType.HYPER_GRID, null, null, null },
				{ "Cloud Provider ", AccountType.HCS_VSPHERE, "127.0.0.1", "dchqinc", "password" },
				{ "Cloud Provider ", AccountType.VLAN_PROVIDER, "http://10.100.15.13:80/api/v1/switches/vlansummary",
						"dchqinc", "password" },
				{ "Cloud Provider ", AccountType.CREDENTIALS, null, "dchqinc", "password" } });
	}

	@Before
	public void setUp() throws Exception {
		registryAccountService = ServiceFactory.buildRegistryAccountService(rootUrl, cloudadminusername,
				cloudadminpassword);
	}

	@Test
	public void testUpdate() throws Exception {

		logger.info("Cloud Provide by name [{}] and type [{}]", registryAccount.getName(),
				registryAccount.getAccountType());
		ResponseEntity<RegistryAccount> response = registryAccountService.create(registryAccount);
		
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}
		
		assertNotNull(response);
		assertNotNull(response.isErrors());
		this.registryAccountCreated = response.getResults();
		String modifiedName = registryAccount.getName() + "_Update";
		// updating name
		registryAccountCreated.setName(modifiedName);
		logger.info("Update Registry Account with Name [{}]", registryAccountCreated.getName());
		response = registryAccountService.update(registryAccountCreated);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}] ", message.getMessageText());
		}
		if (!response.isErrors() && response.getResults() != null) {
			assertEquals(registryAccountCreated.getName(), response.getResults().getName());
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
