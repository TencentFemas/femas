/**
 * Tencent is pleased to support the open source community by making Polaris available.
 * <p>
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 * <p>
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://opensource.org/licenses/BSD-3-Clause
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.tsf.femas.plugin.config.gov;


import com.tencent.tsf.femas.plugin.config.PluginConfig;
import com.tencent.tsf.femas.plugin.config.verify.Verifier;

/**
 * 负载均衡相关配置项
 *
 * @author andrewshan
 * @date 2019/8/20
 */
public interface LoadBalanceConfig extends PluginConfig, Verifier {

    /**
     * 负载均衡类型
     *
     * @return String
     */
    String getType();

}
