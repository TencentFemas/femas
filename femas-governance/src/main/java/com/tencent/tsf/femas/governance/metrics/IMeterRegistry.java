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

package com.tencent.tsf.femas.governance.metrics;


import com.tencent.tsf.femas.common.context.RpcContext;
import com.tencent.tsf.femas.common.entity.ErrorStatus;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Response;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.governance.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author leoziltong
 * @Date: 2021/7/13 17:20
 * @Version 1.0
 */
public interface IMeterRegistry<T extends Meter, N extends Number> extends Plugin {


    /**
     * 分布摘要
     *
     * @param tags
     * @param name
     * @return
     */
    T summary(String name, List<TagPair> tags);

    /**
     * 事件执行消耗的时间
     *
     * @param tags
     * @param name
     * @return
     */
    T timer(String name, List<TagPair> tags);

    /**
     * 长时间执行的任务的持续时间
     *
     * @param tags
     * @param name
     * @return
     */
    T longTaskTimer(String name, List<TagPair> tags);

    /**
     * 计数器
     *
     * @param tags
     * @param name
     * @return
     */
    T counter(String name, List<TagPair> tags);

    /**
     * 仪表
     *
     * @param name
     * @param tags
     * @return
     */
    N gauge(String name, List<TagPair> tags);

    default List<TagPair> buildTags(Response response, RpcContext rpcContext, ErrorStatus statusCode) {
        List<TagPair> list = new ArrayList<>();
        TagPair tagPair;

        if (null == rpcContext.getTracingContext() || StringUtils
                .isEmpty(rpcContext.getTracingContext().getResultStatus())) {
            if (null != response && null != response.getErrorStatus() && StringUtils
                    .isEmpty(response.getErrorStatus().StatusCode())) {
                tagPair = new TagPair(MetricsTag.getHttpStatus(), response.getErrorStatus().StatusCode());
            } else {
                tagPair = new TagPair(MetricsTag.getHttpStatus(), statusCode.StatusCode());
            }
        } else {
            tagPair = new TagPair(MetricsTag.getHttpStatus(), rpcContext.getTracingContext().getResultStatus());
        }

        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || null == rpcContext.getTracingContext().getLocalHttpMethod()) {
            tagPair = new TagPair(MetricsTag.getLocalHttpMethod(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getLocalHttpMethod(), rpcContext.getTracingContext().getLocalHttpMethod());
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || StringUtils
                .isEmpty(rpcContext.getTracingContext().getLocalApplicationVersion())) {
            tagPair = new TagPair(MetricsTag.getLocalVersion(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getLocalVersion(),
                    rpcContext.getTracingContext().getLocalApplicationVersion());
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || StringUtils
                .isEmpty(rpcContext.getTracingContext().getLocalInstanceId())) {
            tagPair = new TagPair(MetricsTag.getInstanceId(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getInstanceId(), rpcContext.getTracingContext().getLocalInstanceId());
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || StringUtils
                .isEmpty(rpcContext.getTracingContext().getLocalNamespaceId())) {
            tagPair = new TagPair(MetricsTag.getLocalNamespace(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getLocalNamespace(), rpcContext.getTracingContext().getLocalNamespaceId());
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || StringUtils
                .isEmpty(rpcContext.getTracingContext().getLocalIpv4())) {
            tagPair = new TagPair(MetricsTag.getLocalHost(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getLocalHost(), rpcContext.getTracingContext().getLocalIpv4());
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || null == rpcContext.getTracingContext().getLocalPort()) {
            tagPair = new TagPair(MetricsTag.getLocalPort(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getLocalPort(),
                    String.valueOf(rpcContext.getTracingContext().getLocalPort()));
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || null == rpcContext.getTracingContext().getLocalInterface()) {
            tagPair = new TagPair(MetricsTag.getLocalInterface(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getLocalInterface(), rpcContext.getTracingContext().getLocalInterface());
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || null == rpcContext.getTracingContext().getLocalServiceName()) {
            tagPair = new TagPair(MetricsTag.getLocalService(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getLocalService(), rpcContext.getTracingContext().getLocalServiceName());
        }
        list.add(tagPair);

        return list;
    }


    default List<TagPair> buildTags(Request request, Response response, RpcContext rpcContext, ErrorStatus statusCode) {

        List<TagPair> list = new ArrayList<>();
        TagPair tagPair;

        if (null == rpcContext.getTracingContext() || null == rpcContext.getTracingContext().getRemoteServiceName()) {
            tagPair = new TagPair(MetricsTag.getRemoteService(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getRemoteService(), rpcContext.getTracingContext().getRemoteServiceName());
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || null == rpcContext.getTracingContext().getRemoteInterface()) {
            tagPair = new TagPair(MetricsTag.getRemoteInterface(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getRemoteInterface(), rpcContext.getTracingContext().getRemoteInterface());
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || null == rpcContext.getTracingContext().getRemoteIpv4()) {
            tagPair = new TagPair(MetricsTag.getRemoteHost(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getRemoteHost(), rpcContext.getTracingContext().getRemoteIpv4());
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || null == rpcContext.getTracingContext().getRemotePort()) {
            tagPair = new TagPair(MetricsTag.getRemotePort(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getRemotePort(),
                    String.valueOf(rpcContext.getTracingContext().getRemotePort()));
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || null == rpcContext.getTracingContext()
                .getRemoteApplicationVersion()) {
            tagPair = new TagPair(MetricsTag.getRemoteVersion(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getRemoteVersion(),
                    rpcContext.getTracingContext().getRemoteApplicationVersion());
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || null == rpcContext.getTracingContext().getRemoteNamespaceId()) {
            tagPair = new TagPair(MetricsTag.getRemoteNamespace(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getRemoteNamespace(),
                    rpcContext.getTracingContext().getRemoteNamespaceId());
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || null == rpcContext.getTracingContext().getRemoteInstanceId()) {
            tagPair = new TagPair(MetricsTag.getRemoteInstanceId(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getRemoteInstanceId(),
                    rpcContext.getTracingContext().getRemoteInstanceId());
        }
        list.add(tagPair);

        if (null == rpcContext.getTracingContext() || null == rpcContext.getTracingContext().getRemoteHttpMethod()) {
            tagPair = new TagPair(MetricsTag.getRemoteHttpMethod(), "");
        } else {
            tagPair = new TagPair(MetricsTag.getRemoteHttpMethod(),
                    rpcContext.getTracingContext().getRemoteHttpMethod());
        }
        list.add(tagPair);

        list.addAll(this.buildTags(response, rpcContext, statusCode));
        return list;
    }

}
