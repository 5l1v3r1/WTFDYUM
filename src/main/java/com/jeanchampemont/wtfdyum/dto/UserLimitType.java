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
package com.jeanchampemont.wtfdyum.dto;

/**
 * The Enum UserLimitType.
 */
public enum UserLimitType {

    /** The twitter exception. */
    CREDENTIALS_INVALID(5);

    /**
     * Instantiates a new user limit type.
     *
     * @param limitValue
     *            the limit value
     */
    private UserLimitType(final int limitValue) {
        this.limitValue = limitValue;
    }

    /** The limit value. */
    private int limitValue;

    /**
     * Gets the limit value.
     *
     * @return the limit value
     */
    public int getLimitValue() {
        return limitValue;
    }
}
