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

package com.tencent.tsf.femas.common.util;

import java.nio.charset.StandardCharsets;


/**
 * @Author leoziltong
 * @Date: 2021/4/21 19:01
 */
public class BytesUtil {


    public static final byte[] EMPTY_BYTES = new byte[0];
    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();


    private BytesUtil() {
    }

    public static boolean isEmpty(final byte[] bytes) {
        return bytes == null || bytes.length == 0;
    }

    public static byte[] writeUtf8(final String in) {
        if (in == null) {
            return null;
        }

        return in.getBytes(StandardCharsets.UTF_8);

    }

    public static String readUtf8(final byte[] in) {
        if (in == null) {
            return null;
        }
        return new String(in, StandardCharsets.UTF_8);
    }

    public static String toHex(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(final String s) {
        if (s == null) {
            return null;
        }
        final int len = s.length();
        final byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return bytes;
    }
}
