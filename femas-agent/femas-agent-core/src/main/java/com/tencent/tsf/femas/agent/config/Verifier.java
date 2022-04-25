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

package com.tencent.tsf.femas.agent.config;

/**
 * 检验SDK配置
 *
 * @author andrewshan
 * @author leoziltong
 * @date 2019/8/20
 */
public interface Verifier {

    /**
     * 执行校验操作，参数校验失败会抛出IllegalArgumentException
     */
    void verify() throws IllegalArgumentException;

    /**
     * TODO 设置默认值信息，直接放默认值;还是优先使用用户配置,用户没有配置才使用默认配置
     */
    void setDefault();

}
