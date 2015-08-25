/*
 * Copyright (C) 2015 WTFDYUM
 *
 * This file is part of the WTFDYUM project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeanchampemont.wtfdyum.service;

import java.util.Set;

import com.jeanchampemont.wtfdyum.dto.Principal;

/**
 * The Interface PrincipalService.
 */
public interface PrincipalService {

    /**
     * Count members.
     *
     * @return the number of members
     */
    int countMembers();

    /**
     * Gets the principal.
     *
     * @param id the id
     * @return the principal
     */
    Principal get(Long id);

    /**
     * Gets the members.
     *
     * @return the members
     */
    Set<Long> getMembers();

    /**
     * Save update.
     *
     * @param user
     *            the user
     */
    void saveUpdate(Principal user);
}