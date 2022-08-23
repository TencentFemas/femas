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

import com.tencent.tsf.femas.plugin.Plugin;
import java.time.Duration;

/**
 * @Author leoziltong
 * @Date: 2021/7/13 20:52
 */
public interface MetricsExporter extends Plugin {

    /**
     * metrics插件上报
     */
    void report();

    /**
     * 上报间隔
     *
     * @return
     */
    Duration step();

}
