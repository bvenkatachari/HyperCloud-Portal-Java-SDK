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

import org.springframework.core.ParameterizedTypeReference;

/**
 * Abstracts and provides infrastructure to all API calls.
 *
 * @author Intesar Mohammed
 * @Contributor Saurabh B.
 * @since 1.0
 */
interface GenericService<E, RL, RO> {

    /**
     * Finds all <code>E</code>.
     *
     * @return - Response
     */
    RL findAll();

    RL findAll(int page, int size);
    
    RL findAllEntitled(int page, int size);

    /**
     * Find <code>E</code> by id.
     *
     * @param id - id to look for
     * @return - Response
     */
    RO findById(String id);

    /**
     * Finds all managed <code>E</code>.
     *
     * @return - Response
     */
    RL findAllManaged();

    /**
     * Find managed <code>E</code> by id.
     *
     * @param id - id to look for
     * @return - Response
     */
    RO findManagedById(String id);

    /**
     * Search all entitled
     *
     * @param term
     * @param page     - defaults to zero
     * @param pageSize - defaults to 20
     * @return
     */
    RL search(String term, Integer page, Integer pageSize);

    /**
     * Create <code>E</code>.
     *
     * @param entity - Object
     * @return
     */
    RO create(E entity);

    /**
     * Creates a post request with url postfix
     *
     * @param entity
     * @param urlPostfix
     * @return
     */
    Object post(E entity, String urlPostfix, ParameterizedTypeReference responseType);

    /**
     * Creates a post request with url postfix
     *
     * @param entity
     * @param urlPostfix
     * @return
     */
    RO doPost(Object entity, String urlPostfix);
    
    Object doPost(Object entity, String urlPostfix, ParameterizedTypeReference referenceType);


  //  Object doGet(String requestParams);

   // RO doGet(Object entity, String urlPostfix);

    Object doGet(String urlPostfix, ParameterizedTypeReference responseType);

    /**
     * Creates a get request with url postfix
     *
     * @param urlPostfix
     * @return
     */
    Object find(String urlPostfix, ParameterizedTypeReference responseType);

    /**
     * Delete <code>E</code> by id.
     *
     * @param id - Entity id
     * @return
     */
    RO delete(String id);
    /**
     * Creates a delete request with url postfix
     *
     * @param urlPostfix
     * @return
     */
    RO delete(String id,String urlPostfix);
    /**
     * Delete <code>E</code> by id.
     *
     * @param id - Entity id
     * @param force - Force
     * @return
     */
    RO delete(String id,boolean force);
    /**
     * Update <code>E</code>
     *
     * @param entity - Entity
     * @return
     */
    RO update(E entity);

    /**
     * Creates a update request with url postfix
     *
     * @param urlPostfix
     * @return
     */
    RO update(E entity, String id, String urlPostfix);
    
    /**
     * Creates a update request with url postfix & Id
     *
     * @param urlPostfix
     * @return
     */
    RO update(String id, String urlPostfix);
}
