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
package io.dchq.sdk.core.builds;

import static junit.framework.TestCase.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
//import com.dchq.schema.beans.one.blueprint.Blueprint;
//import com.dchq.schema.beans.one.blueprint.BlueprintType;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.blueprint.RegistryAccount;
import com.dchq.schema.beans.one.build.Build;
import com.dchq.schema.beans.one.build.BuildTask;
import com.dchq.schema.beans.one.build.BuildType;
import com.dchq.schema.beans.one.provider.DataCenter;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.BuildService;
import io.dchq.sdk.core.RegistryAccountService;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.machines.DockerServerCreateServiceTest;

/**
 * Created by Abedeen on 04/05/16.
 */

/**
 * Abstracts class for holding test credentials.
 *
 * @author Abedeen.
 * @since 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class BuildCreateServiceTest extends AbstractServiceTest {

    private BuildService buildService;
    private DockerServerCreateServiceTest dockerServerCreateService;

    public DataCenter getDataCenter() throws Exception {
        //dockerServerCreateService = new DockerServerCreateServiceTest("TEST_BUILD_RACK ("+getDateSuffix(null)+")", Boolean.FALSE, "VHG01-N03", "cpu=1,memory=1GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\CentOS7HFTemplate.vhdx", "Compute vmSwitch", 1, "2c9180865bb2559a015bd998188e4457", 360000,"Build_Cluster("+getDateSuffix(null)+")", false);
    	dockerServerCreateService = new DockerServerCreateServiceTest("automationtest", Boolean.FALSE, "cpu=1,memory=4GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ubuntu1604HFTemplate.vhdx", "Compute vmSwitch,vlanId=504", 1, "2c9180865d312fc4015d3134e26d0002", 360000,"Build_Cluster("+getDateSuffix(null)+")", false, true);
    	dockerServerCreateService.setUp();

        return dockerServerCreateService.getDataCenter();
    }

    @org.junit.Before
    public void setUp() throws Exception {
        buildService = ServiceFactory.buildBuildService(rootUrl, username, password);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Test Build2", BuildType.GITHUB_PUBLIC_REPO,"https://github.com/dockerfile/ubuntu.git","ff808181542bf58901542d2e611400fa","abedeen/syed", "latest","ff808181542bf58901542d29602400f7",false}
        });
    }

    private RegistryAccountService registryAccountService;
    private Build build;
    private boolean success;
    private Build buildCreated;
    private RegistryAccount githubRegistryAccount;
    private RegistryAccount dockerRegistryAccount;



    DataCenter createDataCenter =null;

    public BuildCreateServiceTest(String dockerScript, BuildType buildType,String gitURL,String cluster,String pustToRepository,String tag,String registryAccountId, boolean success)  throws Exception {
        createDataCenter = getDataCenter();
        this.build = new Build()
                .withBuildType(buildType);
        this.build.setCluster(createDataCenter.getId());

        //this.build.setCluster("ff8081815434dd7e015434e0a2240007");

        build.setTag(tag);
        build.setGitCloneUrl(gitURL);
        build.setRepository(pustToRepository);
        NameEntityBase neb = new NameEntityBase();
        neb.setId(registryAccountId);
        build.setRegistryAccount(neb);
        this.success = success;


    }

    @Ignore
    @org.junit.Test
    public void testCreate() throws Exception {

      //  Assert.assertNotNull(dockerServerCreateService.dockerServerCreated);

        logger.info("Script Started..... {}", build.getDockerScript());
        ResponseEntity<Build> response = buildService.create(build);

        String errorMessage = "";
        for (Message message : response.getMessages()) {
            logger.warn("Error while Create request  [{}] ", message.getMessageText());
            errorMessage += ("Error while Create request  [{}] " + message.getMessageText());
        }
        assertNotNull(response.getResults());
        assertNotNull(response.getResults().getId());
        Assert.assertNotNull(errorMessage,response.getResults());
      //  Assert.assertNotNull(response.getResults().getId());

        if (response.getResults()!=null) {

            assertNotNull(response.getResults());
            assertNotNull(response.getResults().getId());

            buildCreated = response.getResults();
            ResponseEntity<BuildTask> responseTask  = buildService.buildNow(buildCreated.getId());
            BuildTask buildTask=getTask(responseTask);
          //  assertNotNull(buildTask);

                maxWaitTime=3*60*1000;
            waitTime=0;
                do{
                    wait(10000);
                    responseTask  = buildService.findBuildTaskById(buildTask.getId());
                    buildTask=getTask(responseTask);
               //     assertNotNull(buildTask);


                }while(buildTask.getBuildTaskStatus().name().equals("PROCESSING"));

        }
    }
public BuildTask getTask(ResponseEntity<BuildTask> responseTask) {

    String errorMessage = "";
    BuildTask buildTask =null;
    for (Message message : responseTask.getMessages()) {
        logger.warn("Error while Running Build Task request  [{}] ", message.getMessageText());
        errorMessage += ("Error while Running Build Task request  " + message.getMessageText());
    }
    if (responseTask.getResults() != null) {
        buildTask = responseTask.getResults();
        Assert.assertFalse("Machine Creation Replied with Error." + errorMessage, responseTask.isErrors());


    }
    return buildTask;
}
    @After
    public void cleanUp() throws Exception  {
        logger.info("cleaning up...");

        if(buildCreated!=null) buildService.delete(buildCreated.getId());


            dockerServerCreateService.cleanUp();


    }

}

