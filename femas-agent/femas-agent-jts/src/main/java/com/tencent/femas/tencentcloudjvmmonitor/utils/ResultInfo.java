/**
 * Copyright 2010-2021 the original author or authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.femas.tencentcloudjvmmonitor.utils;

public class ResultInfo {
    String status;
    String statusInfo;
    String data;
    boolean compression;

    public ResultInfo() {
        status = "";
        statusInfo = "";
        data = "";
        compression = false;
    }

    public ResultInfo(String st, String info, String dat) {
        status = st;
        statusInfo = info;
        data = dat;
        compression = false;
    }

    public ResultInfo(String st, String info, String dat, boolean compression) {
        status = st;
        statusInfo = info;
        data = dat;
        this.compression = compression;
    }

    public ResultInfo(String st, String dat) {
        status = st;
        statusInfo = "";
        data = dat;
        compression = false;
    }

    public ResultInfo(String st) {
        status = st;
        statusInfo = "";
        data = "";
        compression = false;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResultInfo{"
                + "status='" + status + '\''
                + ", statusInfo='" + statusInfo + '\''
                + ", data='" + data + '\''
                + ", compression = '" + compression + '\''
                + '}';
    }

    public boolean isCompression() {
        return compression;
    }

    public void setCompression(boolean compression) {
        this.compression = compression;
    }
}
