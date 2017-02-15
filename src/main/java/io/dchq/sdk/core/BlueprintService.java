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

import java.util.List;

/**
 * <code>Blueprint</code> endpoint API calls.
 *
 * @author Intesar Mohammed
 * @since 1.0
 */
public interface BlueprintService extends GenericService<Blueprint, ResponseEntity<List<Blueprint>>, ResponseEntity<Blueprint>> {


    /**
     * Find starred <code>Blueprint</code>.
     *
     * @return Starred blueprints
     */
    ResponseEntity<List<Blueprint>> findByStarred();

    /**
     * Find <code>Blueprint</code> YAML.
     *
     * @return Blueprint YAML
     */
    ResponseEntity<Blueprint> findYamlById(String id);

    /**
     * Search entitled <code>Blueprint</code>.
     *
     * @param token
     * @return
     */
    ResponseEntity<List<Blueprint>> searchEntitled(String token, Integer page, Integer pageSize);


}
