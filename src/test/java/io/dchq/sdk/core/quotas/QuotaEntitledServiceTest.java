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

import java.util.ArrayList;
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
import com.dchq.schema.beans.one.blueprint.AccountType;
import com.dchq.schema.beans.one.blueprint.RegistryAccount;
import com.dchq.schema.beans.one.provider.ResourcePool;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.security.Tenant;

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
public class QuotaEntitledServiceTest extends AbstractServiceTest {

	private ResourcePoolService resourcePoolService;
	private ResourcePoolService resourcePoolService2;
	private ResourcePool quota;
	private ResourcePool quotaCreated;
	private RegistryAccountService registryAccountService;
	private RegistryAccount registryAccount;
	private RegistryAccount registryAccountCreated;
	private RegistryAccount availabilityZone;
	private RegistryAccount availabilityZoneCreated;

	public QuotaEntitledServiceTest(String name, String rpType, int cpu, int memory, int disk, String tenant_id) {

		String prefix = RandomStringUtils.randomAlphabetic(3);
		name = name + " " + prefix;

		this.quota = new ResourcePool();
		this.quota.setName(name);
		this.quota.setRpType(rpType);
		this.quota.setCpu(cpu);
		this.quota.setMem(memory);
		this.quota.setDisk(disk);
		this.quota.setEntitlementType(EntitlementType.TENANTS);

		Tenant tenant = new Tenant();
		tenant.setId(tenant_id);
		List<Tenant> tenants = new ArrayList<>();
		tenants.add(tenant);
		this.quota.setEntitledTenants(tenants);

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {

				{ "Quota", "QUOTA", 2, 10, 50, tenantId } });
	}

	@Before
	public void setUp() throws Exception {
		registryAccountService = ServiceFactory.buildRegistryAccountService(rootUrl1, cloudadminusername,
				cloudadminpassword);
		resourcePoolService = ServiceFactory.buildResourcePoolService(rootUrl1, cloudadminusername, cloudadminpassword);
		resourcePoolService2 = ServiceFactory.buildResourcePoolService(rootUrl1, tenant_username, tenant_password);
	}

	@Test
	public void testEntitlement() throws Exception {

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

		// Search from Tenant's credentials
		// https://172.16.1.30/api/resourcepools/tenant/QUOTA?page=0&pageSize=10
		ResponseEntity<List<ResourcePool>> quotaResponseEntity = resourcePoolService2.findAllTenantQuotas(0, 100);

		for (Message message : quotaResponseEntity.getMessages()) {
			logger.warn("Error [{}] ", message.getMessageText());
		}

		assertNotNull(quotaResponseEntity);
		assertNotNull(quotaResponseEntity.isErrors());
		assertNotNull(quotaResponseEntity.getResults());

		boolean found = false;

		for (ResourcePool searchedEntity : quotaResponseEntity.getResults()) {

			if (searchedEntity.getName().equals(quotaCreated.getName())) {

				found = true;

				assertEquals(quotaCreated.getRpType(), searchedEntity.getRpType());
				assertEquals(quotaCreated.getName(), searchedEntity.getName());
				assertEquals(quotaCreated.getCpu(), searchedEntity.getCpu());
				assertEquals(quotaCreated.getMem(), searchedEntity.getMem());
				assertEquals(quotaCreated.getDisk(), searchedEntity.getDisk());
			}

		}

		Assert.assertTrue(found);

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
