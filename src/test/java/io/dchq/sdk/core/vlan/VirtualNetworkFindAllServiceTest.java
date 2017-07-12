package io.dchq.sdk.core.vlan;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

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

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vlan.VirtualNetwork;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.VirtualNetworkService;

/**
 * 
 * @author Jagdeep Jain
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class VirtualNetworkFindAllServiceTest extends AbstractServiceTest{

	
	private VirtualNetworkService vlanService;
	private VirtualNetwork virtualNetwork;
	private VirtualNetwork VirtualNetworkCreated;
	private int countBeforeCreate = 0, countAfterCreate = 0;
	boolean sussess;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mints
	
	public VirtualNetworkFindAllServiceTest(String name, String driver, EntitlementType entitlementType, String vlanId, boolean success)
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
		// driver id - "402881875cf281ee015cf5c9f7ff05d0" on Intesar machine
		return Arrays.asList(new Object[][]{ 
			{"testvlan", "2c9180865d312fc4015d3160f6230092", EntitlementType.OWNER, "21" , true},
//			{"testvlan1", "2c9180865d312fc4015d3160f6230092", EntitlementType.PUBLIC, "50" , true},
//			{"testvlan2", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "12" , true},
//			{"", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "12" , false},
//			{null, "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "12" , false},
//			{"testvlan2", "asasasas", EntitlementType.CUSTOM, "12" , false},
//			{"testvlan2", "", EntitlementType.CUSTOM, "12" , true},
//			{"@@@@@@@@@@@@@@@@", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "12" , false},
//			{"testvlan2testvlan2testvlan2testvlan2testvlan2testvlan2te"
//					+ "stvlan2testvlan2testvlan2testvlan2testvlan2testv"
//					+ "lan2testvlan2testvlan2testvlan2testvlan2testvlan2"
//					+ "testvlan2testvlan2testvlan2testvlan2testvlan2testvlan"
//					+ "2testvlan2testvlan2testvlan2testvlan2testvlan2testvlan"
//					+ "2testvlan2testvlan2testvlan2testvlan2testvlan2testvlan2te"
//					+ "stvlan2testvlan2testvlan2testvlan2testvlan2testvlan2testvl"
//					+ "an2testvlan2testvlan2testvlan2testvlan2testvlan2testvlan2testvl"
//					, "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "12" , false},
//			{"testvlan2223232323", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "" , false},
		});
	}
	public int testVPCPosition(String id) {
		ResponseEntity<List<VirtualNetwork>> response = vlanService.findAll(0, 500);
		assertNotNull(response);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}]  " + message.getMessageText());
		}
		assertNotNull(response.isErrors());
		assertEquals(false, response.isErrors());
		int position = 0;
		if (id != null) {
			for (VirtualNetwork obj : response.getResults()) {
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
	public void findAllTest()
	{
		logger.info("Create vlan name[{}] ", virtualNetwork.getName());
		countBeforeCreate = testVPCPosition(null);
		ResponseEntity<VirtualNetwork> response = vlanService.create(virtualNetwork);
		Assert.assertNotNull(response);
		for (Message msg : response.getMessages()) {
			logger.warn("Error [{}]  " + msg.getMessageText());
		}
		
		if (this.sussess) {
			Assert.assertEquals(false, response.isErrors());
			Assert.assertNotNull(response.getResults());
			if (response.getResults() != null && !response.isErrors()) {
				this.VirtualNetworkCreated = response.getResults();
				logger.info("Create vlan Successful..");
			}
			logger.info("VLan status [{}]", VirtualNetworkCreated.getStatus().name());
			while(VirtualNetworkCreated.getStatus().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime))
			{
				try {
					// wait for some time
					Thread.sleep(10000);
					response = vlanService.findById(VirtualNetworkCreated.getId());
					logger.info("VLan state [{}]", VirtualNetworkCreated.getStatus().name());
					Assert.assertEquals(false, response.isErrors());
					Assert.assertNotNull(response.getResults());
					this.VirtualNetworkCreated = response.getResults();
				} catch (InterruptedException e) {
					// ignore
				}
			}
			Assert.assertEquals("LIVE", VirtualNetworkCreated.getStatus().name());
			logger.info("VLan status [{}]", VirtualNetworkCreated.getStatus().name());
			countAfterCreate = testVPCPosition(VirtualNetworkCreated.getId());
			assertEquals(countBeforeCreate + 1, countAfterCreate);

		} else {

			Assert.assertEquals(true, response.isErrors());
			Assert.assertEquals(null, response.getResults());
		}
	}
	@After 
	public void cleanUp()
	{
		if(this.VirtualNetworkCreated !=null)
		{
			logger.info("cleaning up...");
			// TODO delete not working
//			ResponseEntity<VirtualNetwork> responseDelete = vlanService.delete(VirtualNetworkCreated.getId());
//			Assert.assertEquals(false, responseDelete.isErrors());
//			for (Message message : responseDelete.getMessages()) {
//				logger.warn("Error vlan deletion: [{}] ", message.getMessageText());
//			}
		}
	}


}
