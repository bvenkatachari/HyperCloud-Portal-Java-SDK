package io.dchq.sdk.core.vlan;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vlan.VirtualNetwork;
import com.dchq.schema.beans.one.vpc.VirtualPrivateCloud;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.VirtualNetworkService;


/**
 * 
 * @author Jagdeep Jain
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class VirtualNetworkEntitledServiceTest extends AbstractServiceTest {
	
	private VirtualNetworkService vlanService;
	private VirtualNetwork virtualNetwork;
	private VirtualNetwork VirtualNetworkCreated;
	
	boolean sussess;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mints
	
	public VirtualNetworkEntitledServiceTest(String name, String driver, EntitlementType entitlementType, String vlanId, boolean success)
	{
		String prifix = RandomStringUtils.randomAlphabetic(3);
		if (name != null && !name.isEmpty()) {
			name = name + "-" + prifix;
		}
		virtualNetwork = new VirtualNetwork();
		virtualNetwork.setName(name);
		virtualNetwork.setDriver(driver);
		virtualNetwork.setEntitlementType(entitlementType);
		virtualNetwork.setVlanId(vlanId);
		this.sussess = success;
		
	}
	@Before
	public void setUp()
	{
		vlanService = ServiceFactory.buildVirtualNetworkService(rootUrl, cloudadminusername, cloudadminpassword);
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		
		return Arrays.asList(new Object[][]{ 
			{"testvlan", "402881875cf281ee015cf5c9f7ff05d0", EntitlementType.OWNER, "21" , true},
			{"testvlan1", "402881875cf281ee015cf5c9f7ff05d0", EntitlementType.PUBLIC, "50" , true},
			{"testvlan2", "402881875cf281ee015cf5c9f7ff05d0", EntitlementType.CUSTOM, "12" , true}
		});
	}
	@Ignore
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
			while (VirtualNetworkCreated.getStatus().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime)) {
				try {
					Thread.sleep(10000);
					resultResponse = vlanService.findById(VirtualNetworkCreated.getId());
					Assert.assertEquals(false, resultResponse.isErrors());
					Assert.assertNotNull(resultResponse.getResults());
					this.VirtualNetworkCreated = resultResponse.getResults();
				} catch (InterruptedException e) {
					// ignore
				}

			}

			if (VirtualNetworkCreated.getEntitlementType().equals(EntitlementType.OWNER)) {
				ResponseEntity<VirtualNetwork> resultResponse1 = vlanService.findById(VirtualNetworkCreated.getId());
				for (Message message : resultResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) resultResponse.isErrors()).toString());
				assertNotNull(resultResponse1.getResults());
				assertNotNull(resultResponse1.getResults().getId());
				assertEquals(VirtualNetworkCreated.getId(), resultResponse1.getResults().getId());
				
			} else if (VirtualNetworkCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				
				ResponseEntity<VirtualNetwork> resultResponse1 = vlanService.findById(VirtualNetworkCreated.getId());
				for (Message message : resultResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) resultResponse.isErrors()).toString());
				assertNotNull(resultResponse1.getResults());
				assertNotNull(resultResponse1.getResults().getId());
				assertEquals(VirtualNetworkCreated.getId(), resultResponse1.getResults().getId());

			} else if (VirtualNetworkCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
				
				ResponseEntity<VirtualNetwork> resultResponse1 = vlanService.findById(VirtualNetworkCreated.getId());
				for (Message message : resultResponse.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) resultResponse.isErrors()).toString());
				assertNotNull(resultResponse1.getResults());
				assertNotNull(resultResponse1.getResults().getId());
				assertEquals(VirtualNetworkCreated.getId(), resultResponse1.getResults().getId());

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
			ResponseEntity<VirtualNetwork> responseDelete = vlanService.delete(VirtualNetworkCreated.getId());
			Assert.assertEquals(false, responseDelete.isErrors());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error vlan deletion: [{}] ", message.getMessageText());
			}
		}
	}




}
