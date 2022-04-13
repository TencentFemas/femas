/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.tencent.tsf.femas.entity.trace.skywalking;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DetectPoint {
    SERVER(0), CLIENT(1), PROXY(2), UNRECOGNIZED(3);

    private final int value;


    public static DetectPoint valueOf(int value) {
        switch (value) {
            case 0:
                return SERVER;
            case 1:
                return CLIENT;
            case 2:
                return PROXY;
            default:
                return UNRECOGNIZED;
        }
    }

    public int value() {
        return this.value;
    }
}
