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
import com.mentalresonance.dust.core.actors.Props;
import com.mentalresonance.dust.http.service.WebsocketClientEndpoint;
import com.mentalresonance.dust.http.service.WebsocketHandler;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.net.URI;

/**
 * An Actor to be subclassed. It takes the URI of a websocket server and provides callbacks for onOpen, onClose
 * and onMessage
 */
abstract public class WebsocketClientActor extends Actor implements WebsocketHandler {

    URI uri;
    WebsocketClientEndpoint endpoint;

    /**
     * Create the Actor as a client ot
     * @param uri the websocket server
     * @return Props to construct the Actor
     */
    public static Props props(URI uri) {
        return Props.create(WebsocketClientActor.class, uri);
    }
    /**
     * Constructor
     * @param uri of Websocket Server
     */
    public WebsocketClientActor(URI uri) {
        this.uri = uri;
        endpoint = new WebsocketClientEndpoint(uri, this);
    }
    /**
     * To be overridden
     * @param userSession session returned by server
     */
    public abstract void onOpen(Session userSession);
    /**
     * To be overridden
     * @param userSession session returned by server
     * @param reason for closure
     */
    public abstract void onClose(Session userSession, CloseReason reason);
    /**
     * To be overridden
     * @param message from the websocket server
     */
    public abstract void onMessage(String message);
    /**
     * Send a message to the server
     * @param msg the message
     */
    protected void sendMessage(String msg) {
        endpoint.sendMessage(msg);
    }
}
