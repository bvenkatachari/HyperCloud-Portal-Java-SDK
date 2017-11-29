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

package io.dchq.sdk.core.costpolicies;

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
import com.dchq.schema.beans.one.price.PriceProfile;
import com.dchq.schema.beans.one.price.PriceUnit;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.CostPoliciesService;
import io.dchq.sdk.core.ServiceFactory;

/**
 * @author Jagdeep Jain
 * @since 1.0 
 */

/**
 * Cost Policies: Create
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class CostPoliciesCreateServiceTest extends AbstractServiceTest {

	private CostPoliciesService service;
	private PriceProfile priceProfile;
	private PriceProfile priceProfileCreated;
	private boolean success;

	public CostPoliciesCreateServiceTest(String name, PriceUnit priceUnit, Double value, String currency,
			boolean success) {
		// random name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		name = prefix + "-" + name;
		this.priceProfile = new PriceProfile();
		priceProfile.setName(name);
		priceProfile.setPriceUnit(priceUnit);
		priceProfile.setValue(value);
		priceProfile.setCurrency(currency);
		this.success = success;

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { { "costprofile", PriceUnit.HOURLY, 1.00, "$", false },
				{ "costprofile", PriceUnit.MONTHLY, 1.00, "$", false },
				{ "costprofile", PriceUnit.ONE_TIME, 1.00, "$", false }

		});
	}

	@Before
	public void setUp() throws Exception {
		service = ServiceFactory.buildCostPoliciesService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	@Test
	public void testCreate() {
		logger.info("Price profile by name [{}]", priceProfile.getName());
		ResponseEntity<PriceProfile> response = service.create(priceProfile);
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}
		if (response.getResults() != null) {
			this.priceProfileCreated = response.getResults();
			logger.info("Create cost profile Successful..");
		}
		assertNotNull(response);
		assertNotNull(response.isErrors());
		assertEquals(priceProfile.getName(), priceProfileCreated.getName());

	}

	@After
	public void cleanUp() {
		if (priceProfileCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = service.delete(priceProfileCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error user deletion: [{}] ", message.getMessageText());
			}
		}
	}

}
