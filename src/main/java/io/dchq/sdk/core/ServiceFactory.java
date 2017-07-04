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

package io.dchq.sdk.core;

/**
 * Factory class
 *
 * @author Intesar Mohammed
 * //@contributor Saurabh Bhatia
 * @since 1.0
 */
public class ServiceFactory {

    public static final BlueprintService buildBlueprintService(String baseURI, String username, String password) {
        // validate inputs
        return new BlueprintServiceImpl(baseURI, username, password);
    }

    public static final DockerServerService buildDockerServerService(String baseURI, String username, String password) {
        return new DockerServerServiceImpl(baseURI, username, password);
    }

    public static final BuildService buildBuildService(String baseURI, String username, String password) {
        return new BuildServiceImpl(baseURI, username, password);
    }

    public static final DataCenterService buildDataCenterService(String baseURI, String username, String password) {
        return new DataCenterServiceImpl(baseURI, username, password);
    }

    public static final AppService buildAppService(String baseURI, String username, String password) {
        return new AppServiceImpl(baseURI, username, password);
    }

    public static final PluginService buildPluginService(String baseURI, String username, String password) {
        return new PluginServiceImpl(baseURI, username, password);
    }

    public static final RegistryAccountService buildRegistryAccountService(String baseURI, String username, String password) {
        return new RegistryAccountServiceImpl(baseURI, username, password);
    }

    public static final UserGroupService builduserGroupService(String baseURI, String username, String password) {
        // validate inputs
        return new UserGroupServiceImpl(baseURI, username, password);
    }

    public static final ProfileService buildProfileService(String baseURI, String username, String password) {
        return new ProfileServiceImpl(baseURI, username, password);
    }

    public static final UserService buildUserService(String baseURI, String username, String password) {
        return new UserServiceImpl(baseURI, username, password);
    }

    public static final TenantService buildTenantService(String baseURI, String username, String password) {
        return new TenantServiceImpl(baseURI, username, password);
    }

    public static final NetworkService buildNetworkService(String baseURI, String username, String password) {
        return new NetworkServiceImpl(baseURI, username, password);
    }

    public static final DockerVolumeService buildDockerVolumeService(String baseURI, String username, String password) {
        return new DockerVolumeServiceImpl(baseURI, username, password);
    }
    
    public static final CostPoliciesService buildCostPoliciesService(String baseURI, String username, String password) {
        return new CostPoliciesServiceImpl(baseURI, username, password);
    }
    
    public static final QuotaPolicyService  buildQuotaPolicyService(String baseURI, String username, String password) {
        return new QuotaPolicyServiceImpl(baseURI, username, password);
    }
    
    public static final VPCService buildVPCService(String baseURL, String username, String password){
    	return new VPCServiceImpl(baseURL, username, password);
    }
    
    public static final SubnetService buildSubnetService(String baseURL, String username, String password){
    	return new SubnetServiceImpl(baseURL, username, password);
    }
    
    public static final NetworkACLService buildNetworkACLService(String baseURL, String username, String password){
    	return new NetworkACLServiceImpl(baseURL, username, password);
    }
    
    public static final SecurityGroupService buildSecurityGroupService(String baseURL, String username, String password){
    	return new SecurityGroupServiceImpl(baseURL, username, password);
    }
    
    public static final NetworkACLRuleService buildNetworkACLRuleService(String baseURL, String username, String password){
    	return new NetworkACLRuleServiceImpl(baseURL, username, password);
    }
    
    public static final SecurityGroupRuleService buildSecurityGroupRuleService(String baseURL, String username, String password){
    	return new SecurityGroupRuleServiceImpl(baseURL, username, password);
    }
    
    public static final VirtualNetworkService buildVirtualNetworkService(String baseURL, String username, String password){
    	return new VirtualNetworkServiceImpl(baseURL, username, password);
    }
}
