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
import java.util.HashSet;
import java.util.Set;

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
import com.dchq.schema.beans.one.base.Visibility;
import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.blueprint.BlueprintType;
import com.dchq.schema.beans.one.price.PriceProfile;
import com.dchq.schema.beans.one.price.PriceUnit;
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.BlueprintService;
import io.dchq.sdk.core.CostPoliciesService;
import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class BlueprintCostProfileServiceTest extends AbstractServiceTest {

	private BlueprintService blueprintService;
	private Blueprint bluePrint;
	private boolean error;
	private Blueprint bluePrintCreated;

	private CostPoliciesService costPoliciesService;
	private PriceProfile priceProfileCreated;

	public BlueprintCostProfileServiceTest(String blueprintName, BlueprintType blueprintType, String description,
			String composeVersion, String yaml, Visibility visible, EntitlementType entitlementType, boolean success) {

		String suffix = RandomStringUtils.randomAlphabetic(3);
		if (blueprintName != null && !blueprintName.isEmpty()) {
			blueprintName = blueprintName + "" + suffix;
		}

		bluePrint = new Blueprint();
		bluePrint.setName(blueprintName);
		bluePrint.setBlueprintType(blueprintType);
		bluePrint.setDescription(description);
		bluePrint.setComposeVersion(composeVersion);
		bluePrint.setYml(yaml);
		bluePrint.setVisibility(visible);
		bluePrint.setEntitlementType(entitlementType);
		bluePrint.setInactive(false);

		this.error = success;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {

				{ "Cost_Profile_Blueprint", BlueprintType.DOCKER_COMPOSE, "Cost Profile Blueprint", "V1",
						"LB:\n image: nginx:latest", Visibility.HIDE, EntitlementType.OWNER, false },
				{ "Cost_Profile_Blueprint", BlueprintType.DOCKER_COMPOSE, "Cost Profile Blueprint", "V1",
						"LB:\n image: nginx:latest", Visibility.HIDE, EntitlementType.PUBLIC, false },
				{ "Cost_Profile_Blueprint", BlueprintType.DOCKER_COMPOSE, "Cost Profile Blueprint", "V1",
						"LB:\n image: nginx:latest", Visibility.HIDE, EntitlementType.ALL_TENANT_USERS, false },
				{ "Cost_Profile_Blueprint", BlueprintType.VM_COMPOSE, "Cost Profile Blueprint", "V1",
						"Machine1: \n  name: hc-giz\n  group: hc\n  image: C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub1604HFT_DCHQ_Docker.vhdx\n  instanceType: cpu=1,memory=4GB,disk=20GB,generation=1\n  network: Compute vmSwitch\n  skipAgentInstall: true\n  password: password-hidden",
						Visibility.HIDE, EntitlementType.PUBLIC, false },
				{ "Cost_Profile_Blueprint", BlueprintType.VM_COMPOSE, "Cost Profile Blueprint", "V1",
							"Machine1: \n  name: hc-giz\n  group: hc\n  image: C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub1604HFT_DCHQ_Docker.vhdx\n  instanceType: cpu=1,memory=4GB,disk=20GB,generation=1\n  network: Compute vmSwitch\n  skipAgentInstall: true\n  password: password-hidden",
							Visibility.HIDE, EntitlementType.ALL_TENANT_USERS, false }

		});
	}

	@Before
	public void setUp() throws Exception {
		blueprintService = ServiceFactory.buildBlueprintService(rootUrl1, cloudadminusername, cloudadminpassword);
		costPoliciesService = ServiceFactory.buildCostPoliciesService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	@Test
	public void testCreate() throws Exception {

		// Add cost profile to Blueprint
		Set<PriceProfile> priceProfiles = new HashSet<>();

		priceProfiles.add(createCostProfiles());

		bluePrint.setPriceProfiles(priceProfiles);

		logger.info("Creating Blueprint with name [{}] " + bluePrint.getName());

		ResponseEntity<Blueprint> response = blueprintService.create(bluePrint);
		for (Message m : response.getMessages()) {
			logger.warn("[{}]", m.getMessageText());
		}
		if (response.getResults() != null) {
			bluePrintCreated = response.getResults();
		}

		assertNotNull(response);
		assertNotNull(response.isErrors());
		if (!error) {
			assertNotNull(response.getResults());
			assertNotNull(response.getResults().getId());
			Assert.assertEquals(bluePrint.getName(), bluePrintCreated.getName());
			Assert.assertEquals(bluePrint.getYml(), bluePrintCreated.getYml());
			assertNotNull(response.getResults().getCostPolicyExist());
			// Check cost
			Assert.assertEquals("$ 2.0", response.getResults().getCostPolicyExist());

		} else {
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
		}
	}

	private PriceProfile createCostProfiles() {

		PriceProfile priceProfile = new PriceProfile();
		priceProfile.setName("Blueprint_Cost_Profile");
		priceProfile.setPriceUnit(PriceUnit.MONTHLY);
		priceProfile.setValue(2.00);
		priceProfile.setCurrency("$");

		ResponseEntity<PriceProfile> response = costPoliciesService.create(priceProfile);
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}

		assertNotNull(response);
		assertNotNull(response.getResults().getId());

		if (response.getResults() != null) {
			this.priceProfileCreated = response.getResults();
			logger.info("Create cost profile Successful..");
		}

		assertEquals(priceProfile.getName(), priceProfileCreated.getName());

		return this.priceProfileCreated;

	}

	@After
	public void cleanUp() {
		if (bluePrintCreated != null) {
			logger.info("cleaning up blueprint...");
			ResponseEntity<?> response = blueprintService.delete(bluePrintCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error blueprint deletion: [{}] ", message.getMessageText());
			}
		}

		if (priceProfileCreated != null) {
			logger.info("cleaning up cost profiles...");
			ResponseEntity<?> response = costPoliciesService.delete(priceProfileCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error user deletion: [{}] ", message.getMessageText());
			}
		}
	}
}