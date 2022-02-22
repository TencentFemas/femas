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

package com.tencent.tsf.femas.governance.plugin;

/**
 * @Author leoziltong
 * @Date: 2021/5/28 21:35
 */
public class Attribute {

    private Implement type;

    private String name;

    public Attribute() {
    }

    public Attribute(Implement type, String name) {
        this.type = type;
        this.name = name;
    }

    public Implement getType() {
        return type;
    }

    public void setType(Implement type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum Implement {
        FEMAS,
        POLARIS,
        SENTINEL;

    }
}
