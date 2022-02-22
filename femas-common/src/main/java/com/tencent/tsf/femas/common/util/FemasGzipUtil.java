/*
 * Tencent is pleased to support the open source community by making Femas available.
 *
 * Copyright (C) 2021, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.tsf.femas.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * @date 2018/9/7 15:35
 */
public class FemasGzipUtil {

    public static byte[] compress(String data, String charsetName) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data.getBytes(charsetName));
        gzip.finish();
        gzip.close();
        byte[] ret = bos.toByteArray();
        bos.close();
        return ret;
    }

    public static String compressBase64Encode(String data, String charsetName) throws IOException {
        byte[] compressData = compress(data, charsetName);
        return new String(Base64.getEncoder().encode(compressData), charsetName);
    }

    public static String compressBase64Encode(byte[] byteData, String charsetName) throws IOException {
        byte[] compressData = compress(new String(byteData, charsetName), charsetName);
        return Base64.getEncoder().encodeToString(compressData);
    }


    public static byte[] decompress(byte[] zipData) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(zipData);
        GZIPInputStream gzip = new GZIPInputStream(bis);
        byte[] buf = new byte[256];
        int num = -1;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((num = gzip.read(buf, 0, buf.length)) != -1) {
            bos.write(buf, 0, num);
        }
        gzip.close();
        bis.close();
        byte[] ret = bos.toByteArray();
        bos.flush();
        bos.close();
        return ret;
    }

    public static String base64DecodeDecompress(String data, String charsetName) throws IOException {
        byte[] base64DecodeData = Base64.getDecoder().decode(data);
        return new String(decompress(base64DecodeData), charsetName);
    }

    public static String base64DecodeDecompress(String data) throws IOException {
        return base64DecodeDecompress(data, "utf-8");
    }

}
