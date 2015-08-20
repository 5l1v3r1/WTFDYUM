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
package com.jeanchampemont.wtfdyum.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.jeanchampemont.wtfdyum.dto.User;
import com.jeanchampemont.wtfdyum.service.AuthenticationService;
import com.jeanchampemont.wtfdyum.service.TwitterService;
import com.jeanchampemont.wtfdyum.service.UserService;
import com.jeanchampemont.wtfdyum.utils.WTFDYUMException;

import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * WTFDYUM Main controller.
 *
 * @author Jean
 */
@Controller
public class MainController {

    /** The Constant SESSION_REQUEST_TOKEN. */
    static final String SESSION_REQUEST_TOKEN = "requestToken";

    /** The twitter service. */
    @Autowired
    private TwitterService twitterService;

    /** The user service. */
    @Autowired
    private UserService userService;

    /** The authentication service. */
    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Show the index page.
     *
     * @return "index"
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "index";
    }

    /**
     * Signin.
     *
     * @param request
     *            the request
     * @return the redirect view
     * @throws WTFDYUMException
     *             the WTFDYUM exception
     */
    @RequestMapping(value = "/signin", method = RequestMethod.GET)
    public RedirectView signin(final HttpServletRequest request) throws WTFDYUMException {
        if (authenticationService.isAuthenticated()) {
            return new RedirectView("/user", true);
        }
        final RequestToken requestToken = twitterService.signin("/signin/callback");

        request.getSession().setAttribute(SESSION_REQUEST_TOKEN, requestToken);

        return new RedirectView(requestToken.getAuthenticationURL());
    }

    /**
     * Signin callback.
     *
     * @param verifier
     *            the verifier
     * @param request
     *            the request
     * @return the string
     * @throws WTFDYUMException
     *             the WTFDYUM exception
     */
    @RequestMapping(value = "/signin/callback", method = RequestMethod.GET)
    public RedirectView signinCallback(@RequestParam("oauth_verifier") final String verifier,
            final HttpServletRequest request) throws WTFDYUMException {
        final RequestToken requestToken = (RequestToken) request.getSession().getAttribute(SESSION_REQUEST_TOKEN);
        request.getSession().removeAttribute(SESSION_REQUEST_TOKEN);

        final AccessToken accessToken = twitterService.completeSignin(requestToken, verifier);

        final User user = new User(accessToken.getUserId(), accessToken.getToken(), accessToken.getTokenSecret());
        userService.saveUpdate(user);
        authenticationService.authenticate(user);

        return new RedirectView("/user", true);
    }
}
