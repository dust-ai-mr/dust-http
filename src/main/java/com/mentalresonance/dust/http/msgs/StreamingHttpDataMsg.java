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

package com.mentalresonance.dust.http.msgs;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * A server-sent data packet
 */
@Getter
public class StreamingHttpDataMsg implements Serializable {
    /**
     * Id from streaming source
     */
    String id;
    /**
     * Type from streaming source
     */
    String type;
    /**
     * Data from streaming source
     */
    String data;

    /**Constructor
     * @param id of packet
     * @param type of packet
     * @param data from packet
     */
    public StreamingHttpDataMsg(@Nullable String id, @Nullable String type, @NotNull String data) {
        this.id = id;
        this.type = type;
        this.data = data;
    }
}
