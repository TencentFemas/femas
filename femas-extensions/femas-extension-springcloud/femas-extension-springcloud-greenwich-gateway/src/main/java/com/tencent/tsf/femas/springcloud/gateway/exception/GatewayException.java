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
package com.tencent.tsf.femas.springcloud.gateway.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author leoziltong
 * @Date: 2021/12/29 18:27
 */
public class GatewayException extends RuntimeException {

    /**
     * 异常编码
     */
    private String errorCode;
    /**
     * 异常信息
     */
    private String errorMessage;

    public GatewayException() {
    }

    public GatewayException(String errorCode) {
        this(errorCode, (String) null);
    }

    public GatewayException(String errorCode, String errorMessage) {
        this(errorCode, errorMessage, (Throwable) null);
    }

    public GatewayException(String errorCode, Throwable cause) {
        this(errorCode, (String) null, cause);
    }

    public GatewayException(String errorCode, String errorMessage, Throwable cause) {
        super(String.format("(%s)%s", errorCode, String.format(errorMessage)), cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 异常枚举转换为code
     *
     * @param error 异常枚举
     * @return 大驼峰编码
     */
    public static String errorToCode(Enum<?> error) {
        String errorName = error.name().toLowerCase();
        String[] sp = errorName.split("_");
        StringBuffer code = new StringBuffer();
        for (String s : sp) {
            code.append(StringUtils.capitalize(s));
        }
        return code.toString();
    }

    /**
     * 获取原始异常编码
     *
     * @return 异常编码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取原始异常消息
     *
     * @return 异常消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}