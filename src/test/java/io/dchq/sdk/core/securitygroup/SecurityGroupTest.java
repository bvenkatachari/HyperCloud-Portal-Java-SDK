package io.dchq.sdk.core.securitygroup;

import org.apache.commons.lang3.RandomStringUtils;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vlan.VirtualNetwork;
import com.dchq.schema.beans.one.vpc.SecurityGroup;
import com.dchq.schema.beans.one.vpc.Subnet;
import com.dchq.schema.beans.one.vpc.VirtualPrivateCloud;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.SecurityGroupService;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.SubnetService;
import io.dchq.sdk.core.VPCService;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/
public class SecurityGroupTest extends AbstractServiceTest {

	//getVPC() not functional. Hence using already created VPC from UI.
	 public final String vpcId = "402881845c9458a6015c945ac24c0004";
	 //public final String subnetId = "402881875ced84c3015ced88edfa0009";
		
	//Create VPC
    VPCService vpcService;
	VirtualPrivateCloud createdVPC;
	
	//Create Subnet
	SubnetService subnetService;
	Subnet createdSubnet;
	
	//Create NetworkACL
	SecurityGroupService securityGroupService;
	SecurityGroup securityGroup;
	SecurityGroup securityGroupCreated;
	boolean success;
	
	
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

	 
	 public Subnet getSubnet(String subnetName, String vlanId, String ipv4Cidr, String dhcp, String fromIpRange,
				String toIpRange, String dnsServers, String vpcName, String providerId) {
		 
	        
	        subnetService = ServiceFactory.buildSubnetService(rootUrl, username, password);
	        
	        String postfix = RandomStringUtils.randomAlphabetic(3);
			subnetName = subnetName + postfix;
			
			logger.info("Create Subnet with Name [{}]", subnetName);
			
	        Subnet subnet = new Subnet();
	        subnet.setEntitlementType(EntitlementType.PUBLIC);
	        subnet.setName(subnetName);
	        
	      //createdVPC = getVPC(vpcName, providerId, ipv4Cidr);
			NameEntityBase vpc = new NameEntityBase();
			//vpc.setId(createdVPC.getId());
			vpc.setId(vpcId);
			subnet.setVpc(vpc);

			VirtualNetwork virtualNetwork = new VirtualNetwork();
			virtualNetwork.setId(vlanId);
			subnet.setVirtualNetwork(virtualNetwork);

			subnet.setIpv4Cidr(ipv4Cidr);
			subnet.setDhcp(dhcp);
			subnet.setFromIpRange(fromIpRange);
			subnet.setToIpRange(toIpRange);
			subnet.setDnsServers(dnsServers);


	        ResponseEntity<Subnet> responseEntity = subnetService.create(subnet);
	        if (responseEntity.isErrors())
	            logger.warn("Message from Server... {}", responseEntity.getMessages().get(0).getMessageText());


	        return responseEntity.getResults();

	    }
	
}
