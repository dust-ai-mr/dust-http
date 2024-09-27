/*
 * Copyright 2024 Alan Littleford
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mentalresonance.dust.http.service;

import com.mentalresonance.dust.core.actors.ActorRef;
import com.mentalresonance.dust.core.msgs.ProxyMsg;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.Serializable;

/**
 * Generic request/response message for use in HttpClientActor.
 */
public class HttpRequestResponseMsg extends ProxyMsg {

    /**
     * Request to perform
     */
    public Request request;
    /**
     * Response from that request
     */
    public Response response;
    /**
     * Exception if it error'd
     */
    public IOException exception = null;
    /**
     * Optional 'companion' message - just returned as is. Usually the message that resulted in the HttpRequest.
     */
    public Serializable tag = null;

    /**
     * Constructor
     * @param sender of request
     * @param request to perform
     */
    public HttpRequestResponseMsg(ActorRef sender, Request request) {
        super(sender);
        this.request = request;
    }
    /**
     * Constructor
     * @param sender of request
     * @param request to perform
     * @param tag to tag along
     */
    public HttpRequestResponseMsg(ActorRef sender, Request request, Serializable tag) {
        super(sender);
        this.request = request;
        this.tag = tag;
    }

    /**
     * The default key is the host of the request
     * @return host part of request url
     */
    @Override
    public String key() {
        return request.url().host();
    }
}
