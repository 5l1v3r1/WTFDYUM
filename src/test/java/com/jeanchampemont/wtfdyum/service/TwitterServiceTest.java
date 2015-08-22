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

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.dozer.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jeanchampemont.wtfdyum.WTFDYUMApplication;
import com.jeanchampemont.wtfdyum.dto.Principal;
import com.jeanchampemont.wtfdyum.service.impl.TwitterServiceImpl;
import com.jeanchampemont.wtfdyum.utils.SessionManager;
import com.jeanchampemont.wtfdyum.utils.TwitterFactoryHolder;
import com.jeanchampemont.wtfdyum.utils.WTFDYUMException;
import com.jeanchampemont.wtfdyum.utils.WTFDYUMExceptionType;

import twitter4j.IDs;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.api.UsersResources;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * The Class TwitterServiceTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WTFDYUMApplication.class)
public class TwitterServiceTest {

    /** The Constant DEFAULT_BASE_URL. */
    private static final String DEFAULT_BASE_URL = "http://url/toto/";

    /** The Constant DEFAULT_PATH. */
    private static final String DEFAULT_PATH = "default/path";

    /** The mapper. */
    @Autowired
    private Mapper mapper;

    /** The system under test. */
    private TwitterService sut;

    /** The twitter mock. */
    @Mock
    private Twitter twitter;

    /** The users resources. */
    @Mock
    private UsersResources usersResources;

    /** The twitter factory. */
    @Mock
    private TwitterFactoryHolder twitterFactory;

    /**
     * Inits the test.
     */
    @Before
    public void ainit() {
        initMocks(this);
        when(twitterFactory.getInstance()).thenReturn(twitter);
        sut = new TwitterServiceImpl(twitterFactory, mapper, DEFAULT_BASE_URL, "appId", "appSecret");
    }

    /**
     * Complete signin test nominal.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void completeSigninTestNominal() throws Exception {
        final AccessToken returnedToken = new AccessToken("TOKTOK", "TOK_secret");

        final RequestToken paramToken = new RequestToken("TOK", "SECRET_tok");
        final String verifier = "VERiFy";

        when(twitter.getOAuthAccessToken(paramToken, verifier)).thenReturn(returnedToken);

        final AccessToken accessToken = sut.completeSignin(paramToken, verifier);

        verify(twitter, times(1)).getOAuthAccessToken(paramToken, verifier);

        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isEqualTo(returnedToken);
    }

    /**
     * Complete signin test twitter exception.
     *
     * @throws Exception
     *             the exception
     */
    @Test(expected = WTFDYUMException.class)
    public void completeSigninTestTwitterException() throws Exception {
        final RequestToken paramToken = new RequestToken("TOK", "SECRET_tok");
        final String verifier = "VERiFy";

        when(twitter.getOAuthAccessToken(paramToken, verifier)).thenThrow(new TwitterException("dummy"));

        sut.completeSignin(paramToken, verifier);

        Assertions.fail("Exception not throwned");
    }

    /**
     * Gets the followers test.
     *
     * @return the followers test
     * @throws Exception
     *             the exception
     */
    @Test
    public void getFollowersTest() throws Exception {
        final Optional<Principal> principal = Optional.of(new Principal(123L, "toktok", "secsecret"));
        final IDs idsMock = mock(IDs.class);
        when(twitter.getFollowersIDs(444L, -1)).thenReturn(idsMock);

        final RateLimitStatus rateLimitStatusMock = mock(RateLimitStatus.class);
        when(idsMock.getRateLimitStatus()).thenReturn(rateLimitStatusMock);

        when(rateLimitStatusMock.getRemaining()).thenReturn(1);

        when(idsMock.getIDs()).thenReturn(new long[]{12L, 34L, 44L, 42L, 42L, 999L});

        final Set<Long> followers = sut.getFollowers(444L, principal);

        assertThat(followers).isNotNull();
        assertThat(followers.contains(12L));
        assertThat(followers.contains(34L));
        assertThat(followers.contains(44L));
        assertThat(followers.contains(42L));
        assertThat(followers.contains(999L));

        verify(twitter, times(1)).setOAuthAccessToken(new AccessToken("toktok", "secsecret"));
    }

    /**
     * Gets the followers test without principal.
     *
     * @return the followers test without principal
     * @throws Exception
     *             the exception
     */
    @Test
    public void getFollowersTestWithoutPrincipal() throws Exception {
        final IDs idsMock = mock(IDs.class);
        when(twitter.getFollowersIDs(444L, -1)).thenReturn(idsMock);

        final RateLimitStatus rateLimitStatusMock = mock(RateLimitStatus.class);
        when(idsMock.getRateLimitStatus()).thenReturn(rateLimitStatusMock);

        when(rateLimitStatusMock.getRemaining()).thenReturn(1);

        when(idsMock.getIDs()).thenReturn(new long[]{12L, 34L, 44L, 42L, 42L, 999L});

        final Set<Long> followers = sut.getFollowers(444L, Optional.<Principal> empty());

        assertThat(followers).isNotNull();
        assertThat(followers.contains(12L));
        assertThat(followers.contains(34L));
        assertThat(followers.contains(44L));
        assertThat(followers.contains(42L));
        assertThat(followers.contains(999L));
    }

    /**
     * Gets the followers test without principal rate limit.
     *
     * @return the followers test without principal rate limit
     * @throws Exception
     *             the exception
     */
    @Test
    public void getFollowersTestWithoutPrincipalRateLimit() throws Exception {
        final IDs idsMock = mock(IDs.class);
        when(twitter.getFollowersIDs(444L, -1)).thenReturn(idsMock);

        final RateLimitStatus rateLimitStatusMock = mock(RateLimitStatus.class);
        when(idsMock.getRateLimitStatus()).thenReturn(rateLimitStatusMock);

        when(rateLimitStatusMock.getRemaining()).thenReturn(0);

        try {
            sut.getFollowers(444L, Optional.<Principal> empty());
            Assertions.failBecauseExceptionWasNotThrown(WTFDYUMException.class);
        } catch (final WTFDYUMException e) {
            assertThat(e.getType()).isEqualTo(WTFDYUMExceptionType.GET_FOLLOWERS_RATE_LIMIT_EXCEEDED);
        }
    }

    /**
     * Gets the followers test without principal twitter exception.
     *
     * @return the followers test without principal twitter exception
     * @throws Exception
     *             the exception
     */
    @Test
    public void getFollowersTestWithoutPrincipalTwitterException() throws Exception {
        when(twitter.getFollowersIDs(444L, -1)).thenThrow(new TwitterException("msg"));

        try {
            sut.getFollowers(444L, Optional.<Principal> empty());
            Assertions.failBecauseExceptionWasNotThrown(WTFDYUMException.class);
        } catch (final WTFDYUMException e) {
            assertThat(e.getType()).isEqualTo(WTFDYUMExceptionType.TWITTER_ERROR);
        }

    }

    /**
     * Gets the user test.
     *
     * @return the user test
     * @throws Exception the exception
     */
    @Test
    public void getUserTest() throws Exception {
        SessionManager.setPrincipal(new Principal(1L, "", ""));

        final User userMock = mock(User.class);

        when(twitter.users()).thenReturn(usersResources);
        when(usersResources.showUser(123L)).thenReturn(userMock);
        when(userMock.getId()).thenReturn(123L);
        when(userMock.getName()).thenReturn("name");
        when(userMock.getScreenName()).thenReturn("screenName");
        when(userMock.getProfileImageURL()).thenReturn("profile img url");
        when(userMock.getURL()).thenReturn("user url");

        final com.jeanchampemont.wtfdyum.dto.User result = sut.getUser(123L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getName()).isEqualTo("name");
        assertThat(result.getScreenName()).isEqualTo("screenName");
        assertThat(result.getProfileImageURL()).isEqualTo("profile img url");
        assertThat(result.getURL()).isEqualTo("user url");
    }

    /**
     * Gets the user test exception.
     *
     * @return the user test exception
     * @throws Exception
     *             the exception
     */
    @Test(expected = WTFDYUMException.class)
    public void getUserTestException() throws Exception {
        SessionManager.setPrincipal(new Principal(1L, "", ""));

        when(twitter.users()).thenReturn(usersResources);
        when(usersResources.showUser(123L)).thenThrow(new TwitterException("msg"));

        sut.getUser(123L);
    }

    /**
     * Signin test nominal.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void signinTestNominal() throws Exception {
        final RequestToken returnedToken = new RequestToken("TOK", "SECRET_tok");

        when(twitter.getOAuthRequestToken(DEFAULT_BASE_URL + DEFAULT_PATH)).thenReturn(returnedToken);

        final RequestToken requestToken = sut.signin(DEFAULT_PATH);

        verify(twitter, times(1)).getOAuthRequestToken(DEFAULT_BASE_URL + DEFAULT_PATH);

        assertThat(requestToken).isNotNull();
        assertThat(requestToken).isEqualTo(returnedToken);
    }

    /**
     * Signin test twitter exception.
     *
     * @throws Exception
     *             the exception
     */
    @Test(expected = WTFDYUMException.class)
    public void signinTestTwitterException() throws Exception {
        when(twitter.getOAuthRequestToken(DEFAULT_BASE_URL + DEFAULT_PATH)).thenThrow(new TwitterException("dummy"));

        sut.signin(DEFAULT_PATH);

        Assertions.fail("Exception not throwned");
    }
}
