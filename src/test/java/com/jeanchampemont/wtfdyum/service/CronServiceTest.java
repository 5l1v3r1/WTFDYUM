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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jeanchampemont.wtfdyum.WTFDYUMApplication;
import com.jeanchampemont.wtfdyum.dto.Event;
import com.jeanchampemont.wtfdyum.dto.Feature;
import com.jeanchampemont.wtfdyum.dto.Principal;
import com.jeanchampemont.wtfdyum.dto.User;
import com.jeanchampemont.wtfdyum.dto.type.EventType;
import com.jeanchampemont.wtfdyum.dto.type.UserLimitType;
import com.jeanchampemont.wtfdyum.service.feature.FeaturesService;
import com.jeanchampemont.wtfdyum.service.impl.CronServiceImpl;
import com.jeanchampemont.wtfdyum.utils.WTFDYUMException;
import com.jeanchampemont.wtfdyum.utils.WTFDYUMExceptionType;

/**
 * The Class CronServiceTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WTFDYUMApplication.class)
public class CronServiceTest {

    /** The principal service. */
    @Mock
    private PrincipalService principalService;

    /** The user service. */
    @Mock
    private UserService userService;

    /** The twitter service. */
    @Mock
    private TwitterService twitterService;
    
    /** The features service. */
    @Mock
    private FeaturesService featuresService;

    /** The system under test. */
    private CronService sut;

    /**
     * Inits the test.
     */
    @Before
    public void _init() {
        initMocks(this);
        sut = new CronServiceImpl(principalService, userService, twitterService, featuresService);
    }

    /**
     * Check credentials test.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void checkCredentialsTest() throws Exception {
        final Principal principal = principal(1L);
        when(twitterService.verifyCredentials(principal)).thenReturn(true);

        sut.checkCredentials();
        verify(userService, times(1)).resetLimit(1L, UserLimitType.CREDENTIALS_INVALID);
    }

    /**
     * Check credentials test invalid.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void checkCredentialsTestInvalid() throws Exception {
        final Principal principal = principal(1L);
        when(twitterService.verifyCredentials(principal)).thenReturn(false);

        sut.checkCredentials();

        verify(userService, times(1)).addEvent(1L, new Event(EventType.INVALID_TWITTER_CREDENTIALS, ""));
        verify(userService, times(1)).applyLimit(1L, UserLimitType.CREDENTIALS_INVALID);
    }

    @Test
    public void cronTestDisabled() throws Exception {
        principal(5L);
        featureEnabled(5L, Feature.NOTIFY_UNFOLLOW, false);

        sut.cron();
    }

    @Test
    public void cronTestNominal() throws Exception {
        final Principal principal = principal(1L);
        featureEnabled(1L, Feature.NOTIFY_UNFOLLOW, true);

        sut.cron();
        
        verify(featuresService, times(1)).cron(1L, Feature.NOTIFY_UNFOLLOW);
        verify(featuresService, times(1)).completeCron(1L, Feature.NOTIFY_UNFOLLOW);
    }

    /**
     * Find unfollowers test npe error.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void findUnfollowersTestNPEError() throws Exception {
        principal(4L);
        featureEnabled(4L, Feature.NOTIFY_UNFOLLOW, true);

        when(featuresService.cron(4L, Feature.NOTIFY_UNFOLLOW)).thenThrow(new NullPointerException());

        sut.cron();

        // should have an event UNKNOWN_ERROR
        verify(userService, times(1)).addEvent(4L, new Event(EventType.UNKNOWN_ERROR, null));
    }

    /**
     * Find unfollowers test rate limit error.
     *
     * @throws Exception the exception
     */
    @Test
    public void findUnfollowersTestRateLimitError() throws Exception {
    	
    	principal(3L);
        featureEnabled(3L, Feature.NOTIFY_UNFOLLOW, true);

        when(featuresService.cron(3L, Feature.NOTIFY_UNFOLLOW)).thenThrow(new WTFDYUMException(WTFDYUMExceptionType.GET_FOLLOWERS_RATE_LIMIT_EXCEEDED));

        sut.cron();

        // should have an event RATE_LIMIT_EXCEEDED
        verify(userService, times(1)).addEvent(3L, new Event(EventType.RATE_LIMIT_EXCEEDED, null));
    }

    /**
     * Find unfollowers test twitter error.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void findUnfollowersTestTwitterError() throws Exception {
    	principal(2L);
        featureEnabled(2L, Feature.NOTIFY_UNFOLLOW, true);

        when(featuresService.cron(2L, Feature.NOTIFY_UNFOLLOW)).thenThrow(new WTFDYUMException(WTFDYUMExceptionType.TWITTER_ERROR));

        sut.cron();

        // should have an event TWITTER_ERROR
        verify(userService, times(1)).addEvent(2L, new Event(EventType.TWITTER_ERROR, null));
    }

    /**
     * Feature enabled.
     *
     * @param userId
     *            the user id
     * @param feature
     *            the feature
     * @param value
     *            the value
     */
    private void featureEnabled(final long userId, final Feature feature, final boolean value) {
        when(userService.getEnabledFeatures(userId)).thenReturn(new HashSet<>(Arrays.asList(feature)));
    }

    /**
     * Principal.
     *
     * @param id
     *            the id
     * @return the principal
     */
    private Principal principal(final long id) {
        when(principalService.getMembers()).thenReturn(new HashSet<>(Arrays.asList(id)));
        final Principal principal = new Principal(id, "Principal 1 Token", "Principal 1 Token Secret");
        when(principalService.get(id)).thenReturn(principal);
        return principal;
    }
}
