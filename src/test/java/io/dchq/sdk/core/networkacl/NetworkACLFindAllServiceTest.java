package io.dchq.sdk.core.networkacl;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vpc.NetworkACL;

import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class NetworkACLFindAllServiceTest extends NetworkACLUtil {

	@org.junit.Before
	public void setUp() throws Exception {
		networkACLService = ServiceFactory.buildNetworkACLService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	private int countBeforeCreate = 0, countAfterCreate = 0;
	
	public NetworkACLFindAllServiceTest(String networkACLName, String subnet_Id, EntitlementType entitlementType, boolean isprifix, boolean success) {

		String postfix = RandomStringUtils.randomAlphabetic(3);
		if(isprifix){
		    networkACLName = networkACLName + postfix;
		}

		networkACL = new NetworkACL();
		networkACL.setName(networkACLName);
		networkACL.setEntitlementType(entitlementType);
		
		NameEntityBase subnet = new NameEntityBase();
		subnet.setId(subnet_Id);
		
		networkACL.setSubnet(subnet);

		this.success = success;

	}
	
	
	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			{ "networkACL", subnetId, EntitlementType.OWNER, true, true },
			{ "networkACL", subnetId, EntitlementType.PUBLIC, true, true },
			{ "networkACL", subnetId, EntitlementType.CUSTOM, true, true },
			{ "networkACL", "", EntitlementType.OWNER, true, false },
			{ "", "", EntitlementType.OWNER, false, false },
			{ "networkACL", null, EntitlementType.OWNER, true, false },
			/*
			 * N/W ACL gets created for the blank value & special character, but didn't list/search on UI.
			 * */
			//{ "@@@^%%*&*^networkACL", subnetId, EntitlementType.OWNER, true, false },
			//{ null, subnetId, EntitlementType.OWNER, false, false },
			//{ "", subnetId, EntitlementType.OWNER, false, false },
			//{ "@@@@@@@@@@@@@@@@@@@@@@@@", subnetId, EntitlementType.OWNER, false, false },
			{ "networkACL", "ssssssssssssssssssssssssss", EntitlementType.OWNER, true, false },
			});
	}
	
	public int testNetworkACLPosition(String id) {
		ResponseEntity<List<NetworkACL>> response = networkACLService.findAll(0, 500);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}]  " + message.getMessageText());
		}
		assertNotNull(response);
		assertNotNull(response.isErrors());
		assertEquals(false, response.isErrors());
		int position = 0;
		if (id != null) {
			for (NetworkACL obj : response.getResults()) {
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
			
			countBeforeCreate = testNetworkACLPosition(null);
			
			logger.info("Create Network ACL name as [{}] ", networkACL.getName());
			
			ResponseEntity<NetworkACL> response = networkACLService.create(networkACL);
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			
			if (success) {

				assertNotNull(response);
				assertEquals(false, response.isErrors());

				if (response.getResults() != null && !response.isErrors()) {
					this.networkACLCreated = response.getResults();
					logger.info("Create Network ACL sccessfull..");
				}

				
				logger.info("FindAll Network ACL by Id [{}]", this.networkACLCreated.getId());
				this.countAfterCreate = testNetworkACLPosition(this.networkACLCreated.getId());
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
		}

	}

	@After
	public void cleanUp() {

		if (this.networkACLCreated != null) {
			logger.info("cleaning up Network ACL...");
			ResponseEntity<?> response = networkACLService.delete(this.networkACLCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error Network ACL deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
