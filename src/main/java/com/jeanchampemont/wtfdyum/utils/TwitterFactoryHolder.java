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
package com.jeanchampemont.wtfdyum.utils;

import org.springframework.stereotype.Component;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

/**
 * The Class TwitterFactoryHolder.
 *
 * Simple Holder of TwitterFactory to facilitate mocking. (TwitterFactory is
 * final...)
 */
@Component
public class TwitterFactoryHolder {

    /**
     * Instantiates a new twitter factory holder.
     */
    public TwitterFactoryHolder() {
        twitterFactory = new TwitterFactory();
    }

    /** The twitter factory. */
    private final TwitterFactory twitterFactory;

    /**
     * Gets the instance of Twitter.
     *
     * @return instance of Twitter
     */
    public Twitter getInstance() {
        return twitterFactory.getInstance();
    }
}
