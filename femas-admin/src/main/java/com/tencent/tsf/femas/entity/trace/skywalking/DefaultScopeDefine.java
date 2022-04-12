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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Setter;

public class DefaultScopeDefine {

    /**
     * All metrics IDs in [0, 10,000) are reserved in Apache SkyWalking.
     * <p>
     * If you want to extend the scope, recommend to start with 10,000.
     */
    public static final int ALL = 0;
    public static final int SERVICE = 1;
    public static final int SERVICE_INSTANCE = 2;
    public static final int ENDPOINT = 3;
    public static final int SERVICE_RELATION = 4;
    public static final int SERVICE_INSTANCE_RELATION = 5;
    public static final int ENDPOINT_RELATION = 6;
    public static final int SERVICE_INSTANCE_JVM_CPU = 8;
    public static final int SERVICE_INSTANCE_JVM_MEMORY = 9;
    public static final int SERVICE_INSTANCE_JVM_MEMORY_POOL = 10;
    public static final int SERVICE_INSTANCE_JVM_GC = 11;
    public static final int SEGMENT = 12;
    public static final int ALARM = 13;
    public static final int DATABASE_ACCESS = 17;
    public static final int DATABASE_SLOW_STATEMENT = 18;
    public static final int SERVICE_INSTANCE_CLR_CPU = 19;
    public static final int SERVICE_INSTANCE_CLR_GC = 20;
    public static final int SERVICE_INSTANCE_CLR_THREAD = 21;
    public static final int ENVOY_INSTANCE_METRIC = 22;
    public static final int ZIPKIN_SPAN = 23;
    public static final int JAEGER_SPAN = 24;
    public static final int HTTP_ACCESS_LOG = 25;
    public static final int PROFILE_TASK = 26;
    public static final int PROFILE_TASK_LOG = 27;
    public static final int PROFILE_TASK_SEGMENT_SNAPSHOT = 28;
    public static final int SERVICE_META = 29;
    public static final int SERVICE_INSTANCE_UPDATE = 30;
    public static final int NETWORK_ADDRESS_ALIAS = 31;
    public static final int UI_TEMPLATE = 32;
    public static final int SERVICE_INSTANCE_JVM_THREAD = 33;
    // browser
    public static final int BROWSER_ERROR_LOG = 34;
    public static final int BROWSER_APP_PERF = 35;
    public static final int BROWSER_APP_PAGE_PERF = 36;
    public static final int BROWSER_APP_SINGLE_VERSION_PERF = 37;
    public static final int BROWSER_APP_TRAFFIC = 38;
    public static final int BROWSER_APP_SINGLE_VERSION_TRAFFIC = 39;
    public static final int BROWSER_APP_PAGE_TRAFFIC = 40;
    /**
     * Catalog of scope, the metrics processor could use this to group all generated metrics by oal rt.
     */
    public static final String SERVICE_CATALOG_NAME = "SERVICE";
    public static final String SERVICE_INSTANCE_CATALOG_NAME = "SERVICE_INSTANCE";
    public static final String ENDPOINT_CATALOG_NAME = "ENDPOINT";
    public static final String SERVICE_RELATION_CATALOG_NAME = "SERVICE_RELATION";
    public static final String SERVICE_INSTANCE_RELATION_CATALOG_NAME = "SERVICE_INSTANCE_RELATION";
    public static final String ENDPOINT_RELATION_CATALOG_NAME = "ENDPOINT_RELATION";
    private static final Map<String, Integer> NAME_2_ID = new HashMap<>();
    private static final Map<Integer, String> ID_2_NAME = new HashMap<>();
    private static final Map<String, List<ScopeDefaultColumn>> SCOPE_COLUMNS = new HashMap<>();
    private static final Map<Integer, Boolean> SERVICE_CATALOG = new HashMap<>();
    private static final Map<Integer, Boolean> SERVICE_INSTANCE_CATALOG = new HashMap<>();
    private static final Map<Integer, Boolean> ENDPOINT_CATALOG = new HashMap<>();
    private static final Map<Integer, Boolean> SERVICE_RELATION_CATALOG = new HashMap<>();
    private static final Map<Integer, Boolean> SERVICE_INSTANCE_RELATION_CATALOG = new HashMap<>();
    private static final Map<Integer, Boolean> ENDPOINT_RELATION_CATALOG = new HashMap<>();

    @Setter
    private static boolean ACTIVE_EXTRA_MODEL_COLUMNS = false;

    public static void activeExtraModelColumns() {
        ACTIVE_EXTRA_MODEL_COLUMNS = true;
    }

}