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
package com.jeanchampemont.wtfdyum.service;

import com.jeanchampemont.wtfdyum.WTFDYUMApplication;
import com.jeanchampemont.wtfdyum.dto.Principal;
import com.jeanchampemont.wtfdyum.service.impl.PrincipalServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WTFDYUMApplication.class)
public class PrincipalServiceTest {

    private PrincipalService sut;

    @Mock
    private RedisTemplate<String, Long> longRedisTemplate;

    @Mock
    private RedisTemplate<String, Principal> principalRedisTemplate;

    @Mock
    private ValueOperations<String, Principal> valueOperations;

    @Mock
    private SetOperations<String, Long> setOperations;

    @Before
    public void ainit() {
        initMocks(this);
        sut = new PrincipalServiceImpl(principalRedisTemplate, longRedisTemplate);
    }

    @Test
    public void countMembersTest() {
        when(longRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.size("MEMBERS")).thenReturn(133L);

        final int result = sut.countMembers();

        assertThat(result).isEqualTo(133);
    }

    @Test
    public void getMembersTest() {
        when(longRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("MEMBERS")).thenReturn(new HashSet<Long>(Arrays.asList(12L, 13L, 190L)));

        final Set<Long> members = sut.getMembers();

        assertThat(members).isNotNull();
        assertThat(members.size()).isEqualTo(3);
        assertThat(members.contains(12L)).isTrue();
        assertThat(members.contains(13L)).isTrue();
        assertThat(members.contains(190L)).isTrue();
    }

    @Test
    public void getTest() {
        final Principal u = new Principal(12L, "tokdf", "secrrr");

        when(principalRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("190")).thenReturn(u);

        final Principal principal = sut.get(190L);

        assertThat(principal).isNotNull();
        assertThat(principal).isEqualTo(u);
    }

    @Test(expected = NullPointerException.class)
    public void getTestNullId() {
        sut.get(null);
    }

    @Test(expected = NullPointerException.class)
    public void saveUpdateNullUserIdTest() {
        sut.saveUpdate(new Principal(null, "lkdf", "sec"));
    }

    @Test(expected = NullPointerException.class)
    public void saveUpdateNullUserTest() {
        sut.saveUpdate(null);
    }

    @Test
    public void saveUpdateTest() {
        final Principal u = new Principal(12L, "tokdf", "secrrr");

        when(principalRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(longRedisTemplate.opsForSet()).thenReturn(setOperations);

        sut.saveUpdate(u);

        verify(valueOperations, times(1)).set("12", u);
        verify(setOperations, times(1)).add("MEMBERS", 12L);
    }
}
