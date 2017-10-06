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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;



/** Abstracts class for holding test credentials.
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
public class DataCenterSearchServiceTest extends AbstractServiceTest {

    private DataCenterService dataCenterService;

    @org.junit.Before
    public void setUp() throws Exception {
        dataCenterService = ServiceFactory.buildDataCenterService(rootUrl, cloudadminusername, cloudadminpassword);
    }

    DataCenter dataCenter;
    DataCenter dataCenterCreated;
    boolean error;
    String validationMessage;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Cluster AA4", "Weave", "ABC", EntitlementType.ALL_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced", 5, "1g", false, false, 4, "\nAll Input Values are normal. Malfunction in SDK", false},
                {"Cluster AA4", "Docker Network", "Network", EntitlementType.MY_BLUEPRINTS, EntitlementType.ALL_PLUGINS, "Approval", "Advanced", 5, "1g", false, false, 4, "\nAll Input Values are normal. Malfunction in SDK", false},
                {"Cluster AA4", "Docker Swarm", "Swarm", EntitlementType.MY_BLUEPRINTS, EntitlementType.MY_PLUGINS, "Approval", "Advanced", 5, "1g", false, false, 4, "\nAll Input Values are normal. Malfunction in SDK", false},
                {"Cluster AA4", "Docker - UCP", "UCP", EntitlementType.ALL_BLUEPRINTS, EntitlementType.MY_PLUGINS, "Approval", "Advanced", 5, "1g", false, false, 4, "\nAll Input Values are normal. Malfunction in SDK", false},

        });
    }

    public DataCenterSearchServiceTest(String clusterName, String networkType, String description,
                                       EntitlementType blueprintType, EntitlementType plugins, String capAdd,
                                       String networkPass, int cpuShares, String memoryLimit,
                                       boolean terminationProtection, boolean approvalEnforced,
                                       int maxContainerLimit, String validationMessage, boolean error)

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
        this.validationMessage = validationMessage;
        this.error = error;

        this.validationMessage = validationMessage;

    }

    @Test
    public void testSearch() throws Exception {

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

            //Search Cluster by Name
            ResponseEntity<List<DataCenter>> dataCenterResponseEntity = dataCenterService.search(dataCenterCreated.getName(), 0, 1);

            for (Message message : dataCenterResponseEntity.getMessages()) {
                logger.warn("Error while Create request  [{}] ", message.getMessageText());
                validationMessage += message.getMessageText() + "\n";
            }

            assertNotNull(dataCenterResponseEntity);
            assertNotNull(dataCenterResponseEntity.isErrors());
            assertFalse(validationMessage, dataCenterResponseEntity.isErrors());

            assertNotNull(dataCenterResponseEntity.getResults());

            Assert.assertEquals(1, dataCenterResponseEntity.getResults().size());

            DataCenter searchedEntity = dataCenterResponseEntity.getResults().get(0);
            Assert.assertEquals(dataCenterCreated.getId(), searchedEntity.getId());

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