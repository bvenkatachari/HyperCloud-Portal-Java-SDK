package io.dchq.sdk.core.costpolicies;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
public class CostPoliciesFindAllServiceTest extends AbstractServiceTest {

	private CostPoliciesService service;
	private PriceProfile priceProfile;
	private PriceProfile priceProfileCreated;
	private boolean success;

	@Before
	public void setUp() throws Exception {
		service = ServiceFactory.buildCostPoliciesService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	private int countBeforeCreate = 0, countAfterCreate = 0;

	public CostPoliciesFindAllServiceTest(String name, PriceUnit priceUnit, Double value, String currency,
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
		return Arrays.asList(new Object[][] { { "costprofile", PriceUnit.HOURLY, 1.00, "$", true },
				{ "costprofile", PriceUnit.MONTHLY, 1.00, "$", true },
				{ "costprofile", PriceUnit.ONE_TIME, 1.00, "$", true }

		});
	}

	public int testPriceProfilePosition(String id) {
		ResponseEntity<List<PriceProfile>> response = service.findAll(0, 500);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}]  " + message.getMessageText());
		}
		assertNotNull(response);
		assertNotNull(response.isErrors());
		assertEquals(false, response.isErrors());
		int position = 0;
		if (id != null) {
			for (PriceProfile obj : response.getResults()) {
				position++;
				if (obj.getId().equals(id)) {
					logger.info("  Object Matched in FindAll {}  at Position : {}", id, position);
					assertEquals("Recently Created Object is not at Positon 1 :" + obj.getId(), 1, position);
				}
			}
		}
		logger.info(" Total Number of Objects :{}", response.getResults().size());
		return response.getResults().size();
	}

	@Test
	public void createFindAll() {
		try {

			countBeforeCreate = testPriceProfilePosition(null);

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
					logger.info("Create Price profile sccessfull..");
				}

				logger.info("FindAll Price profile by Id [{}]", this.priceProfileCreated.getId());
				this.countAfterCreate = testPriceProfilePosition(this.priceProfileCreated.getId());
				assertEquals(countBeforeCreate + 1, countAfterCreate);

			} else {

				for (Message message : response.getMessages()) {
					logger.warn("Error while Create request  [{}] ", message.getMessageText());
				}

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
				logger.warn("Error Price profile deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
