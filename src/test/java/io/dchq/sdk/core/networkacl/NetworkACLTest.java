package io.dchq.sdk.core.networkacl;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.NetworkACL;
import com.dchq.schema.beans.one.vpc.Subnet;
import com.dchq.schema.beans.one.vpc.VirtualPrivateCloud;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.NetworkACLService;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.SubnetService;
import io.dchq.sdk.core.VPCService;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/
public class NetworkACLTest extends AbstractServiceTest {

	//Create VPC
    VPCService vpcService;
	VirtualPrivateCloud createdVPC;
	
	//Create Subnet
	SubnetService subnetService;
	Subnet createdSubnet;
	
	//Create NetworkACL
	NetworkACLService networkACLService;
	NetworkACL networkACL;
	NetworkACL networkACLCreated;
	boolean success;
	
	
	 public VirtualPrivateCloud getVPC(String vpcName, boolean success) {
	        logger.info("Create VPC with Name [{}]", vpcName);
	        this.vpcService = ServiceFactory.buildVPCService(rootUrl, cloudadminusername, cloudadminpassword);
	        VirtualPrivateCloud vpc = new VirtualPrivateCloud();
	        vpc.setName(vpcName);

	        ResponseEntity<VirtualPrivateCloud> responseEntity = vpcService.create(vpc);
	        if (responseEntity.isErrors())
	            logger.warn("Message from Server... {}", responseEntity.getMessages().get(0).getMessageText());


	        return responseEntity.getResults();

	    }
	 
	 public Subnet getSubnet(String subnetName, boolean success) {
	        logger.info("Create Subnet with Name [{}]", subnetName);
	        this.subnetService = ServiceFactory.buildSubnetService(rootUrl, cloudadminusername, cloudadminpassword);
	        Subnet subnet = new Subnet();
	        subnet.setName(subnetName);

	        ResponseEntity<Subnet> responseEntity = subnetService.create(subnet);
	        if (responseEntity.isErrors())
	            logger.warn("Message from Server... {}", responseEntity.getMessages().get(0).getMessageText());


	        return responseEntity.getResults();

	    }
	
}
