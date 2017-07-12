package io.dchq.sdk.core.vlan;

import static junit.framework.TestCase.assertNotNull;

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
public class VirtualNetworkSearchServiceTest extends AbstractServiceTest{
	

	
	private VirtualNetworkService vlanService;
	private VirtualNetwork virtualNetwork;
	private VirtualNetwork VirtualNetworkCreated;
	private VirtualNetwork findVlan;
	private String vlanSearchByName;
	
	boolean sussess;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mints
	
	public VirtualNetworkSearchServiceTest(String name, String driver, EntitlementType entitlementType, String vlanId, boolean success)
	{
		String prifix = RandomStringUtils.randomAlphabetic(3);
		if (name != null && !name.isEmpty()) {
			name = name + "-" + prifix;
			vlanSearchByName = prifix;
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
			{"testvlan", "2c9180865d312fc4015d3160f6230092", EntitlementType.OWNER, "21" , true},
			{"testvlan1", "2c9180865d312fc4015d3160f6230092", EntitlementType.PUBLIC, "50" , true},
			{"testvlan2sdsd", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "12" , true},
			{"", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "12" , false},
			{null, "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "12" , false},
			{"testvlan2", "asasasas", EntitlementType.CUSTOM, "12" , false},
			{"testvlan2", "", EntitlementType.CUSTOM, "12" , true},
			{"@@@@@@@@@@@@@@@@", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "12" , false},
			{"testvlan2testvlan2testvlan2testvlan2testvlan2testvlan2te"
					+ "stvlan2testvlan2testvlan2testvlan2testvlan2testv"
					+ "lan2testvlan2testvlan2testvlan2testvlan2testvlan2"
					+ "testvlan2testvlan2testvlan2testvlan2testvlan2testvlan"
					+ "2testvlan2testvlan2testvlan2testvlan2testvlan2testvlan"
					+ "2testvlan2testvlan2testvlan2testvlan2testvlan2testvlan2te"
					+ "stvlan2testvlan2testvlan2testvlan2testvlan2testvlan2testvl"
					+ "an2testvlan2testvlan2testvlan2testvlan2testvlan2testvlan2testvl"
					, "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "12" , false},
			{"testvlan2223232323", "2c9180865d312fc4015d3160f6230092", EntitlementType.CUSTOM, "" , false},
		});
	}
	
	@Test
	public void searchTest()
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
				logger.info("Create vlan Successful..");
			}
			logger.info("VLan status [{}]", VirtualNetworkCreated.getStatus().name());
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
			ResponseEntity<List<VirtualNetwork>> resultFindResponse = vlanService.search(vlanSearchByName,0,1);
			Assert.assertNotNull(resultFindResponse);
			Assert.assertEquals(false, resultFindResponse.isErrors());
			assertNotNull(resultFindResponse.getResults());
			
			Assert.assertEquals(1, resultFindResponse.getResults().size());
			VirtualNetwork searchedEntity = resultFindResponse.getResults().get(0);
			
			Assert.assertNotNull(resultFindResponse);
			Assert.assertEquals(false, resultFindResponse.isErrors());
			
			Assert.assertEquals(findVlan.getName(), searchedEntity.getName());
			Assert.assertEquals(findVlan.getDriver(), searchedEntity.getDriver());
			Assert.assertEquals(findVlan.getVlanId(), searchedEntity.getVlanId());

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
			// TODO delete not working
//			ResponseEntity<VirtualNetwork> responseDelete = vlanService.delete(VirtualNetworkCreated.getId());
//			Assert.assertEquals(false, responseDelete.isErrors());
//			for (Message message : responseDelete.getMessages()) {
//				logger.warn("Error vlan deletion: [{}] ", message.getMessageText());
//			}
		}
	}



}
