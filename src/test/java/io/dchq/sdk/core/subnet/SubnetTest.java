package io.dchq.sdk.core.subnet;

import org.apache.commons.lang3.RandomStringUtils;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vpc.Subnet;
import com.dchq.schema.beans.one.vpc.VirtualPrivateCloud;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.SubnetService;
import io.dchq.sdk.core.VPCService;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */
public class SubnetTest extends AbstractServiceTest {

	//getVPC() not functional. Hence using already created VPC from UI.
	public final String vpcId = "402881845c9458a6015c945ac24c0004";
	// Create VPC
	VPCService vpcService;
	VirtualPrivateCloud createdVPC;

	// Create Subnet
	SubnetService subnetService;
	Subnet subnet;
	Subnet subnetCreated;
	boolean success;

    //Currently this method is not working. 
	public VirtualPrivateCloud getVPC(String vpcName, String providerId, String ipv4Cidr) {
		
		vpcService = ServiceFactory.buildVPCService(rootUrl, username, password);
		
		String postfix = RandomStringUtils.randomAlphabetic(3);
		vpcName = vpcName + postfix;
		
		logger.info("Create VPC with Name [{}]", vpcName);
		
		VirtualPrivateCloud vpc = new VirtualPrivateCloud();
		vpc.setName(vpcName);
		vpc.setEntitlementType(EntitlementType.PUBLIC);
		vpc.setIpv4Cidr(ipv4Cidr);
		NameEntityBase entity = new NameEntityBase();
		entity.withId(providerId);
		vpc.setProvider(entity);

		ResponseEntity<VirtualPrivateCloud> responseEntity = vpcService.create(vpc);
		if (responseEntity.isErrors())
			logger.warn("Message from Server... {}", responseEntity.getMessages().get(0).getMessageText());

		return responseEntity.getResults();

	}

}
