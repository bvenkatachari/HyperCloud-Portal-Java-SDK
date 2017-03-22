/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dchq.sdk.core.providers;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.blueprint.AccountType;
import com.dchq.schema.beans.one.blueprint.RegistryAccount;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.RegistryAccountService;
import io.dchq.sdk.core.ServiceFactory;

/**
 * @author Abedeen
 * @updater Jagdeep Jain
 * @since 1.0
 */

/**
 * Cloud Providers: Find
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class CloudProviderFindServiceTest extends AbstractServiceTest {
	
    private RegistryAccountService registryAccountService;
    private RegistryAccount registryAccount;
    private boolean success;
    private RegistryAccount registryAccountCreated;
    private RegistryAccount registryAccountUpdated;

    public CloudProviderFindServiceTest (
    		// below fields are for Rackspace, Amazon, Digital Ocean, Google Cloud, Aliyun
    		AccountType accountType,
    		String accountName, 
    		String testUsername, // also corresponds to application client id on UI
    		String apiKey, // also corresponds to password, application client secret id on UI
    		
    		// additional fields for Microsoft Azure
    		String subscriptionId, // corresponds to email in API call
    		String TenantId, // corresponds to region in API call
    		
    		// additional field for IBM Softlayer
    		String domainName, // corresponds to groupName in API call
 
    		// additional fields for private cloud
    		String vmDestination, // corresponds to hardwareId in API call
    		String template, // corresponds to imageId in API call
    		
    		// additional field for volume provider
    		String opts,
    		Integer size,
    		
    		boolean success
    		) 
	{
		this.registryAccount = new RegistryAccount().withName(accountName).withUsername(testUsername)
				.withPassword(apiKey).withAccountType(accountType);
		this.registryAccount.setRegion(TenantId);
		this.registryAccount.setEmail(subscriptionId);
		this.registryAccount.setGroupName(domainName);
		this.registryAccount.setHardwareId(vmDestination);
		this.registryAccount.setImageId(template);
		this.registryAccount.setOpts(opts);
		this.registryAccount.setSizeLimit(size);
		this.success = success;
	}

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{

				// public clouds
				{ AccountType.RACKSPACE, "Rackspace US 2 testAccount", "dchqinc", "apiKey", null, null, null, null,
						null, null, null, false },
				// Negative test cases 
				{ AccountType.RACKSPACE, "", "dchqinc", "apiKey", null, null, null, null,
							null, null, null, true },
				{ AccountType.RACKSPACE, "Rackspace US 2 testAccount", "", "apiKey", null, null, null, null,
								null, null, null, true },
				{ AccountType.RACKSPACE, "Rackspace US 2 testAccount", "dchqinc", "", null, null, null, null,
									null, null, null, true },
				
				{ AccountType.AWS_EC2, "Amazon 1 EC2 testAccount", "dchqinc", "apiKey", null, null, null, null, null,
						null, null, false },
				//Negative test cases 
				{ AccountType.DIGITALOCEAN, "Digital Ociean testAccount", "dchqinc", "apiKey", null, null, null, null,
						null, null, null, true },
				{ AccountType.AWS_EC2, "", "dchqinc", "apiKey", null, null, null, null, null,
							null, null, true },
				{ AccountType.AWS_EC2, "Amazon 1 EC2 testAccount", "", "apiKey", null, null, null, null, null,
								null, null, true },
				{ AccountType.AWS_EC2, "Amazon 1 EC2 testAccount", "dchqinc", "", null, null, null, null, null,
									null, null, true },
		
				
				// TODO: Seems to be a bug need to verify
				{ AccountType.GOOGLE_COMPUTE_ENGINE, "Google Cloud testAccount", "dchqinc", "password", null, null,
						null, null, null, null, null, false },
				
				{ AccountType.ALICLOUD, "F ALICLOUD testAccount", "dchqinc", "password", null, null, null, null, null,
						null, null, false },
				// Negative test cases
				{ AccountType.ALICLOUD, "", "dchqinc", "password", null, null, null, null, null,
							null, null, true },
				{ AccountType.ALICLOUD, "F ALICLOUD testAccount", "", "password", null, null, null, null, null,
								null, null, true },
				{ AccountType.ALICLOUD, "F ALICLOUD testAccount", "dchqinc", "", null, null, null, null, null,
									null, null, true },
				
				{ AccountType.MICROSOFT_AZURE, "Microsoft 5 Azure testAccount", "dchqinc", "password", "user@dchq.io",
						"tenantId", null, null, null, null, null, false },
				//Negative test cases
				{ AccountType.MICROSOFT_AZURE, "", "dchqinc", "password", "user@dchq.io",
							"tenantId", null, null, null, null, null, true },
				{ AccountType.MICROSOFT_AZURE, "Microsoft 5 Azure testAccount", "", "password", "user@dchq.io",
								"tenantId", null, null, null, null, null, true },
				{ AccountType.MICROSOFT_AZURE, "Microsoft 5 Azure testAccount", "dchqinc", "", "user@dchq.io",
									"tenantId", null, null, null, null, null, true },
				{ AccountType.MICROSOFT_AZURE, "Microsoft 5 Azure testAccount", "dchqinc", "password", "",
										"tenantId", null, null, null, null, null, true },
				
				
				{ AccountType.SOFTLAYER, "IBM Softlayer testAccount", "dchqinc", "password", null, null,
						"http://dchq.co.in", null, null, null, null, false },
				// Negative test cases
				{ AccountType.SOFTLAYER, "", "dchqinc", "password", null, null,
							"http://dchq.co.in", null, null, null, null, true },
				{ AccountType.SOFTLAYER, "IBM Softlayer testAccount", "", "password", null, null,
								"http://dchq.co.in", null, null, null, null, true },
				{ AccountType.SOFTLAYER, "IBM Softlayer testAccount", "dchqinc", "", null, null,
									"http://dchq.co.in", null, null, null, null, true },

				// private cloud
				{ AccountType.OPENSTACK, "G Openstack testAccount", "dchqinc", "password", null, null,
						"http://dchq.co.in", null, null, null, null, false },
				// Negative test cases
				{ AccountType.OPENSTACK, "", "dchqinc", "password", null, null,
							"http://dchq.co.in", null, null, null, null, true },
				{ AccountType.OPENSTACK, "G Openstack testAccount", "", "password", null, null,
								"http://dchq.co.in", null, null, null, null, true },
				{ AccountType.OPENSTACK, "G Openstack testAccount", "dchqinc", "", null, null,
									"http://dchq.co.in", null, null, null, null, true },
			
				
				//{ AccountType.VSPHERE, "VMware vSphere testAccount", "dchqinc", "password", null, null,
				//		"http://dchq.co.in", null, null, null, null, false },
				{ AccountType.HYPER_GRID, "Hypergrid Cloud testAccount", "dchqinc", "password", null, null,
						"http://dchq.co.in", "hardwareId", "templateId", null, null, false },
				// Negative test cases
				{ AccountType.HYPER_GRID, "", "dchqinc", "password", null, null,
							"http://dchq.co.in", "hardwareId", "templateId", null, null, true },
				{ AccountType.HYPER_GRID, "Hypergrid Cloud testAccount", "", "password", null, null,
								"http://dchq.co.in", "hardwareId", "templateId", null, null, true },
				{ AccountType.HYPER_GRID, "Hypergrid Cloud testAccount", "dchqinc", "", null, null,
									"http://dchq.co.in", "hardwareId", "templateId", null, null, true },
				
				
				{ AccountType.HYPER_V, "Microsoft Hyper-V testAccount", "dchqinc", "password", null, null,
						"http://dchq.co.in", "hardwareId", "templateId", null, null, false },
				// Negative test cases
				{ AccountType.HYPER_V, "", "dchqinc", "password", null, null,
							"http://dchq.co.in", "hardwareId", "templateId", null, null, true },
				{ AccountType.HYPER_V, "Microsoft Hyper-V testAccount", "", "password", null, null,
								"http://dchq.co.in", "hardwareId", "templateId", null, null, true },
				{ AccountType.HYPER_V, "Microsoft Hyper-V testAccount", "dchqinc", "", null, null,
									"http://dchq.co.in", "hardwareId", "templateId", null, null, true },
				{ AccountType.HYPER_V, "Microsoft Hyper-V testAccount", "dchqinc", "password", null, null,
										"", "hardwareId", "templateId", null, null, true },

				// Docker Registry
				{ AccountType.DOCKER_REGISTRY, "Docker Registry testAccount", "dchqinc", "password", "user@dchq.io", null,
						"http://dchq.co.in", null, null, null, null, false },
				// Negative test cases
				{ AccountType.DOCKER_REGISTRY, "", "dchqinc", "password", "user@dchq.io", null,
							"http://dchq.co.in", null, null, null, null, true },
				{ AccountType.DOCKER_REGISTRY, "Docker Registry testAccount", "", "password", "user@dchq.io", null,
								"http://dchq.co.in", null, null, null, null, true },
				{ AccountType.DOCKER_REGISTRY, "Docker Registry testAccount", "dchqinc", "", "user@dchq.io", null,
									"http://dchq.co.in", null, null, null, null, true },
				{ AccountType.DOCKER_REGISTRY, "Docker Registry testAccount", "dchqinc", "password", "", null,
										"http://dchq.co.in", null, null, null, null, true },

				// Jenkins/Hudson
				{ AccountType.JENKINS, "Jenkins testAccount", "dchqinc", "password", null, null, "http://dchq.co.in",
						null, null, null, null, false },
				//Negative test cases
				{ AccountType.JENKINS, "", "dchqinc", "password", null, null, "http://dchq.co.in",
							null, null, null, null, false },
				{ AccountType.JENKINS, "Jenkins testAccount", "", "password", null, null, "http://dchq.co.in",
								null, null, null, null, false },
				{ AccountType.JENKINS, "Jenkins testAccount", "dchqinc", "", null, null, "http://dchq.co.in",
									null, null, null, null, false },

				// Credentials
				{ AccountType.CREDENTIALS, "Credentials testAccount", "dchqinc", "password", null, null, null, null,
						null, null, null, false },
				// Negative test cases
				{ AccountType.CREDENTIALS, "", "dchqinc", "password", null, null, null, null,
							null, null, null, true },
				{ AccountType.CREDENTIALS, "Credentials testAccount", "", "password", null, null, null, null,
								null, null, null, true },
				{ AccountType.CREDENTIALS, "Credentials testAccount", "dchqinc", "", null, null, null, null,
									null, null, null, true },
						

				// volume provider
				{ AccountType.VOLUME_PROVIDER, "Volume Provider testAccount", "dchqinc", "password", null, null,
						"http://dchq.co.in", "hardwareId", "templateId", "opts", 10, false },
				// Negative test cases
				{ AccountType.VOLUME_PROVIDER, "", "dchqinc", "password", null, null,
							"http://dchq.co.in", "hardwareId", "templateId", "opts", 10, true },
				{ AccountType.VOLUME_PROVIDER, "Volume Provider testAccount", "", "password", null, null,
								"http://dchq.co.in", "hardwareId", "templateId", "opts", 10, true },
				{ AccountType.VOLUME_PROVIDER, "Volume Provider testAccount", "dchqinc", "", null, null,
									"http://dchq.co.in", "hardwareId", "templateId", "opts", 10, true },
				{ AccountType.VOLUME_PROVIDER, "Volume Provider testAccount", "dchqinc", "password", null, null,
										"", "hardwareId", "templateId", "opts", 10, true },
				{ AccountType.VOLUME_PROVIDER, "Volume Provider testAccount", "dchqinc", "password", null, null,
											"http://dchq.co.in", "", "templateId", "opts", 10, true },
				{ AccountType.VOLUME_PROVIDER, "Volume Provider testAccount", "dchqinc", "password", null, null,
												"http://dchq.co.in", "hardwareId", "", "opts", 10, true },
				{ AccountType.VOLUME_PROVIDER, "Volume Provider testAccount", "dchqinc", "password", null, null,
													"http://dchq.co.in", "hardwareId", "templateId", "", 10, true }
        });
    }


    @Before
    public void setUp() throws Exception {
        registryAccountService = ServiceFactory.buildRegistryAccountService(rootUrl, username, password);
    }
    
    @Test
    public void testFind() throws Exception {
		ResponseEntity<RegistryAccount> response = registryAccountService.create(registryAccount);
    	
		if (!success) {
			logger.info("Create Registry Account with Name [{}]", registryAccount.getName());
			if (success) {
				logger.info("Expecting Error while Create Registry Account with Name [{}]", registryAccount.getName());
			}
			for (Message message : response.getMessages()) {
				logger.warn("Error [{}] ", message.getMessageText());
			}
			assertNotNull(response);
			this.registryAccountCreated = response.getResults();
			assertNotNull(response.getResults());
			assertNotNull(response.getResults().getId());
			logger.info(" Registry Account Created with Name [{}] and ID [{}]", registryAccountCreated.getName(),
					registryAccountCreated.getId());
			assertEquals("UserName is invalid, when compared with input UserName @ Creation Time ",
					registryAccount.getUsername(), registryAccountCreated.getUsername());
			assertEquals(registryAccount.getAccountType(), registryAccountCreated.getAccountType());
			// password should always be empty
			assertThat("Password is not Expected in Response. ", "password-hidden",
					is(registryAccountCreated.getPassword()));
			logger.info("Find by Id for Registry Account with Id [{}]", registryAccountCreated.getId());
			// find service call by getId
			response = registryAccountService.findById(registryAccountCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error while Finding Cloutprovider  [{}] ", message.getMessageText());
			}
			if (!response.isErrors() && response.getResults() != null) {
				registryAccountUpdated = response.getResults();
				assertEquals("UserName is invalid, when compared with input UserName @ Update Time ",
						registryAccountCreated.getUsername(), registryAccountUpdated.getUsername());
			}
		}
		else
		{
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
		}
    }

    @After
    public void cleanUp() {
		if (registryAccountCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = registryAccountService.delete(registryAccountCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error user deletion: [{}] ", message.getMessageText());
			}
		}
    }
}
