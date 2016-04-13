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

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.plugin.Plugin;

import com.dchq.schema.beans.one.security.EntitlementType;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


import static junit.framework.TestCase.assertNotNull;

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
public class PluginUpdateServiceTest extends AbstractServiceTest {

    private PluginService appService;

    @org.junit.Before
    public void setUp() throws Exception {
        appService = ServiceFactory.buildPluginService(rootUrl, username, password);
    }

    private Plugin plugin;
    private boolean createError, updateError;
    private Plugin pluginCreated, pluginUpdated;
    private String validityMessage, updatedName;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"TestPlugin11", "1.1", "Dummy Script", "PERL", "Apache License 2.0", EntitlementType.CUSTOM, true, userId2, "General Input", "Plugin Updated", false, false},
        });
    }

    public PluginUpdateServiceTest(String pluginName, String version, String pluginScript, String scriptType, String license,
                                   EntitlementType entitlementType, boolean isEntitlementTypeUser, String entitledUserId, String validityMessage, String updatedName, boolean createError, boolean updateError) {
        this.plugin = new Plugin();
        this.plugin.setName(pluginName);
        this.plugin.setVersion(version);
        this.plugin.setBaseScript(pluginScript);
        this.plugin.setScriptLang(scriptType);
        this.plugin.setLicense(license);
        this.plugin.setEntitlementType(entitlementType);
        this.createError = createError;
        this.updateError = updateError;
        this.validityMessage = validityMessage;
        this.updatedName = updatedName;

    }

    @org.junit.Test
    public void testUpdate() throws Exception {

        logger.info("Create Object with Name [{}]", this.plugin.getName());
        ResponseEntity<Plugin> response = appService.create(plugin);

        for (Message message : response.getMessages())
            logger.warn("Error while Create request  [{}] ", message.getMessageText());


        assertNotNull(response);
        assertNotNull(response.isErrors());

        Assert.assertNotNull(((Boolean) createError).toString(), ((Boolean) response.isErrors()).toString());

        if (!response.isErrors() && response.getResults() != null) {
            pluginCreated = response.getResults();
            assertNotNull(response.getResults());
            assertNotNull(response.getResults().getId());

            // Comparing objects agains created Objects.
            Assert.assertNotNull(plugin.getName(), pluginCreated.getName());
            Assert.assertNotNull(plugin.getVersion(), pluginCreated.getVersion());
            Assert.assertNotNull(plugin.getBaseScript(), pluginCreated.getBaseScript());
            Assert.assertNotNull(plugin.getScriptLang(), pluginCreated.getScriptLang());
            Assert.assertNotNull(plugin.getLicense(), pluginCreated.getLicense());
            Assert.assertNotNull(plugin.getEntitlementType().toString(), pluginCreated.getEntitlementType().toString());


        }
        pluginCreated.setName(updatedName);
        logger.info("Update Object with  Name [{}]", this.pluginCreated.getName());
        response = appService.create(pluginCreated);

        for (Message message : response.getMessages())
            logger.warn("Error while Update request  [{}] ", message.getMessageText());


        assertNotNull(response);
        assertNotNull(response.isErrors());

        Assert.assertNotNull(((Boolean) updateError).toString(), ((Boolean) response.isErrors()).toString());

        if (!response.isErrors() && response.getResults() != null) {
            pluginUpdated = response.getResults();
            assertNotNull(response.getResults());
            assertNotNull(response.getResults().getId());

            // Comparing objects agains created Objects.
            Assert.assertNotNull(pluginUpdated.getName(), pluginCreated.getName());
            /*Assert.assertNotNull(pluginUpdated.getName(), pluginCreated.getName());
            Assert.assertNotNull(pluginUpdated.getVersion(), pluginCreated.getVersion());
            Assert.assertNotNull(pluginUpdated.getBaseScript(), pluginCreated.getBaseScript());
            Assert.assertNotNull(pluginUpdated.getScriptLang(), pluginCreated.getScriptLang());
            Assert.assertNotNull(pluginUpdated.getLicense(), pluginCreated.getLicense());
            Assert.assertNotNull(pluginUpdated.getEntitlementType().toString(), pluginCreated.getEntitlementType().toString());*/
            logger.info("Update Object Successfully  with  Name [{}]", this.pluginUpdated.getName());
        }

    }

    @After
    public void cleanUp() {
        logger.info("cleaning up...");

        if (pluginCreated != null) {
            appService.delete(pluginCreated.getId());
            logger.info("Deleted Object Successfully  with  ID [{}]", this.pluginCreated.getId());
        }
    }
}