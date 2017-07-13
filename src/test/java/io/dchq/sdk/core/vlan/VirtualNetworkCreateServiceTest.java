package io.dchq.sdk.core.vlan;

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
public class VirtualNetworkCreateServiceTest extends AbstractServiceTest{
	
	private VirtualNetworkService vlanService;
	private VirtualNetwork virtualNetwork;
	private VirtualNetwork VirtualNetworkCreated;
	
	boolean sussess;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mints
	
	public VirtualNetworkCreateServiceTest(String name, String driver, EntitlementType entitlementType, String vlanId, boolean isprifix, boolean success)
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
		this.sussess = success;
		
	}
	@Before
	public void setUp()
	{
		vlanService = ServiceFactory.buildVirtualNetworkService(rootUrl1, cloudadminusername, cloudadminpassword);
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		// driver id - "402881875cf281ee015cf5c9f7ff05d0" on Intesar machine
		
		return Arrays.asList(new Object[][]{ 
			{"testvlan", "2c9180865d312fc4015d3160f6230092", EntitlementType.OWNER, "505" , true, true},
	/*		{"testvlan1", "2c9180865d312fc4015d3160f6230092", EntitlementType.PUBLIC, "506" , true, true},
			{"testvlan2", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "507" , true,true},
			
			{"", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "508" ,false, false},
			{null, "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "509" , false, false},
			{"testvlan2", "asasasas", EntitlementType.CUSTOM, "512" , true, false},
			{"testvlan2", "", EntitlementType.CUSTOM, "513" , true, true},
			{"@@@@@@@@@@@@@@@@", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "510" ,false, false},
			{"testvlan2testvlan2testvlan2testvlan2testvlan2testvlan2te"
					+ "stvlan2testvlan2testvlan2testvlan2testvlan2testv"
					+ "lan2testvlan2testvlan2testvlan2testvlan2testvlan2"
					+ "testvlan2testvlan2testvlan2testvlan2testvlan2testvlan"
					+ "2testvlan2testvlan2testvlan2testvlan2testvlan2testvlan"
					+ "2testvlan2testvlan2testvlan2testvlan2testvlan2testvlan2te"
					+ "stvlan2testvlan2testvlan2testvlan2testvlan2testvlan2testvl"
					+ "an2testvlan2testvlan2testvlan2testvlan2testvlan2testvlan2testvl"
					, "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "511" , true, false},
			{"testvlan2223232323", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "" , true, false}, */
		});
	}
	//@Ignore
	@Test
	public void createTest()
	{
		logger.info("Create vlan name[{}] ", virtualNetwork.getName());
		ResponseEntity<VirtualNetwork> resultResponse = vlanService.create(virtualNetwork);
		Assert.assertNotNull(resultResponse);
		
		Assert.assertNotNull(resultResponse);

		for (Message msg : resultResponse.getMessages()) {
			logger.warn("Error [{}]  " + msg.getMessageText());
		}
		if (this.sussess) {
			Assert.assertEquals(false, resultResponse.isErrors());
			Assert.assertNotNull(resultResponse.getResults());

			if (resultResponse.getResults() != null && !resultResponse.isErrors()) {
				this.VirtualNetworkCreated = resultResponse.getResults();
				logger.info("Create Vlan Successfully..");
			}
			logger.info("VLan state [{}]", VirtualNetworkCreated.getStatus().name());
			while(VirtualNetworkCreated.getStatus().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime))
			{
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
			Assert.assertEquals(VirtualNetworkCreated.getName(), virtualNetwork.getName());
			Assert.assertEquals(VirtualNetworkCreated.getDriver(), virtualNetwork.getDriver());
			Assert.assertEquals(VirtualNetworkCreated.getVlanId(), virtualNetwork.getVlanId());

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
			// TODO not working delete api
			ResponseEntity<VirtualNetwork> responseDelete = vlanService.delete(VirtualNetworkCreated.getId(),"release/");
			//Assert.assertNotNull(responseDelete);
			//Assert.assertEquals(false, responseDelete.isErrors());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error vlan deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
