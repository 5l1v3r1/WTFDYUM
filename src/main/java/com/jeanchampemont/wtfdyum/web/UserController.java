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
package com.jeanchampemont.wtfdyum.web;

import com.jeanchampemont.wtfdyum.dto.Event;
import com.jeanchampemont.wtfdyum.dto.Feature;
import com.jeanchampemont.wtfdyum.dto.type.EventType;
import com.jeanchampemont.wtfdyum.security.Secured;
import com.jeanchampemont.wtfdyum.service.AuthenticationService;
import com.jeanchampemont.wtfdyum.service.FeatureService;
import com.jeanchampemont.wtfdyum.service.TwitterService;
import com.jeanchampemont.wtfdyum.service.UserService;
import com.jeanchampemont.wtfdyum.utils.SessionManager;
import com.jeanchampemont.wtfdyum.utils.WTFDYUMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private TwitterService twitterService;

    @Autowired
    private UserService userService;

    @Autowired
    private FeatureService featureService;

    @RequestMapping(value = "/feature/disable/{feature}", method = RequestMethod.GET)
    @Secured
    public RedirectView disableFeature(@PathVariable("feature") final Feature feature) {
        final Long userId = authenticationService.getCurrentUserId();

        if (featureService.disableFeature(userId, feature)) {
            userService.addEvent(userId, new Event(EventType.FEATURE_DISABLED, feature.getShortName()));
        }

        return new RedirectView("/user", true);
    }

    @RequestMapping(value = "/feature/enable/{feature}", method = RequestMethod.GET)
    @Secured
    public RedirectView enableFeature(@PathVariable("feature") final Feature feature) {
        final Long userId = authenticationService.getCurrentUserId();

        if (featureService.enableFeature(userId, feature)) {
            userService.addEvent(userId, new Event(EventType.FEATURE_ENABLED, feature.getShortName()));
        }

        return new RedirectView("/user", true);
    }

    @RequestMapping(method = RequestMethod.GET)
    @Secured
    public ModelAndView index() {
        final ModelAndView result = new ModelAndView("user/index");

        final Long userId = authenticationService.getCurrentUserId();

        try {
            result.getModel().put("user", twitterService.getUser(SessionManager.getPrincipal(), userId));
        } catch (final WTFDYUMException e) {
            authenticationService.logOut();
            return new ModelAndView("redirect:/");
        }
        result.getModel().put("events", userService.getRecentEvents(userId, 10));
        result.getModel().put("availableFeatures", Feature.values());

        final Map<String, Boolean> featuresStatus = new HashMap<>();
        for (final Feature f : Feature.values()) {
            featuresStatus.put(f.name(), featureService.isEnabled(userId, f));
        }

        result.getModel().put("featuresStatus", featuresStatus);

        return result;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public RedirectView logout() {
        authenticationService.logOut();
        return new RedirectView("/", true);
    }
}
