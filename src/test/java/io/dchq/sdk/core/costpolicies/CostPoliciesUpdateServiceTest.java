package io.dchq.sdk.core.costpolicies;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class CostPoliciesUpdateServiceTest extends AbstractServiceTest {

	private CostPoliciesService service;
	private PriceProfile priceProfile;
	private PriceProfile priceProfileCreated;
	private boolean success;

	@Before
	public void setUp() throws Exception {
		service = ServiceFactory.buildCostPoliciesService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	public CostPoliciesUpdateServiceTest(String name, PriceUnit priceUnit, Double value, String currency,
			boolean success) {
		// random name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		name = prefix + name;
		this.priceProfile = new PriceProfile();
		priceProfile.setName(name);
		priceProfile.setPriceUnit(priceUnit);
		priceProfile.setValue(value);
		priceProfile.setCurrency(currency);
		this.success = success;

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { { "costprofile", PriceUnit.HOURLY, 1.00, "$", true },
				{ "costprofile", PriceUnit.MONTHLY, 1.00, "$", true },
				{ "costprofile", PriceUnit.ONE_TIME, 1.00, "$", true }

		});
	}

	@Test
	public void testUpdate() {
		try {

			logger.info("Create Price profile name as [{}] ", priceProfile.getName());
			ResponseEntity<PriceProfile> response = service.create(priceProfile);
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			if (success) {

				assertNotNull(response);
				assertEquals(false, response.isErrors());

				if (response.getResults() != null && !response.isErrors()) {
					this.priceProfileCreated = response.getResults();
					logger.info("Create Price profile sccessful..");
				}

				String updatedName = this.priceProfileCreated.getName() + "_updated";
				this.priceProfileCreated.setName(updatedName);

				// Updating Network ACL Name
				logger.info("Updating Network ACL name with [{}]", updatedName);
				response = service.update(this.priceProfileCreated);

				for (Message message : response.getMessages()) {
					logger.warn("Error while Update request  [{}] ", message.getMessageText());

				}

				assertNotNull(response);

				if (!response.isErrors()) {
					assertNotNull(response.getResults());
					assertEquals(response.getResults().getName(), this.priceProfileCreated.getName());
				}
			} else {
				assertEquals(null, response.getResults());
				assertEquals(true, response.isErrors());
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			fail(e.getMessage());
		}

	}

	@After
	public void cleanUp() {

		if (this.priceProfileCreated != null) {
			logger.info("cleaning up Price profile...");
			ResponseEntity<?> response = service.delete(this.priceProfileCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error Network ACL deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
