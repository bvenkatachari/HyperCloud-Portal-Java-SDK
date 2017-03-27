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

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.plugin.Plugin;
import com.dchq.schema.beans.one.security.EntitlementType;
import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.PluginService;
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


import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;


/**
 * @author Intesar Mohammed
 * @contributor Saurabh B.
 * @since 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class PluginEntitleServiceTest extends AbstractServiceTest {

    private PluginService pluginService;
    private PluginService pluginService2, pluginService3;
    private String messageText;

    @org.junit.Before
    public void setUp() throws Exception {
        pluginService = ServiceFactory.buildPluginService(rootUrl, username, password);
        pluginService2 = ServiceFactory.buildPluginService(rootUrl, username2, password2);
        pluginService3 = ServiceFactory.buildPluginService(rootUrl, username3, password3);
    }

    private Plugin plugin;
    private boolean errors;
    private Plugin pluginCreated;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"TestPlugin1", "1.1", "Dummy Script", "SHELL", "Apache License 2.0", EntitlementType.OWNER, true, userId2, false},
                {"TestPlugin2", "1.1", "Dummy Script", "POWERSHELL", "Apache License 2.0", EntitlementType.OWNER, true, userId2, false},
                {"TestPlugin3", "1.1", "Dummy Script", "PERL", "Apache License 2.0", EntitlementType.OWNER, true, userId2, false},
                {"TestPlugin4", "1.1", "Dummy Script", "PYTHON", "Apache License 2.0", EntitlementType.OWNER, true, userId2, false},
                {"TestPlugin5", "1.1", "Dummy Script", "RUBY", "Apache License 2.0", EntitlementType.OWNER, true, userId2, false},

                {"TestPlugin1111", "1.1", "Dummy Script", "SHELL", "Apache License 2.0", EntitlementType.PUBLIC, true, userId2, false},
                {"TestPlugin1111", "1.1", "Dummy Script", "POWERSHELL", "Apache License 2.0", EntitlementType.PUBLIC, true, userId2, false},
                {"TestPlugin1111", "1.1", "Dummy Script", "PERL", "Apache License 2.0", EntitlementType.PUBLIC, true, userId2, false},
                {"TestPlugin1111", "1.1", "Dummy Script", "PYTHON", "Apache License 2.0", EntitlementType.PUBLIC, true, userId2, false},
                {"TestPlugin1111", "1.1", "Dummy Script", "RUBY", "Apache License 2.0", EntitlementType.PUBLIC, true, userId2, false},

                {"TestPlugin1111", "1.1", "Dummy Script", "SHELL", "Apache License 2.0", EntitlementType.CUSTOM, true, userId2, false},
                {"TestPlugin1111", "1.1", "Dummy Script", "POWERSHELL", "Apache License 2.0", EntitlementType.CUSTOM, true, userId2, false},
                {"TestPlugin1111", "1.1", "Dummy Script", "PERL", "Apache License 2.0", EntitlementType.CUSTOM, true, userId2, false},
                {"TestPlugin1111", "1.1", "Dummy Script", "PYTHON", "Apache License 2.0", EntitlementType.CUSTOM, true, userId2, false},
                {"TestPlugin1111", "1.1", "Dummy Script", "RUBY", "Apache License 2.0", EntitlementType.CUSTOM, true, userId2, false},

                {"TestPlugin1111", "1.1", "Dummy Script", "SHELL", "Apache License 2.0", EntitlementType.CUSTOM, false, USER_GROUP, false},
                {"TestPlugin1111", "1.1", "Dummy Script", "POWERSHELL", "Apache License 2.0", EntitlementType.CUSTOM, false, USER_GROUP, false},
                {"TestPlugin1111", "1.1", "Dummy Script", "PERL", "Apache License 2.0", EntitlementType.CUSTOM, false, USER_GROUP, false},
                {"TestPlugin1111", "1.1", "Dummy Script", "PYTHON", "Apache License 2.0", EntitlementType.CUSTOM, false, USER_GROUP, false},
                {"TestPlugin1111", "1.1", "Dummy Script", "RUBY", "Apache License 2.0", EntitlementType.CUSTOM, false, USER_GROUP, false},


        });
    }

    public PluginEntitleServiceTest(String pluginName, String version, String pluginScript, String scriptType, String license,
                                    EntitlementType entitlementType, boolean isEntitlementTypeUser, String entitledUserId, boolean errors) {

        // random plugin name
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
        this.plugin.setBaseScript(pluginScript);
        this.plugin.setScriptLang(scriptType);
        this.plugin.setLicense(license);
        this.plugin.setEntitlementType(entitlementType);

        if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
            UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
            List<UsernameEntityBase> entiledUsers = new ArrayList<>();
            entiledUsers.add(entitledUser);
            this.plugin.setEntitledUsers(entiledUsers);
        } else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
            NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
            List<NameEntityBase> entiledUsers = new ArrayList<>();
            entiledUsers.add(entitledUser);
            this.plugin.setEntitledUserGroups(entiledUsers);
        }

        this.errors = errors;

    }

    // Test for Entitlement - 'Only Me', 'Everyone' & 'Custom' through Search Operation.
    @Test
    public void testEntitledUserOwnerSearch() throws Exception {
        logger.info("Create Plugin [{}]", plugin.getName());
        ResponseEntity<Plugin> response = pluginService.create(plugin);
        for (Message m : response.getMessages()) {
            logger.warn("[{}]", m.getMessageText());
        }
        if(response.getResults() != null){
            pluginCreated = response.getResults();
        }
        if (!errors) {
            if (pluginCreated.getEntitlementType().equals(EntitlementType.OWNER) ) {
                ResponseEntity<List<Plugin>> pluginSearchResponseEntity1 = pluginService2.search(plugin.getName(), 0, 1);
                for (Message message : pluginSearchResponseEntity1.getMessages()) {
                    logger.warn("Error while Search request  [{}] ", message.getMessageText());
                    //errorMessage += message.getMessageText() + "\n";
                }
                assertNotNull(pluginSearchResponseEntity1);
                assertNotNull(pluginSearchResponseEntity1.isErrors());
                assertNotNull(pluginSearchResponseEntity1.getResults());
                assertEquals(0, pluginSearchResponseEntity1.getResults().size());
            }

            else if (pluginCreated.getEntitlementType().equals(EntitlementType.PUBLIC) ) {
                ResponseEntity<List<Plugin>> pluginSearchResponseEntity = pluginService2.search(plugin.getName(), 0, 1);
                for (Message message : pluginSearchResponseEntity.getMessages()) {
                    logger.warn("Error while Search request  [{}] ", message.getMessageText());
                    }
                assertNotNull(pluginSearchResponseEntity);
                assertNotNull(pluginSearchResponseEntity.isErrors());
                assertEquals(1, pluginSearchResponseEntity.getResults().size());
            }
            else  if (pluginCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
                ResponseEntity<List<Plugin>> pluginSearchResponseEntity = pluginService2
                        .search(plugin.getName(), 0, 1);
                for (Message message : pluginSearchResponseEntity.getMessages()) {
                    logger.warn("Error while Search request  [{}] ", message.getMessageText());
                }
                assertNotNull(pluginSearchResponseEntity);
                assertNotNull(pluginSearchResponseEntity.isErrors());
                assertEquals(1, pluginSearchResponseEntity.getResults().size());
            }
            else {
                Assert.fail("Entitlement Type Not supported: " + pluginCreated.getEntitlementType());
            }
        }
    }

    // Test for Entitlement - 'Only Me', 'Everyone' & 'Custom'  through Find by ID.
    @Test
    public void testEntitledUserOwnerFindById() throws Exception {
        logger.info("Create Plugin [{}]", plugin.getName());
        ResponseEntity<Plugin> response = pluginService.create(plugin);
        for (Message m : response.getMessages()) {
            logger.warn("[{}]", m.getMessageText());
        }
        if(response.getResults() != null){
            pluginCreated = response.getResults();
        }
        if (!errors) {
            if (pluginCreated.getEntitlementType().equals(EntitlementType.OWNER)) {
                ResponseEntity<Plugin> findbyIdResponse = pluginService2.findById(pluginCreated.getId());
                for (Message message : findbyIdResponse.getMessages()) {
                    logger.warn("Error while Find request  [{}] ", message.getMessageText());
                }
                Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
                assertNotNull(findbyIdResponse);
                assertNotNull(findbyIdResponse.isErrors());
                assertEquals(findbyIdResponse.getResults(), null);
            }

            else  if (pluginCreated.getEntitlementType().equals(EntitlementType.PUBLIC)) {
                ResponseEntity<Plugin> findbyIdResponse = pluginService2.findById(pluginCreated.getId());
                for (Message message : findbyIdResponse.getMessages()) {
                    logger.warn("Error while Find request  [{}] ", message.getMessageText());
                }
                Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
                assertNotNull(findbyIdResponse.getResults());
                assertEquals(pluginCreated.getId(), findbyIdResponse.getResults().getId());
            }

            else if (pluginCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
                ResponseEntity<Plugin> findbyIdResponse = pluginService2.findById(pluginCreated.getId());
                for (Message message : findbyIdResponse.getMessages()) {
                    logger.warn("Error while Find request  [{}] ", message.getMessageText());
                }
                Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
                assertNotNull(findbyIdResponse.getResults());
                assertEquals(pluginCreated.getId(), findbyIdResponse.getResults().getId());
            }

            else {
                    Assert.fail("Entitlement Type Not supported: " + pluginCreated.getEntitlementType());

                 }
        }
    }

   // Negative Test for Entitlement - 'Owner', 'Everyone' & 'Custom' through Search operation for users that does not belong to same Tenant.
    @Test
    public void testEntitledUserSearchForOutsideTenant() throws Exception {
        logger.info("Create Plugin [{}]", plugin.getName());
        ResponseEntity<Plugin> response = pluginService.create(plugin);
        for (Message m : response.getMessages()) {
            logger.warn("[{}]", m.getMessageText());
        }
        if(response.getResults() != null){
            pluginCreated = response.getResults();
        }
        if (!errors) {

            if (((pluginCreated.getEntitlementType().equals(EntitlementType.OWNER) )
                    || (pluginCreated.getEntitlementType().equals(EntitlementType.PUBLIC))
                    || (pluginCreated.getEntitlementType().equals(EntitlementType.CUSTOM)))) {
                ResponseEntity<List<Plugin>> pluginSearchResponseEntity = pluginService3.search(plugin.getName(), 0, 1);
                for (Message message : pluginSearchResponseEntity.getMessages()) {
                    logger.warn("Error while Search request  [{}] ", message.getMessageText());
                }
                assertNotNull(pluginSearchResponseEntity);
                assertNotNull(pluginSearchResponseEntity.isErrors());
                assertEquals(0, pluginSearchResponseEntity.getResults().size());
            }

         else {
                Assert.fail("Entitlement Type Not supported: " + pluginCreated.getEntitlementType());
            }
        }
    }

    // Negative Test for Entitlement - 'Owner', 'Everyone' & 'Custom' through find by ID for users that does not belong to same Tenant.
    @Test
    public void testEntitledUserFindByIdForOutsizeTenant() throws Exception {
        logger.info("Create Plugin [{}]", plugin.getName());
        ResponseEntity<Plugin> response = pluginService.create(plugin);
        for (Message m : response.getMessages()) {
            logger.warn("[{}]", m.getMessageText());
        }
        if(response.getResults() != null){
            pluginCreated = response.getResults();
        }
        if (!errors) {
            if (((pluginCreated.getEntitlementType().equals(EntitlementType.OWNER) )
                    || (pluginCreated.getEntitlementType().equals(EntitlementType.PUBLIC))
                    || (pluginCreated.getEntitlementType().equals(EntitlementType.CUSTOM)))) {
                ResponseEntity<Plugin> findbyIdResponse = pluginService3.findById(pluginCreated.getId());
                for (Message message : findbyIdResponse.getMessages()) {
                    logger.warn("Error while Find request  [{}] ", message.getMessageText());
                }
                Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) findbyIdResponse.isErrors()).toString());
                assertNotNull(findbyIdResponse);
                assertEquals(findbyIdResponse.getResults(), null);
            }
            else {
                Assert.fail("Entitlement Type Not supported: " + pluginCreated.getEntitlementType());
            }

        }
    }

    @After
    public void cleanUp() {
        logger.info("cleaning up...");

        if (pluginCreated != null) {
            ResponseEntity<Plugin> deleteResponse  =   pluginService.delete(pluginCreated.getId());

            for (Message m : deleteResponse.getMessages()){
                logger.warn("[{}]", m.getMessageText());
                messageText = m.getMessageText();}

            //check for errors
            Assert.assertFalse(messageText ,deleteResponse.isErrors());
        }
    }
}
