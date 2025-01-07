/*
 * Copyright 2024-2025 Alan Littleford
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

package com.mentalresonance.dust.http.trait;

import com.mentalresonance.dust.core.actors.ActorRef;
import com.mentalresonance.dust.core.actors.ActorTrait;
import com.mentalresonance.dust.http.service.HttpRequestResponseMsg;
import com.mentalresonance.dust.http.service.HttpService;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * Takes an {@link HttpRequestResponseMsg} and dispatches to the HttpService then reply with
 * filled in message to sender. Create a customized HttpClient by having a class implement this interface
 * which calls request() and handles {@link HttpRequestResponseMsg} in its behavior.
 * <br/><br/>
 * See WebClient test for a simple example.
 */
public interface HttpClientActor extends ActorTrait {

    /**
     * Generic request
     * @param msg Request/Response container - {@link HttpRequestResponseMsg}
     */
    default void request(HttpRequestResponseMsg msg) {
        HttpService.doRequest(
            msg.request,
                (Response response) -> {
                    msg.response = response;
                    msg.getSender().tell(msg, getSelf());
                    return null;
                } ,
                (IOException e) -> {
                    msg.exception = e;
                    msg.getSender().tell(msg, getSelf());
                    return null;
                }
        );
    }

    /**
     * Simple case - GET on url
     * @param url - the url
     */
    default void request(String url) {
        request(new HttpRequestResponseMsg(getSelf(), HttpService.buildGetRequest(url)));
    }
    /**
     * Simple case - GET on url with headers
     * @param url  the url
     * @param headers headers to provide. UserAgent will be added if not in headers
     */
    default void request(String url, LinkedHashMap<String, String> headers) {
        request(new HttpRequestResponseMsg(getSelf(), HttpService.buildGetRequest(url, headers)));
    }
    /**
     * Simple case - GET on url with tag object
     * @param url - the url
     * @param tag - an object which tags along with the request/response
     */
    default void request(String url, Serializable tag) {
        request(new HttpRequestResponseMsg(getSelf(), HttpService.buildGetRequest(url), tag));
    }

    /**
     * Do a Streaming (Server Sent) request. To keep things consistent we wrap the underlying
     * Http Request (which is all we need) in a RequestResponse.
     * <p>
     * This is sent to the server which will then send {@link com.mentalresonance.dust.http.msgs.StreamingHttpStartMsg},
     * a series of {@link com.mentalresonance.dust.http.msgs.StreamingHttpDataMsg}s and finally a
     * {@link com.mentalresonance.dust.http.msgs.StreamingHttpEndMsg}. If any errors occur a
     * {@link com.mentalresonance.dust.http.msgs.StreamingHttpFailureMsg} will be sent.
     * </p>
     * @param msg the request which is sent to
     * @param server Actor requesting the stream which it sends as message back to
     * @param client the client Actor
     * @return The EventSource which we can cancel
     */
    default EventSource request(HttpRequestResponseMsg msg, ActorRef client, ActorRef server) {
        return HttpService.doRequest(msg.request, client, server);
    }
}
