package io.dchq.sdk.core.subnet;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;

import io.dchq.sdk.core.ServiceFactory;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SubnetUpdateServiceTest extends SubnetTest {


	@org.junit.Before
	public void setUp() throws Exception {
		subnetService = ServiceFactory.buildSubnetService(rootUrl, cloudadminusername, cloudadminpassword);
	}
	

	public SubnetUpdateServiceTest() 
	{
		
		
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { });
	}

	@Ignore
	@Test
	public void testUpdate() {
		try {
			
		} catch (Exception e) {
			// ignore
		}

	}

	@After
	public void cleanUp() {
		if (this.subnetCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = subnetService.delete(this.subnetCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error subnet deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
