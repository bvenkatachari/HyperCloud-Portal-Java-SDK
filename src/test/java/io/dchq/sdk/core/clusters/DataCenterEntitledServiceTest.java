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
package io.dchq.sdk.core.clusters;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.provider.DataCenter;
import com.dchq.schema.beans.one.security.EntitlementType;
import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DataCenterService;
import io.dchq.sdk.core.ServiceFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

/**
 * Abstracts class for holding test credentials.
 *
 * @author Abedeen.
 * @updater Saurabh B.
 * @since 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DataCenterEntitledServiceTest extends AbstractServiceTest {

    private DataCenterService dataCenterService;
    private DataCenterService dataCenterService2, dataCenterService3;

    DataCenter dataCenter;
    DataCenter dataCenterCreated;
    boolean error;
    String validationMessage;

    @org.junit.Before
    public void setUp() throws Exception{
        dataCenterService = ServiceFactory.buildDataCenterService(rootUrl, username, password);
        dataCenterService2 = ServiceFactory.buildDataCenterService(rootUrl, username2, password2);
        dataCenterService3 = ServiceFactory.buildDataCenterService(rootUrl, username3, password3);
    }


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Cluster AA4","Weave", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.OWNER, true, userId2, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Weave", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.PUBLIC, true, userId2, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Weave", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.CUSTOM, true, userId2, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Weave", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.CUSTOM, false, USER_GROUP, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Docker Network", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.OWNER, true, userId2, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Docker Network", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.PUBLIC, true, userId2, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Docker Network", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.CUSTOM, true, userId2, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Docker Network", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.CUSTOM, false, USER_GROUP, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Docker Swarm", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.OWNER, true, userId2, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Docker Swarm", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.PUBLIC, true, userId2, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Docker Swarm", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.CUSTOM, true, userId2, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Docker Swarm", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.CUSTOM, false, USER_GROUP, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Docker - UCP", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.OWNER, true, userId2, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Docker - UCP", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.PUBLIC, true, userId2, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Docker - UCP", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.CUSTOM, true, userId2, "\nAll Input Values are normal. Malfunction in SDK",false},
                {"Cluster AA4","Docker - UCP", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced",
                        5, "1g",false,true, 4, EntitlementType.CUSTOM, false, USER_GROUP, "\nAll Input Values are normal. Malfunction in SDK",false},


        });
    }
    public DataCenterEntitledServiceTest(String clusterName, String networkType, String description,
                                       EntitlementType blueprintType, EntitlementType plugins, String capAdd,
                                       String networkPass, int cpuShares, String memoryLimit,
                                       boolean terminationProtection,boolean approvalEnforced,
                                       int maxContainerLimit, EntitlementType entitlementType, boolean isEntitlementTypeUser,
                                         String entitledUserId, String validationMessage, boolean error)

    {
        // random clustername
        if (clusterName == null){
            throw new IllegalArgumentException("ClusterName==null");
        }

        if (!clusterName.isEmpty()) {
            String prefix = RandomStringUtils.randomAlphabetic(3);
            clusterName = prefix + clusterName;
            clusterName = org.apache.commons.lang3.StringUtils.lowerCase(clusterName);
        }



        this.dataCenter = new DataCenter().withName(clusterName).withNetwork(networkType).withBlueprintEntitlementType(blueprintType).withPluginEntitlementType(plugins);
        this.dataCenter.setDescription(description);
        this.dataCenter.setCapAdd(capAdd);
        this.dataCenter.setNetworkPass(networkPass);
        this.dataCenter.setCpuShares(cpuShares);
        this.dataCenter.setMemLimit(memoryLimit);
        this.dataCenter.setTerminationProtection(terminationProtection);
        this.dataCenter.setApprovalEnforced(approvalEnforced);
        this.dataCenter.setMaxContainerLimit(maxContainerLimit);
        this.dataCenter.setEntitlementType(entitlementType);
        this.validationMessage = validationMessage;
        this.error=error;

        if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
            UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
            List<UsernameEntityBase> entiledUsers = new ArrayList<>();
            entiledUsers.add(entitledUser);
            this.dataCenter.setEntitledUsers(entiledUsers);
        } else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
            NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
            List<NameEntityBase> entiledUsers = new ArrayList<>();
            entiledUsers.add(entitledUser);
            this.dataCenter.setEntitledUserGroups(entiledUsers);
        }
        this.validationMessage=validationMessage;

    }

    // Test for Entitlement - 'Only Me', 'Everyone' & 'Custom' through Search Operation.
    @Ignore
    @Test
    public void testEntitledUserOwnerSearch() throws Exception {
        logger.info("Create Cluster [{}]", dataCenter.getName());
        ResponseEntity<DataCenter> response = dataCenterService.create(dataCenter);
        for (Message m : response.getMessages()) {
            logger.warn("[{}]", m.getMessageText());
        }
        if(response.getResults() != null){
            dataCenterCreated = response.getResults();
        }
        if (!error) {
            if (dataCenterCreated.getEntitlementType().equals(EntitlementType.OWNER) ) {
                ResponseEntity<List<DataCenter>> clusterSearchResponseEntity1 = dataCenterService2.search(dataCenter.getName(), 0, 1);
                for (Message message : clusterSearchResponseEntity1.getMessages()) {
                    logger.warn("Error while Search request  [{}] ", message.getMessageText());
                    }
                assertNotNull(clusterSearchResponseEntity1);
                assertNotNull(clusterSearchResponseEntity1.isErrors());
                assertNotNull(clusterSearchResponseEntity1.getResults());
                Assert.assertEquals(0, clusterSearchResponseEntity1.getResults().size());
            }

            else if (dataCenterCreated.getEntitlementType().equals(EntitlementType.PUBLIC) ) {
                ResponseEntity<List<DataCenter>> clusterSearchResponseEntity = dataCenterService2.search(dataCenter.getName(), 0, 1);
                for (Message message : clusterSearchResponseEntity.getMessages()) {
                    logger.warn("Error while Search request  [{}] ", message.getMessageText());
                }
                assertNotNull(clusterSearchResponseEntity);
                assertNotNull(clusterSearchResponseEntity.isErrors());
                Assert.assertEquals(1, clusterSearchResponseEntity.getResults().size());
            }
            else  if (dataCenterCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
                ResponseEntity<List<DataCenter>> clusterSearchResponseEntity = dataCenterService2
                        .search(dataCenter.getName(), 0, 1);
                for (Message message : clusterSearchResponseEntity.getMessages()) {
                    logger.warn("Error while Search request  [{}] ", message.getMessageText());
                }
                assertNotNull(clusterSearchResponseEntity);
                assertNotNull(clusterSearchResponseEntity.isErrors());
                Assert.assertEquals(1, clusterSearchResponseEntity.getResults().size());
            }
            else {
                Assert.fail("Entitlement Type Not supported: " + dataCenterCreated.getEntitlementType());
            }
        }
    }

    // Test for Entitlement - 'Only Me', 'Everyone' & 'Custom'  through Find by ID.
    @Ignore
    @Test
    public void testEntitledUserOwnerFindById() throws Exception {
        logger.info("Create Cluster [{}]", dataCenter.getName());
        ResponseEntity<DataCenter> response = dataCenterService.create(dataCenter);
        for (Message m : response.getMessages()) {
            logger.warn("[{}]", m.getMessageText());
        }
        if(response.getResults() != null){
            dataCenterCreated = response.getResults();
        }
        if (!error) {
            if (dataCenterCreated.getEntitlementType().equals(EntitlementType.OWNER)) {
                ResponseEntity<DataCenter> findbyIdResponse = dataCenterService2.findById(dataCenterCreated.getId());
                for (Message message : findbyIdResponse.getMessages()) {
                    logger.warn("Error while Find request  [{}] ", message.getMessageText());
                }
                Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
                assertNotNull(findbyIdResponse);
                assertNotNull(findbyIdResponse.isErrors());
                Assert.assertEquals(findbyIdResponse.getResults(), null);
            }

            else  if (dataCenterCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
                ResponseEntity<DataCenter> findbyIdResponse = dataCenterService2.findById(dataCenterCreated.getId());
                for (Message message : findbyIdResponse.getMessages()) {
                    logger.warn("Error while Find request  [{}] ", message.getMessageText());
                }
                Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
                assertNotNull(findbyIdResponse.getResults());
                Assert.assertEquals(dataCenterCreated.getId(), findbyIdResponse.getResults().getId());
            }

            else if (dataCenterCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
                ResponseEntity<DataCenter> findbyIdResponse = dataCenterService2.findById(dataCenterCreated.getId());
                for (Message message : findbyIdResponse.getMessages()) {
                    logger.warn("Error while Find request  [{}] ", message.getMessageText());
                }
                Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
                assertNotNull(findbyIdResponse.getResults());
                Assert.assertEquals(dataCenterCreated.getId(), findbyIdResponse.getResults().getId());
            }

            else {
                Assert.fail("Entitlement Type Not supported: " + dataCenterCreated.getEntitlementType());

            }
        }
    }

    // Negative Test for Entitlement - 'Owner', 'Everyone' & 'Custom' through Search operation for users that does not belong to same Tenant.
    @Test
    public void testEntitledUserSearchForOutsideTenant() throws Exception {
        logger.info("Create Cluster [{}]", dataCenter.getName());
        ResponseEntity<DataCenter> response = dataCenterService.create(dataCenter);
        for (Message m : response.getMessages()) {
            logger.warn("[{}]", m.getMessageText());
        }
        if(response.getResults() != null){
            dataCenterCreated = response.getResults();
        }
        if (!error) {

            if (((dataCenterCreated.getEntitlementType().equals(EntitlementType.OWNER) )
                    || (dataCenterCreated.getEntitlementType().equals(EntitlementType.PUBLIC))
                    || (dataCenterCreated.getEntitlementType().equals(EntitlementType.CUSTOM)))) {
                ResponseEntity<List<DataCenter>> clusterSearchResponseEntity = dataCenterService3.search(dataCenter.getName(), 0, 1);
                for (Message message : clusterSearchResponseEntity.getMessages()) {
                    logger.warn("Error while Search request  [{}] ", message.getMessageText());
                }
                assertNotNull(clusterSearchResponseEntity);
                assertNotNull(clusterSearchResponseEntity.isErrors());
                Assert.assertEquals(0, clusterSearchResponseEntity.getResults().size());
            }

            else {
                Assert.fail("Entitlement Type Not supported: " + dataCenterCreated.getEntitlementType());
            }
        }
    }

    // Negative Test for Entitlement - 'Owner', 'Everyone' & 'Custom' through find by ID for users that does not belong to same Tenant.
    @Test
    public void testEntitledUserFindByIdForOutsizeTenant() throws Exception {
        logger.info("Create Cluster [{}]", dataCenter.getName());
        ResponseEntity<DataCenter> response = dataCenterService.create(dataCenter);
        for (Message m : response.getMessages()) {
            logger.warn("[{}]", m.getMessageText());
        }
        if(response.getResults() != null){
            dataCenterCreated = response.getResults();
        }
        if (!error) {
            if (((dataCenterCreated.getEntitlementType().equals(EntitlementType.OWNER) )
                    || (dataCenterCreated.getEntitlementType().equals(EntitlementType.PUBLIC))
                    || (dataCenterCreated.getEntitlementType().equals(EntitlementType.CUSTOM)))) {
                ResponseEntity<DataCenter> findbyIdResponse = dataCenterService3.findById(dataCenterCreated.getId());
                for (Message message : findbyIdResponse.getMessages()) {
                    logger.warn("Error while Find request  [{}] ", message.getMessageText());
                }
                Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
                assertNotNull(findbyIdResponse);
                Assert.assertEquals(findbyIdResponse.getResults(), null);
            }
            else {
                Assert.fail("Entitlement Type Not supported: " + dataCenterCreated.getEntitlementType());
            }

        }
    }

    //Delete the Cluster created above to keep the data neat & clean.
    @After
    public void cleanUp() {
        logger.info("cleaning up...");

        if (dataCenterCreated != null) {
            ResponseEntity<DataCenter> deleteResponse  =   dataCenterService.delete(dataCenterCreated.getId());

            for (Message m : deleteResponse.getMessages()){
                logger.warn("[{}]", m.getMessageText());
                validationMessage = m.getMessageText();}

            //check for errors
            Assert.assertFalse(validationMessage ,deleteResponse.isErrors());
        }
    }
}
