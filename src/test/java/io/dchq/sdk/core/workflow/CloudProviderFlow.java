package io.dchq.sdk.core.workflow;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

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
*   Methods:
*    1) Test Connection
*/


public class CloudProviderFlow extends AbstractServiceTest {
	
	
	private RegistryAccountService registryAccountService;
    private RegistryAccount registryAccount;
    private boolean success;
    
    
    public CloudProviderFlow (){
    
    	this.success = true;
    	setUp();
    }
    	
    
    public void setUp() {
    	registryAccountService = ServiceFactory.buildRegistryAccountService(rootUrl1, cloudadminusername, cloudadminpassword);
		
	}
	
	public void testConnection() throws Exception{

		try {
			logger.info("Getting cloud provider's detail with id as [{}] ", computeProviderId);
			ResponseEntity<RegistryAccount> response = registryAccountService.findById(computeProviderId);
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
			throw e;
		}

	}
	
	
}
