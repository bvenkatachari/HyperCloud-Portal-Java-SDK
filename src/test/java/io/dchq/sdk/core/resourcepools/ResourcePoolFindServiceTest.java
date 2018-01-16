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

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
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
public class ResourcePoolFindServiceTest extends AbstractServiceTest {

	private ResourcePoolService resourcePoolService;
	private ResourcePool resourcePool;
	private ResourcePool resourcePoolCreated;

	public ResourcePoolFindServiceTest(String name, String rpType, int cpu, int memory, int disk, EntitlementType entitleType) {

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
		this.resourcePool.setEntitlementType(entitleType);

		if(entitleType.equals(EntitlementType.CUSTOM)){
			UsernameEntityBase entitledUser = new UsernameEntityBase().withId(userId1);
			List<UsernameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			this.resourcePool.setEntitledUsers(entiledUsers);
		}

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {

			{ "Resource Pool", "RESOURCE_POOL", 2, 2, 10, EntitlementType.PUBLIC }, 
			{ "Resource Pool", "RESOURCE_POOL", 2, 2, 10, EntitlementType.OWNER }, 
			{ "Resource Pool", "RESOURCE_POOL", 2, 2, 10, EntitlementType.CUSTOM} 
			
		});
	}

	@Before
	public void setUp() throws Exception {
		resourcePoolService = ServiceFactory.buildResourcePoolService(rootUrl1, tenant_username, tenant_password);
	}

	@Test
	public void testFind() throws Exception {

		logger.info("Resource Pool by name [{}] and type [{}]", resourcePool.getName(), resourcePool.getRpType());

		ResponseEntity<ResourcePool> response = resourcePoolService.create(resourcePool);

		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}

		assertNotNull(response);
		assertNotNull(response.isErrors());
		this.resourcePoolCreated = response.getResults();
		assertNotNull(response.getResults().getId());

		// find service call by getId
		response = resourcePoolService.findById(resourcePoolCreated.getId());
		for (Message message : response.getMessages()) {
			logger.warn("Error while Finding Quota  [{}] ", message.getMessageText());
		}

		ResourcePool findEntity = response.getResults();

		assertEquals(resourcePoolCreated.getRpType(), findEntity.getRpType());
		assertEquals(resourcePoolCreated.getName(), findEntity.getName());
		assertEquals(resourcePoolCreated.getCpu(), findEntity.getCpu());
		assertEquals(resourcePoolCreated.getMem(), findEntity.getMem());
		assertEquals(resourcePoolCreated.getDisk(), findEntity.getDisk());

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
