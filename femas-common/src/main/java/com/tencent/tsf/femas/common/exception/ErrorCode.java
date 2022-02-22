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

package com.tencent.tsf.femas.common.exception;

/**
 * @Author leoziltong
 * @Date: 2021/5/25 16:30
 */
public enum ErrorCode {
    /**
     * 获取插件失败
     */
    PLUGIN_NOT_FOUND(1003),
    /**
     * 插件初始化错误
     */
    PLUGIN_INIT_ERROR(1004);

    int code;

    ErrorCode(int code) {
        this.code = code;
    }


}
