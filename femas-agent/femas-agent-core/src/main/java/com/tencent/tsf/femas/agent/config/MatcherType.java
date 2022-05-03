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
 */
package com.tencent.tsf.femas.agent.config;

/**
 * @Author leoziltong
 * @Date: 2022/4/8 17:12
 * @Version 1.0
 */
public enum MatcherType {
    /**
     * 精准匹配
     */
    EXACT_MATCH("exactMatch"),
    /**
     * 前缀
     */
    PREFIX("prefix"),

    /**
     * 前缀
     */
    CONTAIN("contain"),
    /**
     * 后缀匹配
     */
    SUFFIX("suffix");

    MatcherType(String type) {
        this.type = type;
    }

    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
