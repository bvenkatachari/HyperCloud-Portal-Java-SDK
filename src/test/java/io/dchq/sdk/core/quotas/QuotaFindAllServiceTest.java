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
package io.dchq.sdk.core.quotas;

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
import com.dchq.schema.beans.one.provider.ResourcePool;
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.RegistryAccountService;
import io.dchq.sdk.core.ResourcePoolService;
import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class QuotaFindAllServiceTest extends AbstractServiceTest {

	private ResourcePoolService resourcePoolService;
	private ResourcePool quota;
	private ResourcePool quotaCreated;
	private RegistryAccountService registryAccountService;
	private RegistryAccount registryAccount;
	private RegistryAccount registryAccountCreated;
	private RegistryAccount availabilityZone;
	private RegistryAccount availabilityZoneCreated;
	
	private int countBeforeCreate;
	private int countAfterCreate;

	public QuotaFindAllServiceTest(String name, String rpType, int cpu, int memory, int disk) {

		String prefix = RandomStringUtils.randomAlphabetic(3);
		name = name + " " + prefix;

		this.quota = new ResourcePool();
		this.quota.setName(name);
		this.quota.setRpType(rpType);
		this.quota.setCpu(cpu);
		this.quota.setMem(memory);
		this.quota.setDisk(disk);

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {

				{ "Quota", "QUOTA", 2, 10, 50 } });
	}

	@Before
	public void setUp() throws Exception {
		registryAccountService = ServiceFactory.buildRegistryAccountService(rootUrl1, cloudadminusername,
				cloudadminpassword);
		resourcePoolService = ServiceFactory.buildResourcePoolService(rootUrl1, cloudadminusername, cloudadminpassword);
	}
	
	
	public int testQuotaPosition(String id) {
		ResponseEntity<List<ResourcePool>> response = null;
		try {
			response = resourcePoolService.findAll(0, 5000);
			for (Message message : response.getMessages()) {
				logger.warn("Error [{}]  " + message.getMessageText());
			}
			assertNotNull(response);
			assertNotNull(response.isErrors());
			assertEquals(false, response.isErrors());
			int position = 0;
			if (id != null) {
				for (ResourcePool obj : response.getResults()) {
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
			if (id == null)
				return 0;
			else
				return 1;
		else
			return response.getResults().size();
	}


	@Test
	public void testFindAll() throws Exception {
		
		this.countBeforeCreate = testQuotaPosition(null);

		createAvailabilityZone();

		this.quota.setAzName(this.availabilityZoneCreated.getName());
		this.quota.setAzId(this.availabilityZoneCreated.getId());

		logger.info("Quota by name [{}] and type [{}]", quota.getName(), quota.getRpType());

		ResponseEntity<ResourcePool> response = resourcePoolService.create(quota);
		
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}
		
		assertNotNull(response);
		assertNotNull(response.isErrors());
		this.quotaCreated = response.getResults();
		assertNotNull(response.getResults().getId());
		assertEquals(quota.getRpType(), quotaCreated.getRpType());
		assertEquals(quota.getName(), quotaCreated.getName());
		assertEquals(quota.getCpu(), quotaCreated.getCpu());
		assertEquals(quota.getMem(), quotaCreated.getMem());
		assertEquals(quota.getDisk(), quotaCreated.getDisk());
		
		this.countAfterCreate = testQuotaPosition(quotaCreated.getId());
		assertEquals(
				"Count of Find all Quota between before and after create does not have diffrence of 1 for QuotaID :"
						+ quotaCreated.getId(),
				countBeforeCreate + 1, countAfterCreate);

	}

	private RegistryAccount createAvailabilityZone() {

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

		this.availabilityZone = new RegistryAccount();
		this.availabilityZone.setName("Availability Zone Automation");
		this.availabilityZone.setAccountType(AccountType.AVAILABILITY_ZONE);
		this.availabilityZone.setUrl("127.0.0.1");
		this.availabilityZone.setUsername("dchqinc");
		this.availabilityZone.setPassword("password");
		this.availabilityZone.setInactive(false);
		this.availabilityZone.setBlueprintEntitlementType(EntitlementType.ALL_BLUEPRINTS);
		this.availabilityZone.setEntitlementType(EntitlementType.OWNER);
		this.availabilityZone.setVmQuota(100);
		this.availabilityZone.setFreeFormEntitlement("true");

		this.availabilityZone.setReferenceId(this.registryAccountCreated.getId());

		ResponseEntity<RegistryAccount> response2 = registryAccountService.create(availabilityZone);
		assertNotNull(response2);
		assertNotNull(response2.isErrors());
		this.availabilityZoneCreated = response2.getResults();

		return this.availabilityZone;

	}

	@After
	public void cleanUp() {

		if (quotaCreated != null) {
			logger.info("cleaning up Quota...");
			ResponseEntity<?> response = resourcePoolService.delete(quotaCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error quota deletion: [{}] ", message.getMessageText());
			}
		}

		if (registryAccountCreated != null) {
			logger.info("cleaning up cloud provider...");
			ResponseEntity<?> response = registryAccountService.delete(registryAccountCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error cloud provider deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
