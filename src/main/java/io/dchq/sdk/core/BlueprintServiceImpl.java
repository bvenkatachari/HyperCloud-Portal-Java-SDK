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

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.blueprint.Blueprint;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

/**
 * Encapsulates DCHQ Blueprint endpoint calls.
 *
 * @author Intesar Mohammed
 * @see <a href="https://dchq.readme.io/docs/blueprints-1">Blueprint endpoint</a>
 * @since 1.0
 */
class BlueprintServiceImpl extends GenericServiceImpl<Blueprint, ResponseEntity<List<Blueprint>>, ResponseEntity<Blueprint>>
        implements BlueprintService {

    public static final String ENDPOINT = "blueprints/";

    /**
     * @param baseURI  - e.g. https://dchq.io/api/1.0/
     * @param username - registered username with DCHQ.io
     * @param password - password used with the username
     */
    public BlueprintServiceImpl(String baseURI, String username, String password) {
        super(baseURI, ENDPOINT, username, password,
                new ParameterizedTypeReference<ResponseEntity<List<Blueprint>>>() {
                },
                new ParameterizedTypeReference<ResponseEntity<Blueprint>>() {
                }
        );
    }


    @Override
    public ResponseEntity<List<Blueprint>> findByStarred() {
        return findAll();
    }

    @Override
    public ResponseEntity<Blueprint> findYamlById(String id) {
        return findById(id + "/yaml");
    }

    @Override
    public ResponseEntity<List<Blueprint>> searchEntitled(String term, Integer page, Integer pageSize) {
        String url = baseURI + endpoint + "searchLibraryPage";
        return searchBase(term, page, pageSize, url);
    }
}
