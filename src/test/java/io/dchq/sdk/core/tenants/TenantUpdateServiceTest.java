package io.dchq.sdk.core.tenants;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.security.Tenant;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.TenantService;

/**
 * 
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class TenantUpdateServiceTest extends AbstractServiceTest {

	private TenantService tenantService;

	private Tenant tenant;
	private Tenant tenantCreated;

	@org.junit.Before
	public void setUp() throws Exception {
		tenantService = ServiceFactory.buildTenantService(rootUrl, cloudadminusername, cloudadminpassword);

	}

	public TenantUpdateServiceTest(String name, String contactName, String email, String contactPhone) {

		String prefix = RandomStringUtils.randomAlphabetic(3);
		name = name + " " + prefix;

		tenant = new Tenant();
		tenant.setName(name);
		tenant.setContactName(contactName);
		tenant.setEmail(email);
		tenant.setContactPhone(contactPhone);
		tenant.setContactEmail(email);
		tenant.setPassword(RandomStringUtils.randomAlphabetic(12));
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "Tenant", "TenantName", "tenant@hypergrid.com", "9898989898" } });
	}

	@Ignore
	@org.junit.Test
	public void testUpdate() throws Exception {

		logger.info("Create Tenant with Tenant Name [{}]", tenant.getName());
		ResponseEntity<Tenant> response = tenantService.create(tenant);

		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}

		assertNotNull(response);
		assertNotNull(response.isErrors());
		this.tenantCreated = response.getResults();
		assertNotNull(response.getResults().getId());

		assertEquals(tenant.getName(), tenantCreated.getName());
		assertEquals(tenant.getContactName(), tenantCreated.getContactName());
		assertEquals(tenant.getContactPhone(), tenantCreated.getContactPhone());
		assertEquals(tenant.getContactEmail(), tenantCreated.getContactEmail());
		
		String modifiedName = tenantCreated.getName() + "_Update";
		// updating name
		tenantCreated.setName(modifiedName);
		logger.info("Update Tenant with Name [{}]", tenantCreated.getName());
		response = tenantService.update(tenantCreated);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}] ", message.getMessageText());
		}
		if (!response.isErrors() && response.getResults() != null) {
			assertEquals(tenantCreated.getName(), response.getResults().getName());
		}


	}

	@After
	public void cleanUp() {
		logger.info("cleaning up Tenant...");

		if (tenantCreated != null) {
			ResponseEntity<Tenant> deleteResponse = tenantService.delete(tenantCreated.getId());

			for (Message m : deleteResponse.getMessages()) {
				logger.warn("[{}]", m.getMessageText());
			}
		}
	}
}
