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
import com.dchq.schema.beans.one.provider.DataCenter;
import com.dchq.schema.beans.one.security.EntitlementType;
import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DataCenterService;
import io.dchq.sdk.core.ServiceFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;


/**
 * Created by Abedeen on 04/05/16.
 */

/**
 * Abstracts class for holding test credentials.
 *
 * @author Abedeen.
 * @contributor Saurabh B.
 * @since 1.0
 */

/**
 * clusterName   -> Name of Cluster
 * networkType   -> Cluster N/W Type
 * description   -> Short Description
 * EntitlementType blueprintType  -> Trusted Blueprints
 * EntitlementType plugins        -> Trusted Plugins
 * capAdd  -> Cap-Add Policy "Approval" / "None"
 * networkPass  -> N/W Isolation Policy "Advanced", "Basic" / "None"
 * cpuShares    -> Max Shares, use Integer value
 * memoryLimit  -> Max memory policy
 * terminationProtection   -> VM Termination Protection accept boolean
 * approvalEnforced        -> Approval Policy accept boolean
 * maxContainerLimit       -> Max. container policy, use Integer
 **/


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DataCenterUpdateServiceTest extends AbstractServiceTest {

    private DataCenterService dataCenterService;

    @org.junit.Before
    public void setUp() throws Exception {
        dataCenterService = ServiceFactory.buildDataCenterService(rootUrl, username, password);
    }

    DataCenter dataCenter;
    DataCenter dataCenterCreated;
    DataCenter dataCenterUpdated;
    boolean error;
    String validationMessage;
    String updatedClusterName;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Cluster AA4","Weave", "ABC",EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "UpdatedName", "Approval", "Advanced", 5, "1g",false,true, 4,"\nAll Input Values are normal. Malfunction in SDK",false},

        });
    }

    public DataCenterUpdateServiceTest(String clusterName, String networkType, String description,
                                       EntitlementType blueprintType, EntitlementType plugins, String clusterUpdatedName, String capAdd,
                                       String networkPass, int cpuShares, String memoryLimit,
                                       boolean terminationProtection,boolean approvalEnforced,
                                       int maxContainerLimit, String validationMessage, boolean error)

    {
        // random clustername
        String prefix = RandomStringUtils.randomAlphabetic(3);
        clusterName = prefix + clusterName;
        clusterName = org.apache.commons.lang3.StringUtils.lowerCase(clusterName);

        clusterUpdatedName = prefix + clusterUpdatedName;
        clusterUpdatedName = org.apache.commons.lang3.StringUtils.lowerCase(clusterUpdatedName);


        this.dataCenter = new DataCenter().withName(clusterName).withNetwork(networkType).withBlueprintEntitlementType(blueprintType).withPluginEntitlementType(plugins);
        this.dataCenter.setDescription(description);
        this.dataCenter.setCapAdd(capAdd);
        this.dataCenter.setNetworkPass(networkPass);
        this.dataCenter.setCpuShares(cpuShares);
        this.dataCenter.setMemLimit(memoryLimit);
        this.dataCenter.setTerminationProtection(terminationProtection);
        this.dataCenter.setApprovalEnforced(approvalEnforced);
        this.dataCenter.setMaxContainerLimit(maxContainerLimit);
        this.validationMessage = validationMessage;
        this.updatedClusterName = clusterUpdatedName;
        this.error=error;

        this.validationMessage=validationMessage;

    }

    @org.junit.Test
    public void testUpdate() throws Exception {

        //Create Data Center
        logger.info("Create Cluster with Name [{}]", dataCenter.getName());
        ResponseEntity<DataCenter> response = dataCenterService.create(dataCenter);

        if (response.isErrors()) {
            for (Message m : response.getMessages()) {
                logger.warn("[{}]", m.getMessageText());
                validationMessage = m.getMessageText();
            }
            //check for errors
            Assert.assertEquals(validationMessage, error, response.isErrors());
        }

        assertNotNull(response);
        assertNotNull(response.isErrors());
        dataCenterCreated = response.getResults();
        assertEquals(error, response.isErrors());

        if (!response.isErrors()) {

            assertNotNull(response.getResults());
            assertNotNull(response.getResults().getId());

            assertEquals(dataCenter.getName(), dataCenterCreated.getName());
            assertEquals(dataCenter.getEntitledBlueprint(), dataCenterCreated.getEntitledBlueprint());
            assertEquals(dataCenter.getEntitledPlugin(), dataCenterCreated.getEntitledPlugin());
            assertEquals(dataCenter.getApprovalEnforced(), dataCenterCreated.getApprovalEnforced());

            logger.warn("Search Object wth Cluster Name  [{}] ", dataCenterCreated.getName());

            //Set Updated Cluster Name
            this.dataCenterCreated.setName(updatedClusterName);
            logger.info("Update Cluster with Name [{}]", dataCenterCreated.getName());

            //UPdating Cluster Name
            response = dataCenterService.update(dataCenterCreated);
            for (Message message : response.getMessages()){
                logger.warn("Error while Update request  [{}] ", message.getMessageText());
                validationMessage = message.getMessageText();
            }

            assertNotNull(response);
            if (response.isErrors()){
                Assert.fail(validationMessage);
            }
            else {
                Assert.assertNotNull(response.getResults());
                Assert.assertNotNull(response.getResults().getName(), updatedClusterName);
            }
        }

    }

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
