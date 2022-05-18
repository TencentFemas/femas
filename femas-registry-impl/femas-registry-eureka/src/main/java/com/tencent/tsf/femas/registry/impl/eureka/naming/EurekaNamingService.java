package com.tencent.tsf.femas.registry.impl.eureka.naming;/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.netflix.discovery.shared.transport.EurekaHttpClient;
import com.netflix.discovery.shared.transport.EurekaHttpResponse;
import com.tencent.tsf.femas.common.httpclient.HttpStatus;
import com.tencent.tsf.femas.registry.impl.eureka.serviceregistry.EurekaBeatReactor;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author leoziltong
 */
public class EurekaNamingService {

    private final EurekaHttpClient eurekaHttpClient;

    private final EurekaBeatReactor beatReactor;


    public EurekaNamingService(EurekaHttpClient eurekaHttpClient) {
        this.eurekaHttpClient = eurekaHttpClient;
        beatReactor = new EurekaBeatReactor(eurekaHttpClient);
    }


    public void registerInstance(InstanceInfo instanceInfo) {
        EurekaHttpResponse<Void> response = eurekaHttpClient.register(instanceInfo);
        if (Objects.requireNonNull(HttpStatus.resolve(response.getStatusCode())).is2xxSuccessful()) {
            beatReactor.addInstance(instanceInfo.getId(), instanceInfo);
        }
    }

    public void deregisterInstance(InstanceInfo instanceInfo) {
        EurekaHttpResponse<Void> response = eurekaHttpClient.cancel(instanceInfo.getAppName(), instanceInfo.getId());
        if (Objects.requireNonNull(HttpStatus.resolve(response.getStatusCode())).is2xxSuccessful()) {
            beatReactor.removeInstance(instanceInfo.getId());
        }
    }

    public List<InstanceInfo> getApplications(String serviceName) {
        EurekaHttpResponse<Application> eurekaHttpResponse =
                eurekaHttpClient.getApplication(serviceName);
        if (Objects.requireNonNull(HttpStatus.resolve(eurekaHttpResponse.getStatusCode())).is2xxSuccessful()) {
            return eurekaHttpResponse.getEntity().getInstances();
        }
        return Collections.emptyList();
    }

    public List<Application> getAllApplications() {
        EurekaHttpResponse<Applications> applicationsEurekaHttpResponse = eurekaHttpClient.getApplications();
        if (Objects.requireNonNull(HttpStatus.resolve(applicationsEurekaHttpResponse.getStatusCode()))
                .is2xxSuccessful()) {
            return applicationsEurekaHttpResponse.getEntity().getRegisteredApplications();
        }
        return Collections.emptyList();
    }

    public EurekaBeatReactor getBeatReactor() {
        return beatReactor;
    }
}