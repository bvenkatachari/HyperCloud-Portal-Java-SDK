package io.dchq.sdk.core.smoke.testsuite;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.blueprint.RegistryAccount;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.RegistryAccountService;
import io.dchq.sdk.core.ServiceFactory;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class CloudProviderTest extends AbstractServiceTest {
	
	
	private RegistryAccountService registryAccountService;
    private RegistryAccount registryAccount;
    private String cloudProvider_Id = "";
    private boolean success;
    
    @org.junit.Before
	public void setUp() throws Exception {
    	registryAccountService = ServiceFactory.buildRegistryAccountService(rootUrl1, cloudadminusername, cloudadminpassword);
		
	}
    
    public CloudProviderTest (String cloudProvider_Id, boolean success){
    	
    	this.cloudProvider_Id = cloudProvider_Id;
    	this.success = success;
    }
    	
    
    @Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			//Cloud Provider Id (HCS), Flag     
			{ computeProviderId, true }
			});
	}
	
	@Ignore
	@Test
	public void testConnection() {

		try {
			logger.info("Getting cloud provider's detail with id as [{}] ", cloudProvider_Id);
			ResponseEntity<RegistryAccount> response = registryAccountService.findById(cloudProvider_Id);
			if (success) {
				for (Message message : response.getMessages()) {
					logger.warn("Error while get request  [{}] ", message.getMessageText());
				}

				assertNotNull(response);
				assertEquals(false, response.isErrors());

				if (response.getResults() != null && !response.isErrors()) {
					this.registryAccount = response.getResults();
					logger.info("Getting cloud provider details Successful..");
				}

				if (this.registryAccount != null) {
					ResponseEntity<String> testresponse = registryAccountService.testConnection(this.registryAccount);
					
					assertNotNull(testresponse);
					assertEquals(false, testresponse.isErrors());
					
					if (testresponse.getResults() != null && !testresponse.isErrors()) {
						
						String result = testresponse.getResults();
						assertEquals("Connection Successful!", result);
					}
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
	
	
	
}
