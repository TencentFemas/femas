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

import lombok.Getter;


public enum Scope {
    All(DefaultScopeDefine.ALL),
    Service(DefaultScopeDefine.SERVICE),
    ServiceInstance(DefaultScopeDefine.SERVICE_INSTANCE),
    Endpoint(DefaultScopeDefine.ENDPOINT),
    ServiceRelation(DefaultScopeDefine.SERVICE_RELATION),
    ServiceInstanceRelation(DefaultScopeDefine.SERVICE_INSTANCE_RELATION),
    EndpointRelation(DefaultScopeDefine.ENDPOINT_RELATION);

    @Getter
    private int scopeId;

    Scope(int scopeId) {
        this.scopeId = scopeId;
    }


}
