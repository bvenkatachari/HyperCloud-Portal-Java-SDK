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
package io.dchq.sdk.core.plugins;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.springframework.util.StringUtils;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.container.Env;
import com.dchq.schema.beans.one.plugin.Plugin;
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.PluginService;
import io.dchq.sdk.core.ServiceFactory;

/**
 * Abstracts class for holding test credentials.
 *
 * @author Abedeen.
 * @author Intesar Mohammed
 * @contributor SaurabhB.
 * @since 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class PluginUpdateServiceTest extends AbstractServiceTest {

    private PluginService appService;

    @org.junit.Before
    public void setUp() throws Exception {
        appService = ServiceFactory.buildPluginService(rootUrl1, cloudadminusername, cloudadminpassword);
    }

    private Plugin plugin;
    private Plugin pluginUpdated;
    private String messageText;
    private Boolean isEntitlementTypeUser;
    private boolean errors;

    // base -- create - find - change -- update -- check

    /**
     * Name: Not-Empty, Max_Length:Short-Text, Unique with Version per owner
     * Version: default:1.0,
     * Description: Optional, Max_length:Large-Text
     * Script: Not-Empty, Large-Text
     * Script-Lang: default:SHELL, POWERSHELL, PERL, RUBY, PYTHON
     * License: default:EUlA, Apache License 2.0
     * Timeout: default:30, > 0, Max < ?
     * Entitlement-Type: default:OWNER, CUSTOM: USERS, GROUPS
     * Entitled-Users:
     * Valid user_id
     * Entitled-Groups
     * Valid group_id
     * Arguments: Optional
     * ScriptArgs: Optional
     * ENV: Optional
     * prop:  Not-Empty
     * val: Not-Empty
     * eVal: value should be ignored
     * hidden: default: false, true
     * InActive: default: false, true
     * <p/>
     * <p/>
     * Test-Cases
     * 1. Valid - name, version, script,
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // sets of two, first is the base and the second is the update version
                {"TestPlugin113", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), false,  false},
                {"TestPlugin12", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), false,  false},

                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},
                {"TestPlugin11", "1.4", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},

                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},
                {"TestPlugin11", "1.3", "Description1", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},

                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},
                {"TestPlugin11", "1.3", "Description", "Dummy Script1", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},

                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},
                {"TestPlugin11", "1.3", "Description", "Dummy Script", "PERL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},

                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},
                {"TestPlugin11", "1.3", "Description", "Dummy Script", "RUBY", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},

                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},
                {"TestPlugin11", "1.3", "Description", "Dummy Script", "PYTHON", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},

                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},
                {"TestPlugin11", "1.3", "Description", "Dummy Script", "POWERSHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},

                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},
                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "EULA", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},

                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},
                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 31, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},

                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},
                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.OWNER, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},

                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},
                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, null, true,  false},

                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), true,  false},
                {"TestPlugin11", "1.3", "Description", "Dummy Script", "SHELL", "Apache License 2.0", 30, EntitlementType.CUSTOM, true, userId2, null, new HashSet<>(Arrays.asList(new Env().withProp("prop1").withVal("val1"))), false,  false}
        });
    }

    public PluginUpdateServiceTest(String pluginName, String version, String description, String pluginScript, String scriptType, String license,
                                   Integer timeout, EntitlementType entitlementType, boolean isEntitlementTypeUser, String entitledUserId,
                                   String scriptArgs, Set<Env> envs, Boolean inactive, boolean errors) {

        // random pluginname
        if (pluginName == null){
            throw new IllegalArgumentException("PluginName==null");
        }

        if (!pluginName.isEmpty()) {

            String prefix = RandomStringUtils.randomAlphabetic(3);
            pluginName = prefix + pluginName;
            pluginName = org.apache.commons.lang3.StringUtils.lowerCase(pluginName);
        }

        this.plugin = new Plugin();

        this.plugin.setName(pluginName);
        this.plugin.setVersion(version);
        this.plugin.setDescription(description);

        this.plugin.setBaseScript(pluginScript);
        this.plugin.setScriptLang(scriptType);

        this.plugin.setLicense(license);
        this.plugin.setTimeout(timeout);

        this.plugin.setEnvs(envs);
        this.plugin.setScriptArgs(scriptArgs);

        this.isEntitlementTypeUser = isEntitlementTypeUser;
        this.plugin.setEntitlementType(entitlementType);
        if (EntitlementType.CUSTOM == entitlementType && isEntitlementTypeUser) {
            this.plugin.setEntitledUsers(new ArrayList<UsernameEntityBase>(Arrays.asList(new UsernameEntityBase().withId(entitledUserId))));
        } else if (EntitlementType.CUSTOM == entitlementType && !isEntitlementTypeUser) {
            this.plugin.setEntitledUserGroups(new ArrayList<NameEntityBase>(Arrays.asList(new NameEntityBase().withId(entitledUserId))));
        }

        //this.plugin.setInactive(inactive);

        this.errors = errors;


    }

   // @Ignore
    @org.junit.Test
	public void testUpdate() throws Exception {

		ResponseEntity<Plugin> response = this.appService.create(this.plugin);
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
			messageText = message.getMessageText();
		}

        //Check for response
        assertNotNull(response);
        //Check error is not null
        assertNotNull(response.isErrors());
        //Check for error boolean response
        Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) response.isErrors()).toString());
        //check for errors
        Assert.assertFalse(messageText , response.isErrors());
        
        this.plugin = response.getResults();
        
        if (!response.isErrors()) {
        	
        	String updatedName = this.plugin.getName()+"updated";
        	this.plugin.setName(updatedName);
        	
        	response = appService.update(this.plugin);
        	
            pluginUpdated = response.getResults();
            assertNotNull(response.getResults());
            assertNotNull(response.getResults().getId());

            // name
            assertEquals(plugin.getName(), pluginUpdated.getName());

            // version
            if (StringUtils.isEmpty(plugin.getVersion())) {
                assertEquals("1.0", pluginUpdated.getVersion());
            } else {
                assertEquals(plugin.getVersion(), pluginUpdated.getVersion());
            }

            assertEquals(plugin.getDescription(), pluginUpdated.getDescription());
            assertEquals(plugin.getBaseScript(), pluginUpdated.getBaseScript());

            // license
            if (StringUtils.isEmpty(plugin.getLicense())) {
                assertEquals("EULA", pluginUpdated.getLicense());
            } else {
                assertEquals(plugin.getLicense(), pluginUpdated.getLicense());
            }

            // timeout
            if (StringUtils.isEmpty(plugin.getTimeout())) {
                assertEquals("30", pluginUpdated.getTimeout());
            } else {
                assertEquals(plugin.getTimeout(), pluginUpdated.getTimeout());
            }

            assertEquals(plugin.getBaseScript(), pluginUpdated.getBaseScript());
            // script-lang
            if (StringUtils.isEmpty(plugin.getScriptLang())) {
                assertEquals("SHELL", pluginUpdated.getScriptLang());
            } else {
                assertEquals(plugin.getScriptLang(), pluginUpdated.getScriptLang());
            }
            assertEquals(plugin.getEnvs(), pluginUpdated.getEnvs());
            assertEquals(plugin.getScriptArgs(), pluginUpdated.getScriptArgs());

            assertEquals(plugin.getEntitlementType(), pluginUpdated.getEntitlementType());

            assertEquals(plugin.getInactive(), pluginUpdated.getInactive());

            assertEquals(plugin.getEntitlementType(), pluginUpdated.getEntitlementType());
            if (EntitlementType.CUSTOM == plugin.getEntitlementType() && isEntitlementTypeUser) {
                assertEquals(plugin.getEntitledUsers(), pluginUpdated.getEntitledUsers());
            } else if (EntitlementType.CUSTOM == plugin.getEntitlementType() && !isEntitlementTypeUser) {
                assertEquals(plugin.getEntitledUserGroups(), pluginUpdated.getEntitledUserGroups());
            }


        }

    }

    @After
    public void cleanUp() {
        logger.info("cleaning up...");

        if (this.plugin != null) {
            ResponseEntity<Plugin> deleteResponse  =   appService.delete(this.plugin.getId());
            for (Message m : deleteResponse.getMessages()){
                logger.warn("[{}]", m.getMessageText());
                messageText = m.getMessageText();}

            //check for errors
            Assert.assertFalse(messageText ,deleteResponse.isErrors());
            logger.info("Deleted Object Successfully  with  ID [{}]", this.pluginUpdated.getId());
        }
    }
}
