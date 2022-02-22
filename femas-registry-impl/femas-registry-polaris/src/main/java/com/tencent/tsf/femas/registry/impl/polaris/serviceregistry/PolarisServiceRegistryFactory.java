/*
 * Tencent is pleased to support the open source community by making Femas available.
 *
 * Copyright (C) 2022, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */   
package com.tencent.tsf.femas.registry.impl.polaris.serviceregistry;

import java.util.Map;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistryFactory;

/**
* <pre>  
* 文件名称：PolarisServiceRegistryFactory.java  
* 创建时间：Jan 2, 2022 4:35:09 PM   
* @author juanyinyang  
* 类说明：  
*/
public class PolarisServiceRegistryFactory implements ServiceRegistryFactory {

    /** 
     * @see com.tencent.tsf.femas.common.spi.SpiExtensionClass#getType()
     */
    @Override
    public String getType() {
        return RegistryEnum.POLARIS.name();
    }

    /** 
     * @see com.tencent.tsf.femas.common.serviceregistry.ServiceRegistryFactory#getServiceRegistry(java.util.Map)
     */
    @Override
    public ServiceRegistry getServiceRegistry(Map<String, String> configMap) {
        return new PolarisServiceRegistry(configMap);
    }

}
  