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

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
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
 * @author Abedeen.
 * @updater Jagdeep Jain
 * @since 1.0
 */

/**
 * Cloud Providers: FindAll
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class CloudProviderFindAllServiceTest extends AbstractServiceTest {

	private RegistryAccountService registryAccountService;
	private RegistryAccount registryAccount;
	private boolean success;
	private RegistryAccount registryAccountCreated;
	private int countBeforeCreate = 0, countAfterCreate = 0, countAfterDelete = 0;

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { AccountType.RACKSPACE, "Rackspace US 2 testAccount", "dchqinc",
				"apiKey", null, null, null, null, null, null, null, false } });
	}

	public CloudProviderFindAllServiceTest(
			// below fields are for Rackspace, Amazon, Digital Ocean, Google
			// Cloud, Aliyun
			AccountType accountType, String accountName, String testUsername, // also corresponds to application client id on UI
			String apiKey, // also corresponds to password, application client
							// secret id on UI

			// additional fields for Microsoft Azure
			String subscriptionId, // corresponds to email in API call
			String TenantId, // corresponds to region in API call

			// additional field for IBM Softlayer
			String domainName, // corresponds to groupName in API call

			// additional fields for private cloud
			String vmDestination, // corresponds to hardwareId in API call
			String template, // corresponds to imageId in API call

			// additional field for volume provider
			String opts, Integer size,

			boolean success) {
		String postfix = RandomStringUtils.randomAlphabetic(3);
    	accountName = accountName+" "+postfix;
		this.registryAccount = new RegistryAccount().withName(accountName).withUsername(testUsername)
				.withPassword(apiKey).withAccountType(accountType);
		this.registryAccount.setRegion(TenantId);
		this.registryAccount.setEmail(subscriptionId);
		this.registryAccount.setGroupName(domainName);
		this.registryAccount.setHardwareId(vmDestination);
		this.registryAccount.setImageId(template);
		this.registryAccount.setOpts(opts);
		this.registryAccount.setSizeLimit(size);
		this.success = success;
	}

	public int testRegistryAccountPosition(String id) {
		// TODO report a bug findAll if nothing found do not throw an exception
		// instead it should return zero
		ResponseEntity<List<RegistryAccount>> response = null;
		try {
			response = registryAccountService.findAll(0, 5000);
			for (Message message : response.getMessages()) {
				logger.warn("Error [{}]  " + message.getMessageText());
			}
			assertNotNull(response);
			assertNotNull(response.isErrors());
			assertEquals(false, response.isErrors());
			int position = 0;
			if (id != null) {
				for (RegistryAccount obj : response.getResults()) {
					position++;
					if (obj.getId().equals(id)) {
						logger.info("  Object Matched in FindAll {}  at Position : {}", id, position);
						assertEquals("Recently Created Object is not at Positon 1 :" + obj.getId(), 1, position);
					}
				}
			}
			logger.info(" Total Number of Objects :{}", response.getResults().size());
		} catch (Exception e) {

		}
		if (response == null)
			return 0;
		else
			return response.getResults().size();
	}

	@Before
	public void setUp() throws Exception {
		registryAccountService = ServiceFactory.buildRegistryAccountService(rootUrl, cloudadminusername,
				cloudadminpassword);
	}

	@Test
	public void testFindAll() throws Exception {
		logger.info("Count of Cloud Provider before Create Cloudprovider with  Account with Name [{}]",
				registryAccount.getName());
		countBeforeCreate = testRegistryAccountPosition(null);
		logger.info("Create Registry Account with Name [{}]", registryAccount.getName());
		if (success) {
			logger.info("Expecting Error while Create Registry Account with Name [{}]", registryAccount.getName());
		}
		ResponseEntity<RegistryAccount> response = registryAccountService.create(registryAccount);
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create Cloutprovider  [{}] ", message.getMessageText());
		}
		if (response.getResults() != null) {
			this.registryAccountCreated = response.getResults();
			logger.info(" Registry Account Created with Name [{}] and ID [{}]", registryAccountCreated.getName(),
					registryAccountCreated.getId());
		}
		assertNotNull(response);
		assertNotNull(response.isErrors());
		if (!success) {
			assertNotNull(response.getResults());
			assertNotNull(response.getResults().getId());
			assertEquals("UserName is invalid, when compared with input UserName @ Creation Time ",
					registryAccount.getUsername(), registryAccountCreated.getUsername());
			assertEquals(registryAccount.getAccountType(), registryAccountCreated.getAccountType());
			// Password should always be empty
			assertEquals("Password is not Expected in Response. ", "password-hidden",
					registryAccountCreated.getPassword());
			// getting Count of objects after creating Object
			logger.info("FindAll User RegistryAccount by Id [{}]", registryAccountCreated.getId());
			this.countAfterCreate = testRegistryAccountPosition(registryAccountCreated.getId());
			assertEquals(
					"Count of Find all RegistryAccount between before and after create does not have diffrence of 1 for UserId :"
							+ registryAccountCreated.getId(),
					countBeforeCreate + 1, countAfterCreate);
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
		logger.info("Find All RegistryAccount After Delete  User by Id {}", registryAccountCreated.getId());
		countAfterDelete = testRegistryAccountPosition(null);
		assertEquals("Count of FInd all RegistryAccount between before and after delete are not same for UserId :"
				+ registryAccountCreated.getId(), countBeforeCreate, countAfterDelete);
	}
}
