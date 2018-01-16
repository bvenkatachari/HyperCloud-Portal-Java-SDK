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
public class CloudProviderAvailabilityZonesTest extends AbstractServiceTest {

	private RegistryAccountService registryAccountService;
	private RegistryAccount registryAccount;
	private RegistryAccount registryAccountCreated;
	private RegistryAccount availabilityZone;
	private RegistryAccount availabilityZoneCreated;

	public CloudProviderAvailabilityZonesTest(String name, AccountType accountType, String url, String userName,
			String password, String imageId, String hardwareId) {

		String prefix = RandomStringUtils.randomAlphabetic(3);
		name = name +" "+prefix ;

		this.availabilityZone = new RegistryAccount();
		this.availabilityZone.setName(name);
		this.availabilityZone.setAccountType(accountType);
		this.availabilityZone.setUrl(url);
		this.availabilityZone.setUsername(userName);
		this.availabilityZone.setPassword(password);
		this.availabilityZone.setInactive(false);
		this.availabilityZone.setBlueprintEntitlementType(EntitlementType.ALL_BLUEPRINTS);
		this.availabilityZone.setEntitlementType(EntitlementType.OWNER);
		this.availabilityZone.setVmQuota(100);
		this.availabilityZone.setFreeFormEntitlement("true");
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {

				{ "Availability Zone", AccountType.AVAILABILITY_ZONE, "https://127.0.0.1:443", "Hyper-V", "password", "\\Shared-Volume", "\\Shared-Volume" } });
	}

	@Before
	public void setUp() throws Exception {
		registryAccountService = ServiceFactory.buildRegistryAccountService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	@Test
	public void testCreate() throws Exception {
		
		createCloudProvider();
		
		this.availabilityZone.setReferenceId(this.registryAccountCreated.getId());

		logger.info("Availability Zone by name [{}] and type [{}]", availabilityZone.getName(),
				availabilityZone.getAccountType());
		ResponseEntity<RegistryAccount> response = registryAccountService.create(availabilityZone);
		
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}
		
		assertNotNull(response);
		assertNotNull(response.isErrors());
		this.availabilityZoneCreated = response.getResults();
		assertNotNull(response.getResults().getId());
		
		//Find AZ by provider Id
		ResponseEntity<List<RegistryAccount>> response2 = registryAccountService.findAZByRegistryAccountId(this.registryAccountCreated.getId());
		assertNotNull(response2);
		assertNotNull(response2.isErrors());
		assertNotNull(response2.getResults());
		
		//We have only one az attached to cloud provider
		for(RegistryAccount az : response2.getResults()){
			assertEquals(availabilityZoneCreated.getAccountType(), az.getAccountType());
			assertEquals(availabilityZoneCreated.getName(), az.getName());
		}

	}

	private RegistryAccount createCloudProvider(){
		
		this.registryAccount = new RegistryAccount();
		this.registryAccount.setName("Cloud Provider Automation");
		this.registryAccount.setAccountType(AccountType.HYPER_GRID);
		this.registryAccount.setUrl("127.0.0.1");
		this.registryAccount.setUsername("dchqinc");
		this.registryAccount.setPassword("password");
		this.registryAccount.setInactive(false);
		this.registryAccount.setBlueprintEntitlementType(EntitlementType.ALL_BLUEPRINTS);
		this.registryAccount.setEntitlementType(EntitlementType.OWNER);
		this.registryAccount.setVmQuota(100);
		this.registryAccount.setFreeFormEntitlement("true");	
		
		ResponseEntity<RegistryAccount> response = registryAccountService.create(registryAccount);
		assertNotNull(response);
		assertNotNull(response.isErrors());
		this.registryAccountCreated = response.getResults();
		
		return this.registryAccountCreated;
		
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
