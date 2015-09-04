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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jeanchampemont.wtfdyum.WTFDYUMApplication;
import com.jeanchampemont.wtfdyum.dto.Event;
import com.jeanchampemont.wtfdyum.dto.Feature;
import com.jeanchampemont.wtfdyum.dto.type.EventType;
import com.jeanchampemont.wtfdyum.dto.type.UserLimitType;
import com.jeanchampemont.wtfdyum.service.feature.FeaturesService;
import com.jeanchampemont.wtfdyum.service.impl.UserServiceImpl;

/**
 * The Class UserServiceTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WTFDYUMApplication.class)
public class UserServiceTest {

    /** The system under test. */
    private UserService sut;

    /** The long redis template mock. */
    @Mock
    private RedisTemplate<String, Long> longRedisTemplate;

    /** The Event redis template mock. */
    @Mock
    private RedisTemplate<String, Event> eventRedisTemplate;

    /** The Feature redis template mock. */
    @Mock
    private RedisTemplate<String, Feature> featureRedisTemplate;

    /** The Long Set operations. */
    @Mock
    private SetOperations<String, Long> longSetOperations;

    /** The long value operations. */
    @Mock
    private ValueOperations<String, Long> longValueOperations;

    /** The Feature Set operations. */
    @Mock
    private SetOperations<String, Feature> featureSetOperations;

    /** The event list operations. */
    @Mock
    private ListOperations<String, Event> eventListOperations;
    
    /** The features service. */
    @Mock
    private FeaturesService featuresService;

    /** The clock. */
    private final Clock clock = Clock.fixed(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneId.of("Z"));

    /**
     * Inits the test.
     */
    @Before
    public void _init() {
        initMocks(this);
        sut = new UserServiceImpl(eventRedisTemplate, featureRedisTemplate, longRedisTemplate, featuresService, clock);
    }

    /**
     * Adds the event test.
     */
    @Test
    public void addEventTest() {
        when(eventRedisTemplate.opsForList()).thenReturn(eventListOperations);

        final Event event = new Event(EventType.REGISTRATION, "data");

        sut.addEvent(31L, event);

        verify(eventListOperations, times(1)).leftPush("EVENTS_31", event);
        assertThat(event.getCreationDateTime()).isEqualTo(LocalDateTime.now(clock));
    }

    /**
     * Apply limit test not reached.
     */
    @Test
    public void applyLimitTestNotReached() {
        when(longRedisTemplate.opsForValue()).thenReturn(longValueOperations);
        when(longValueOperations.increment(UserLimitType.CREDENTIALS_INVALID.name() + "_442", 1)).thenReturn(4L);

        final boolean result = sut.applyLimit(442L, UserLimitType.CREDENTIALS_INVALID);

        assertThat(result).isFalse();
    }

    /**
     * Apply limit test reached.
     */
    @Test
    public void applyLimitTestReached() {
        when(longRedisTemplate.opsForValue()).thenReturn(longValueOperations);
        when(eventRedisTemplate.opsForList()).thenReturn(eventListOperations);
        when(longValueOperations.increment(UserLimitType.CREDENTIALS_INVALID.name() + "_442", 1)).thenReturn(5L);

        final boolean result = sut.applyLimit(442L, UserLimitType.CREDENTIALS_INVALID);

        assertThat(result).isTrue();
        for (final Feature f : Feature.values()) {
            verify(featuresService, times(1)).disableFeature(442L, f);
        }
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventListOperations, times(1)).leftPush(eq("EVENTS_442"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().getType()).isEqualTo(EventType.CREDENTIALS_INVALID_LIMIT_REACHED);
        assertThat(eventCaptor.getValue().getAdditionalData()).isEmpty();
        assertThat(eventCaptor.getValue().getCreationDateTime()).isEqualTo(LocalDateTime.now(clock));
    }

    /**
     * Gets the enabled features test.
     *
     * @return the enabled features test
     */
    @Test
    public void getEnabledFeaturesTest() {
    	when(featureRedisTemplate.opsForSet()).thenReturn(featureSetOperations);
    	when(featureSetOperations.members("FEATURES_1234")).thenReturn(new HashSet<>(Arrays.asList(Feature.NOTIFY_UNFOLLOW, Feature.TWEET_UNFOLLOW)));
    	
    	final Set<Feature> result = sut.getEnabledFeatures(1234L);
    	
    	assertThat(result).isEqualTo(new HashSet<>(Arrays.asList(Feature.NOTIFY_UNFOLLOW, Feature.TWEET_UNFOLLOW)));
    }

    /**
     * Gets the recent events test.
     *
     * @return the recent events test
     */
    @Test
    public void getRecentEventsTest() {
        final List<Event> result = Arrays.asList(new Event(EventType.REGISTRATION, "reg"),
                new Event(EventType.UNFOLLOW, "unfoll"));

        when(eventRedisTemplate.opsForList()).thenReturn(eventListOperations);
        when(eventListOperations.range("EVENTS_1249", 0, 12)).thenReturn(result);

        final List<Event> returnedResult = sut.getRecentEvents(1249L, 12);

        assertThat(returnedResult).isNotNull();
        assertThat(returnedResult.size()).isEqualTo(2);
        assertThat(returnedResult).isEqualTo(result);
    }

    /**
     * Reset limit test.
     */
    @Test
    public void resetLimitTest() {

        sut.resetLimit(199L, UserLimitType.CREDENTIALS_INVALID);

        verify(longRedisTemplate, times(1)).delete(UserLimitType.CREDENTIALS_INVALID.name() + "_199");
    }
}
