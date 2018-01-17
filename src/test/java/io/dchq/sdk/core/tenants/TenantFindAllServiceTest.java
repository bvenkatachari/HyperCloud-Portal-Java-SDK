package io.dchq.sdk.core.tenants;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
public class TenantFindAllServiceTest extends AbstractServiceTest {

	private TenantService tenantService;

	private Tenant tenant;
	private Tenant tenantCreated;
	
	private int countBeforeCreate;
	private int countAfterCreate;

	@org.junit.Before
	public void setUp() throws Exception {
		tenantService = ServiceFactory.buildTenantService(rootUrl, cloudadminusername, cloudadminpassword);

	}

	public TenantFindAllServiceTest(String name, String contactName, String email, String contactPhone) {

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
	
	public int testTenantPosition(String id) {
		ResponseEntity<List<Tenant>> response = null;
		try {
			response = tenantService.findAll(0, 5000);
			for (Message message : response.getMessages()) {
				logger.warn("Error [{}]  " + message.getMessageText());
			}
			assertNotNull(response);
			assertNotNull(response.isErrors());
			assertEquals(false, response.isErrors());
			int position = 0;
			if (id != null) {
				for (Tenant obj : response.getResults()) {
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

	@Ignore
	@org.junit.Test
	public void testFindAll() throws Exception {

		this.countBeforeCreate = testTenantPosition(null);
		
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
		
		this.countAfterCreate = testTenantPosition(tenantCreated.getId());
		assertEquals(
				"Count of Find all Tenant between before and after create does not have diffrence of 1 for TenantId :"
						+ tenantCreated.getId(),
				countBeforeCreate + 1, countAfterCreate);

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
