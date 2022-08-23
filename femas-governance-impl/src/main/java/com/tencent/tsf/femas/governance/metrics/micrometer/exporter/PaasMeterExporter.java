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

package com.tencent.tsf.femas.governance.metrics.micrometer.exporter;

import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.connector.server.ServerConnectorManager;
import com.tencent.tsf.femas.governance.metrics.micrometer.CompositeMetricsData;
import com.tencent.tsf.femas.governance.metrics.micrometer.MicroMeterExporter;

import com.tencent.tsf.femas.plugin.config.gov.MetricsExporterConfig;
import com.tencent.tsf.femas.plugin.config.verify.DefaultValues;
import com.tencent.tsf.femas.plugin.context.ConfigContext;
import com.tencent.tsf.femas.plugin.impl.FemasPluginContext;

/**
 * @Author leoziltong
 * @Date: 2021/7/26 16:48
 */
public class PaasMeterExporter extends MicroMeterExporter {

    protected static ServerConnectorManager manager = FemasPluginContext.getServerConnectorManager();
    private String exporterAddr;

    @Override
    public void registerMetricsEvent(CompositeMetricsData data) {
        super.registerMetricsEvent(data);
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {
        MetricsExporterConfig config = conf.getConfig().getMetricsExporter();
        if (config == null) {
            this.exporterAddr = DefaultValues.DEFAULT_METRICS_EXPORTER_ADDR;
        } else {
            this.exporterAddr = config.getExporterAddr();
        }
    }


    @Override
    public void unRegisterMetricsEvent(CompositeMetricsData data) {
        super.unRegisterMetricsEvent(data);
    }

    @Override
    public void run() {
//        Map<Endpoint, EndPointMetricsContext.EndPointMetrics> contextMap = EndPointMetricsContext.getEndPointMetricsContextCache();

    }

    @Override
    public String getName() {
        return "unico";
    }

}
