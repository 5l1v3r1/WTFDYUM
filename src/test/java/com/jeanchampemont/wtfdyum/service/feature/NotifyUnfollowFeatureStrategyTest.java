/*
 * Copyright (C) 2015, 2016 WTFDYUM
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
package com.jeanchampemont.wtfdyum.service.feature;

import com.jeanchampemont.wtfdyum.WTFDYUMApplication;
import com.jeanchampemont.wtfdyum.dto.Event;
import com.jeanchampemont.wtfdyum.dto.Principal;
import com.jeanchampemont.wtfdyum.dto.User;
import com.jeanchampemont.wtfdyum.dto.type.EventType;
import com.jeanchampemont.wtfdyum.service.feature.impl.NotifyUnfollowFeatureStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WTFDYUMApplication.class)
public class NotifyUnfollowFeatureStrategyTest extends AbstractFeatureStrategyTest {

    @Override
    @Before
    public void _init() {
        super._init();
        sut = new NotifyUnfollowFeatureStrategy(principalService, followersService, twitterService, DM_TEXT);
        ReflectionTestUtils.setField(sut, "featureRedisTemplate", featureRedisTemplate);
    }

    @Test
    public void completeCronTest() throws Exception {
        final Principal principal = principal(1L);

        final Set<Long> followers = followers(principal, (s, f) -> s.thenReturn(f));

        sut.completeCron(1L);

        // New followers list should be saved
        verify(followersService, times(1)).saveFollowers(1L, followers);
    }

    @Test
    public void cronTest() throws Exception {
        final Principal principal = principal(1L);

        final Set<Long> followers = followers(principal, (s, f) -> s.thenReturn(f));

        final List<User> unfollowers = unfollowers(principal, followers, (s, u) -> s.thenReturn(u));

        final Set<Event> events = sut.cron(1L);

        verifyUnfollowDM(principal, unfollowers.get(0));
        verifyUnfollowDM(principal, unfollowers.get(1));

        assertThat(events.contains(new Event(EventType.UNFOLLOW, unfollowers.get(0).getScreenName())));
        assertThat(events.contains(new Event(EventType.UNFOLLOW, unfollowers.get(1).getScreenName())));
    }

    @Test
    public void hasCronTest() {
        assertThat(sut.hasCron()).isTrue();
    }
}
