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
$(function() {
    $('.option').on('change', function() {
        if($(this).is(':checked')) {
            window.location.href = "/user/feature/enable/" + $(this).data('name');
        } else {
            window.location.href = "/user/feature/disable/" + $(this).data('name');
        }
    });

    $('#more-button').on('click', function() {
        $.get("/ajax/recentEvents/" + ($('#events div.alert').length + 1), function(events) {
            if(events.trim().length == 0) {
                $('#more-button').hide();
            }
            $('#events').append(events);
        });
    });
});
