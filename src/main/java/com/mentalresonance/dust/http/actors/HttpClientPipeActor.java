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

package com.mentalresonance.dust.http.actors;


import com.mentalresonance.dust.core.actors.Actor;
import com.mentalresonance.dust.core.actors.ActorBehavior;
import com.mentalresonance.dust.core.actors.Props;
import com.mentalresonance.dust.http.service.HttpRequestResponseMsg;
import com.mentalresonance.dust.http.trait.HttpClientActor;

import java.util.Objects;

/**
 * A convenience implementation of an {@link HttpClientActor} used within Dust Pipelines (see dust-core repo).
 * When sent an {@link HttpRequestResponseMsg} it performs the http request and then sends the response to its
 * <b>parent</b>. Thus HttpClientPipeActors are useful at the front end of pipes where they receive an http request and
 * send the response on its way down the pipe.
 */
public class HttpClientPipeActor extends Actor implements HttpClientActor {

    /**
     * Create the Props
     * @return Props
     */
    public static Props props() {
        return Props.create(HttpClientPipeActor.class);
    }

    /**
     * Constructor
     */
    public HttpClientPipeActor() {}

    @Override
    protected ActorBehavior createBehavior() {
        return message -> {
            if (Objects.requireNonNull(message) instanceof HttpRequestResponseMsg msg) {
                if (sender == self) {
                    parent.tell(msg, self);
                } else
                    request(new HttpRequestResponseMsg(self, msg.request, msg.tag));
                /*
                 * Let super handle all the rest
                 */
            } else {
                super.createBehavior().onMessage(message);
            }
        };
    }
}
