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

import java.net.URI;
import javax.websocket.*;

/**
 * Contacts the websocket and hands off websocket callbacks to the given handler
 */
@ClientEndpoint
public class WebsocketClientEndpoint {

    Session userSession = null;
    private WebsocketHandler websocketHandler;

    /**
     * Constructor
     * @param endpointURI web socket endpoint
     * @param handler callback
     */
    public WebsocketClientEndpoint(URI endpointURI, WebsocketHandler handler) {
        try {
            websocketHandler = handler;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
        websocketHandler.onOpen(userSession);
    }
    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {

        websocketHandler.onClose(userSession, reason);
    }
    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {

        websocketHandler.onMessage(message);
    }
    /**
     * Send a message asynchronously.
     *
     * @param message to be sent
     */
    public void sendMessage(String message) {
        userSession.getAsyncRemote().sendText(message);
    }
}

