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

package com.mentalresonance.dust.http.msgs;

import lombok.Getter;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * A server-sent data packet
 */
@Getter
public class StreamingHttpFailureMsg implements Serializable {
    /**
     * Cause of the error
     */
    Throwable t;
    /**
     * Response
     */
    Response response;

    /**
     * Constructor
     * @param t throwable of the error
     * @param response whatever response was obtained
     */
    public StreamingHttpFailureMsg( @Nullable Throwable t, @Nullable Response response) {
        this.t = t;
        this.response = response;
    }
}
