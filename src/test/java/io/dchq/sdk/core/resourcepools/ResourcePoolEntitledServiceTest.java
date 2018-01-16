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
package io.dchq.sdk.core.resourcepools;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
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
import org.springframework.util.StringUtils;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.provider.ResourcePool;
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
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
public class ResourcePoolEntitledServiceTest extends AbstractServiceTest {

	private ResourcePoolService resourcePoolService;
	private ResourcePoolService resourcePoolService2;
	private ResourcePool resourcePool;
	private ResourcePool resourcePoolCreated;
	
	@Before
	public void setUp() throws Exception {
		resourcePoolService = ServiceFactory.buildResourcePoolService(rootUrl1, tenant_username, tenant_password);
		resourcePoolService2 = ServiceFactory.buildResourcePoolService(rootUrl1, username1, password1);
	}

	public ResourcePoolEntitledServiceTest(String name, String rpType, int cpu, int memory, int disk, 
			EntitlementType entitlementType, boolean isEntitlementTypeUser, String entitledUserId) {

		String prefix = RandomStringUtils.randomAlphabetic(3);
		name = name + " " + prefix;

		this.resourcePool = new ResourcePool();
		this.resourcePool.setName(name);
		this.resourcePool.setRpType(rpType);
		this.resourcePool.setCpu(cpu);
		this.resourcePool.setMem(memory);
		this.resourcePool.setDisk(disk);
		this.resourcePool.setAzName(quotaName);
		this.resourcePool.setAzId(quotaId);
		this.resourcePool.setEntitlementType(entitlementType);

		if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
			UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
			List<UsernameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			this.resourcePool.setEntitledUsers(entiledUsers);
		} else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
			NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
			List<NameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			this.resourcePool.setEntitledUserGroups(entiledUsers);
		}

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {

				{ "Resource Pool", "RESOURCE_POOL", 2, 2, 10, EntitlementType.CUSTOM, true, userId1 },
				{ "Resource Pool", "RESOURCE_POOL", 2, 2, 10, EntitlementType.CUSTOM, false, USER_GROUP } 
				});
	}


	@Test
	public void testEntitlement() throws Exception {

		logger.info("Resource Pool by name [{}] and type [{}]", resourcePool.getName(), resourcePool.getRpType());

		ResponseEntity<ResourcePool> response = resourcePoolService.create(resourcePool);

		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}

		assertNotNull(response);
		assertNotNull(response.isErrors());
		this.resourcePoolCreated = response.getResults();
		assertNotNull(response.getResults().getId());
		
		ResponseEntity<ResourcePool> rpResponseEntity = resourcePoolService2.findById(resourcePoolCreated.getId());

		for (Message message : rpResponseEntity.getMessages()) {
			logger.warn("Error [{}] ", message.getMessageText());
		}
		assertNotNull(rpResponseEntity);
		assertNotNull(rpResponseEntity.getResults());
		ResourcePool searchedEntity = rpResponseEntity.getResults();

		assertEquals(resourcePoolCreated.getRpType(), searchedEntity.getRpType());
		assertEquals(resourcePoolCreated.getName(), searchedEntity.getName());
		assertEquals(resourcePoolCreated.getCpu(), searchedEntity.getCpu());
		assertEquals(resourcePoolCreated.getMem(), searchedEntity.getMem());
		assertEquals(resourcePoolCreated.getDisk(), searchedEntity.getDisk());

	}

	@After
	public void cleanUp() {

		if (resourcePoolCreated != null) {
			logger.info("cleaning up Resource Pool...");
			ResponseEntity<?> response = resourcePoolService.delete(resourcePoolCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error quota deletion: [{}] ", message.getMessageText());
			}
		}

	}
}
