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

package com.tencent.tsf.femas.enums;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/5/13 11:21
 */
public enum RegistryStatus {

    RUNNING(1, "运行中"),
    DOWN(2, "异常");
    Integer code;
    String status;

    RegistryStatus(Integer code, String status) {
        this.code = code;
        this.status = status;
    }
}
