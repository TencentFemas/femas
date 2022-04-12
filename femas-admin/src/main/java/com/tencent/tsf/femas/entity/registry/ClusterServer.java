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

package com.tencent.tsf.femas.entity.registry;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/4/30 10:49
 */
public class ClusterServer {

    private String serverAddr;

    @ApiModelProperty("最后更新时间 consul没有 需要先判断在展示")
    private Long lastRefreshTime;

    @ApiModelProperty("状态")
    private String state;

    @ApiModelProperty("角色  eureka没有角色的概念 需要先判断有值在展示")
    private String clusterRole;

    public ClusterServer() {
    }

    public ClusterServer(String serverAddr, Long lastRefreshTime, String state) {
        this.serverAddr = serverAddr;
        this.lastRefreshTime = lastRefreshTime;
        this.state = state;
    }

    public ClusterServer(String serverAddr, Long lastRefreshTime, String state, String clusterRole) {
        this.serverAddr = serverAddr;
        this.lastRefreshTime = lastRefreshTime;
        this.state = state;
        this.clusterRole = clusterRole;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public Long getLastRefreshTime() {
        return lastRefreshTime;
    }

    public void setLastRefreshTime(Long lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public String getClusterRole() {
        return clusterRole;
    }

    public void setClusterRole(String clusterRole) {
        this.clusterRole = clusterRole;
    }
}
