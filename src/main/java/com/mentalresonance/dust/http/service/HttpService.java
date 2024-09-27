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

import com.google.gson.Gson;
import com.mentalresonance.dust.core.actors.ActorRef;
import com.mentalresonance.dust.core.services.SerializationService;
import com.mentalresonance.dust.http.msgs.StreamingHttpDataMsg;
import com.mentalresonance.dust.http.msgs.StreamingHttpEndMsg;
import com.mentalresonance.dust.http.msgs.StreamingHttpFailureMsg;
import com.mentalresonance.dust.http.msgs.StreamingHttpStartMsg;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.LinkedHashMap;
import static okhttp3.sse.EventSources.createFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;
import java.util.function.Function;


/**
 * Low level convenience wrapper around OkHttp3
 */
@Slf4j
public class HttpService {

    /**
     * POST method
     */
    public static final String POST = "POST";
    /**
     * PUT method
     */
    public static final String PUT = "PUT";
    /**
     * GET method
     */
    public static final String GET = "GET";
    /**
     * DELETE method
     */
    public static final String DELETE = "DELETE";
    /**
     * HEAD method
     */
    public static final String HEAD = "HEAD";
    /**
     * Default user Agent header
     */
    static final String defaultUserAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:91.0) Gecko/20100101 Firefox/91.0";

    static OkHttpClient httpClient;

    static {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        JavaNetCookieJar cookieJar = new JavaNetCookieJar(cookieManager);

        //noinspection KotlinInternalInJava
        httpClient = new OkHttpClient.Builder()
                .readTimeout(60*1000L, TimeUnit.MILLISECONDS)
                .writeTimeout(60*1000L, TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                .cookieJar(cookieJar)
                .build();
    }

    /**
     * Constructor
     */
    public HttpService() {}

    /**
     * Build a okhttp3 Request object. If headers does not contain User-Agent one is added
     * @param url - the url
     * @param method - the method
     * @param body - body of request
     * @param headers - for the request
     * @return the request
     */
    public static Request buildRequest(
            String url,
            String method,
            RequestBody body,
            Map<String, String> headers) {

        Request.Builder builder = new Request.Builder()
                .url(url);

        Request.Builder finalBuilder = builder;
        headers.forEach(finalBuilder::addHeader);

        if (! headers.containsKey("User-Agent")) {
            finalBuilder.addHeader("User-Agent", defaultUserAgent);
        }

        switch (method) {
            case HttpService.POST -> builder = builder.post(body);
            case HttpService.PUT -> builder = builder.put(body);
            case HttpService.DELETE -> builder = body != null ? builder.delete(body) : builder.delete();
            case HttpService.GET -> builder = builder.get();
            case HttpService.HEAD -> builder = builder.head();
            default -> {
                log.error("Bad method");
                return null;
            }
        }
        return builder.build();
    }

    /**
     * Build get request with given header
     * @param url url
     * @param headers headers to use
     * @return Request
     */
    public static Request buildGetRequest(String url, Map<String, String> headers) {
        return buildRequest(url, GET, null, headers);
    }

    /**
     * Build get request with default headers
     * @param url url
     * @return Request
     */
    public static Request buildGetRequest(String url) {
        return buildRequest(url, GET, null, new LinkedHashMap<String, String>());
    }

    /** Build a POST request
     *
     * @param url to post to
     * @param body to post
     * @param headers to use
     * @return a Request object
     */
    public static Request buildPostRequest(String url, String body, Map<String, String> headers) {
        RequestBody rb = RequestBody.create(body, MediaType.get("application/json"));
        return buildRequest(url, POST, rb, headers);
    }

    /** Build a POST request. Use default headers.
     *
     * @param url to post to
     * @param body to post
     * @return a Request object
     */
    public static Request buildPostRequest(String url, String body) {
        RequestBody rb = RequestBody.create(body, MediaType.get("application/json"));
        return buildRequest(url, POST, rb, new LinkedHashMap<String, String>());
    }
    /** Build a POST request whose Body is JSON
     *
     * @param url to post to
     * @param body to post. Sent as JSON.
     * @param headers to use
     * @return a Request object
     */
    public static Request buildPostRequest(String url, Map<String, Object> body, Map<String, String> headers) {
        RequestBody rb = RequestBody.create(new Gson().toJson(body), MediaType.get("application/json"));
        return buildRequest(url, POST, rb, headers);
    }
    /** Build a POST request whose Body is JSON. Use default headers
     *
     * @param url to post to
     * @param body to post. Sent as JSON.
     * @return a Request object
     */
    public static Request buildPostRequest(String url, LinkedHashMap<String, Object> body) {
        RequestBody rb = RequestBody.create(new Gson().toJson(body), MediaType.get("application/json"));
        return buildRequest(url, POST, rb, new LinkedHashMap<String, String>());
    }

    /**
     * Convenient query params adder
     * @param path of url (shcheme, host, port, path)
     * @param params key-values to add
     * @return Completed url
     */
    public static String buildUrl(String path, Map<String, String> params) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(path)).newBuilder();

        for( String key: params.keySet()) {
            String val = params.get(key);
            if (null != val)
                urlBuilder.addQueryParameter(key, val);
        }
        return urlBuilder.build().toString();
    }

    /**
     * Todo: serialization's default Json does not map maps and lists cleanly ... don't use for now
     *  if you are relying on it for API communication etc.
     * @param url of request
     * @param body of request
     * @return Request
     */
    public static Request buildJsonPostRequest(String url, Serializable body) {
        RequestBody rb = RequestBody.create(SerializationService.writeJson(body), MediaType.get("application/json"));
        return buildRequest(url, POST, rb, new LinkedHashMap<String, String>());
    }

    /**
     * Synchronous request -- it is the caller's responsibility to close the response after processing it.
     *
     * @param request to perform
     * @return the Response
     * @throws ExecutionException if error
     * @throws InterruptedException if interrupted
     */
    public static Response doRequest(Request request) throws ExecutionException, InterruptedException {
        CompletableFuture<Response> cf = new CompletableFuture<Response>();

        httpClient
            .newCall(request)
            .enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    cf.completeExceptionally(e);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response)  {
                    cf.complete(response);
                }
            });
        return cf.get();
    }

    /**
     * Asynchronous request
     * @param request to perform
     * @param succeed called on success passing in Response
     * @param fail called on error passing in exception
     */
    public static void doRequest(
            Request request,
            Function<Response, Void> succeed,
            Function<IOException, Void> fail
    ) {

        httpClient
            .newCall(request)
            .enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    fail.apply(e);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    succeed.apply(response);
                }
            });
    }

    /**
     * Server Sent request. Initiates the request and passes the server-sent events to the listener
     * @param request which initiates the stream of server sent events
     * @param server processes request and generates a series of StreamingHttpMsgs sent to
     * @param client recipient of StreamingHttpMsgs Msgs
     * @return Event source. This can be cancelled at any time, closing the stream.
     */
    public static EventSource doRequest(
            Request request,
            ActorRef client,
            ActorRef server
    ) {
        EventSource.Factory factory = createFactory(httpClient);
        return factory.newEventSource(request, new MyEventSourceListener(client, server));
    }

    static class MyEventSourceListener extends EventSourceListener {
        ActorRef client, server;

        public MyEventSourceListener(ActorRef client, ActorRef server) {
            this.client = client;
            this.server = server;
        }
        @Override
        public void onClosed(@NotNull EventSource eventSource) {
            client.tell(new StreamingHttpEndMsg(), null);
            server.tell(new StreamingHttpEndMsg(), null);
        }

        @Override
        public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
            client.tell( new StreamingHttpDataMsg(id, type, data), null);
        }

        @Override
        public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
            client.tell( new StreamingHttpFailureMsg(t, response), null);
            server.tell( new StreamingHttpFailureMsg(t, response), null);
        }

        /*
         Note - we pass the server as the sender - so a client knows who the server is and can
         interrupt it if needed
         */
        @Override
        public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
            client.tell(new StreamingHttpStartMsg(), server);
        }
    }
}

