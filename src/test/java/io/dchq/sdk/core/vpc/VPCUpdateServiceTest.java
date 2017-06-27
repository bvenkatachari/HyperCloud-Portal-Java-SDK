package io.dchq.sdk.core.vpc;

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
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vpc.VirtualPrivateCloud;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.VPCService;

/**
 *
 * @author Jagdeep Jain
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class VPCUpdateServiceTest extends AbstractServiceTest {
	
	private VPCService vpcService;

	VirtualPrivateCloud createVPC;
	VirtualPrivateCloud createdVPC;
	VirtualPrivateCloud updatedVpc;
	boolean sussess;
	
	public VPCUpdateServiceTest(String vpcName, String providerId, EntitlementType entitlementType, String ipv4Cidr, String description,boolean success) {
		String prifix = RandomStringUtils.randomAlphabetic(3);

		if (vpcName != null && !vpcName.isEmpty()) {
			vpcName = vpcName + "-" + prifix;
		}
		createVPC = new VirtualPrivateCloud();
		createVPC.setName(vpcName);
		createVPC.setEntitlementType(entitlementType);
		createVPC.setIpv4Cidr(ipv4Cidr);
		NameEntityBase entity = new NameEntityBase();
		entity.withId(providerId);
		createVPC.setProvider(entity);
		createVPC.setDescription(description);
		this.sussess = success;
	}
	@Before
	public void setUp()
	{
		vpcService = ServiceFactory.buildVPCService(rootUrl, cloudadminusername, cloudadminpassword);
	}
	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][]{ 
			{"testvpc", "8a818a105c83f42a015c83fd71240014", EntitlementType.OWNER, "10.0.0.0/24", "descriptions test" , true},
			{"testvpcc", "8a818a105c83f42a015c83fd71240014", EntitlementType.PUBLIC, "10.0.0.0/24", "descriptions test" , true},
			{"testvpccc", "8a818a105c83f42a015c83fd71240014", EntitlementType.CUSTOM, "10.0.0.0/24", "descriptions test" , true}
		});
	}
	@Ignore
	@Test
	public void updateTest()
	{

		logger.info("Create VPC name[{}] ", createVPC.getName());
		ResponseEntity<VirtualPrivateCloud> resultResponse = vpcService.create(createVPC);
		Assert.assertNotNull(resultResponse);

		for (Message msg : resultResponse.getMessages()) {
			logger.warn("Error [{}]  " + msg.getMessageText());
		}
		if (this.sussess) {
			Assert.assertEquals(false, resultResponse.isErrors());
			Assert.assertNotNull(resultResponse.getResults());

			if (resultResponse.getResults() != null && !resultResponse.isErrors()) {
				this.createdVPC = resultResponse.getResults();
				logger.info("Create VPC Successful..");
			}
			createVPC.setName(createVPC.getName()+"-updated");
			ResponseEntity<VirtualPrivateCloud> resultFindResponse = vpcService.update(createVPC);
			
			Assert.assertNotNull(resultFindResponse);
			Assert.assertEquals(false, resultFindResponse.isErrors());
			
			if(resultFindResponse.getResults() != null && !resultFindResponse.isErrors())
			{
				this.updatedVpc = resultFindResponse.getResults();
				logger.info("Create VPC Successful..");
			}
			
			Assert.assertEquals(updatedVpc.getName(), createdVPC.getName());

		} else {

			Assert.assertEquals(true, resultResponse.isErrors());
			Assert.assertEquals(null, resultResponse.getResults());
		}
	
	}
	@After
	public void cleanUp()
	{
		if(this.createdVPC !=null)
		{
			logger.info("cleaning up...");
			ResponseEntity<VirtualPrivateCloud> responseDelete = vpcService.delete(createdVPC.getId());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error volume deletion: [{}] ", message.getMessageText());
			}
		}
	}

}
