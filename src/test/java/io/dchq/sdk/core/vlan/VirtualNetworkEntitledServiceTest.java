package io.dchq.sdk.core.vlan;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.springframework.util.StringUtils;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vlan.VirtualNetwork;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.VirtualNetworkService;


/**
 * 
 * @author Jagdeep Jain
 * @Updater Saurabh Bhatia
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class VirtualNetworkEntitledServiceTest extends AbstractServiceTest {
	
	private VirtualNetworkService vlanService, vlanService2;
	private VirtualNetwork virtualNetwork;
	private VirtualNetwork VirtualNetworkCreated;
	
	boolean sussess;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mints
	
	public VirtualNetworkEntitledServiceTest(String name, String driver, EntitlementType entitlementType, String vlanId, boolean isprifix, 
			boolean isEntitlementTypeUser, String entitledUserId, boolean success)
	{
		String prifix = RandomStringUtils.randomAlphabetic(3);
		if (name != null && !name.isEmpty() && isprifix) {
			name = (name + prifix).toLowerCase();
		}
		virtualNetwork = new VirtualNetwork();
		virtualNetwork.setName(name);
		virtualNetwork.setDriver(driver);
		virtualNetwork.setEntitlementType(entitlementType);
		virtualNetwork.setVlanId(vlanId);
		
		if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
			UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
			List<UsernameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			virtualNetwork.setEntitledUsers(entiledUsers);
		} else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
			NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
			List<NameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			virtualNetwork.setEntitledUserGroups(entiledUsers);
		}
		
		this.sussess = success;
		
	}
	@Before
	public void setUp()
	{
		vlanService = ServiceFactory.buildVirtualNetworkService(rootUrl1, cloudadminusername, cloudadminpassword);
		vlanService2 = ServiceFactory.buildVirtualNetworkService(rootUrl1, username2, password2);
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		// driver id - "402881875cf281ee015cf5c9f7ff05d0" on Intesar machine
		
		return Arrays.asList(new Object[][]{ 
			{"testvlan", "2c9180865d312fc4015d3160f6230092", EntitlementType.OWNER, "501" , true, false, null, true},
		    {"testvlan", "2c9180865d312fc4015d3160f6230092", EntitlementType.PUBLIC, "502" , true, false, null, true},
			{"testvlan", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "513" , true,true, userId2,true},
			{"testvlan", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "519" , true,false, USER_GROUP,true},
			
			
		});
	}
	
	@Test
	public void createTest()
	{
		logger.info("Create vlan name[{}] ", virtualNetwork.getName());
		ResponseEntity<VirtualNetwork> resultResponse = vlanService.create(virtualNetwork);
		Assert.assertNotNull(resultResponse);

		for (Message msg : resultResponse.getMessages()) {
			logger.warn("Error [{}]  " + msg.getMessageText());
		}
		if (this.sussess) {
			Assert.assertEquals(false, resultResponse.isErrors());
			Assert.assertNotNull(resultResponse.getResults());

			if (resultResponse.getResults() != null && !resultResponse.isErrors()) {
				this.VirtualNetworkCreated = resultResponse.getResults();
				logger.info("Create VPC Successfully..");
			}
			logger.info("VLan status [{}]", VirtualNetworkCreated.getStatus().name());
			while (VirtualNetworkCreated.getStatus().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime)) {
				try {
					// wait for some time
					Thread.sleep(10000);
					resultResponse = vlanService.findById(VirtualNetworkCreated.getId());
					logger.info("VLan status [{}]", VirtualNetworkCreated.getStatus().name());
					Assert.assertEquals(false, resultResponse.isErrors());
					Assert.assertNotNull(resultResponse.getResults());
					this.VirtualNetworkCreated = resultResponse.getResults();
				} catch (InterruptedException e) {
					// ignore
				}

			}
			logger.info("VLan status [{}]", VirtualNetworkCreated.getStatus().name());
			Assert.assertEquals("LIVE", VirtualNetworkCreated.getStatus().name());
			if (VirtualNetworkCreated.getEntitlementType().equals(EntitlementType.OWNER)) {
				ResponseEntity<List<VirtualNetwork>> resultResponse1 = vlanService2.search(VirtualNetworkCreated.getName(), 0, 1);
				for (Message message : resultResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				assertNotNull(resultResponse1);
				assertNotNull(resultResponse1.isErrors());
				assertEquals(null, resultResponse1.getResults());
				
			} else if (VirtualNetworkCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				
				ResponseEntity<List<VirtualNetwork>> resultResponse1 = vlanService2.search(VirtualNetworkCreated.getName(), 0, 1);
				for (Message message : resultResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				assertNotNull(resultResponse1);
				assertNotNull(resultResponse1.isErrors());
				assertNotNull(resultResponse1.getResults());
				assertEquals(1, resultResponse1.getResults().size());

			} else if (VirtualNetworkCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
				
				ResponseEntity<List<VirtualNetwork>> resultResponse1 = vlanService2.search(VirtualNetworkCreated.getName(), 0, 1);
				for (Message message : resultResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				assertNotNull(resultResponse1);
				assertNotNull(resultResponse1.isErrors());
				assertNotNull(resultResponse1.getResults());
				assertEquals(1, resultResponse1.getResults().size());

			}

		} else {

			Assert.assertEquals(true, resultResponse.isErrors());
			Assert.assertEquals(null, resultResponse.getResults());
		}
	
	}
	@After 
	public void cleanUp()
	{
		if(this.VirtualNetworkCreated !=null)
		{
			logger.info("cleaning up...");
			ResponseEntity<VirtualNetwork> responseDelete = vlanService.update(VirtualNetworkCreated.getId(),"release/");
			//Assert.assertEquals(false, responseDelete.isErrors());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error vlan deletion: [{}] ", message.getMessageText());
			}
		}
	}




}
