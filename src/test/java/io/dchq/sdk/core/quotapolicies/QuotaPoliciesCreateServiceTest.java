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

package io.dchq.sdk.core.quotapolicies;

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
import com.dchq.schema.beans.one.quotapolicy.QuotaEntitlementType;
import com.dchq.schema.beans.one.quotapolicy.QuotaPolicy;
import com.dchq.schema.beans.one.quotapolicy.QuotaType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.QuotaPolicyService;
import io.dchq.sdk.core.ServiceFactory;

/**
 * @author Jagdeep Jain
 * @since 1.0 
 */

/**
 * Quota Policies: Create
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class QuotaPoliciesCreateServiceTest extends AbstractServiceTest {

	private QuotaPolicyService service;
	private QuotaPolicy quotaPolicy;
	private QuotaPolicy quotaPolicyCreated;
	private boolean success;

	public QuotaPoliciesCreateServiceTest(
			String name, 
			QuotaEntitlementType quotaEntitlementType, 
			QuotaType quotaType, 
			Integer quotaValue,
			boolean success
		) {
		// random name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		name = prefix + "-" + name;
		this.quotaPolicy = new QuotaPolicy();
		quotaPolicy.setName(name);
		quotaPolicy.setQuotaEntitlementType(quotaEntitlementType);
		quotaPolicy.setQuotaType(quotaType);
		quotaPolicy.setQuotaValue(quotaValue);
		this.success = success;

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			{"apitest", QuotaEntitlementType.ANY_USER , QuotaType.VM, 1, false}
		});
	}

	@Before
	public void setUp() throws Exception {
		service = ServiceFactory.buildQuotaPolicyService(rootUrl, username, password);
	}

	@Test
	public void testCreate() {
		logger.info("Price profile by name [{}]", quotaPolicy.getName());
		ResponseEntity<QuotaPolicy> response = service.create(quotaPolicy);
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}
		if (response.getResults() != null) {
			this.quotaPolicyCreated = response.getResults();
			logger.info("Create cost profile Successful..");
		}
		assertNotNull(response);
		assertNotNull(response.isErrors());
		assertEquals(quotaPolicy.getName(), quotaPolicyCreated.getName());

	}

	@After
	public void cleanUp() {
		if (quotaPolicyCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = service.delete(quotaPolicyCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error user deletion: [{}] ", message.getMessageText());
			}
		}
	}

}
