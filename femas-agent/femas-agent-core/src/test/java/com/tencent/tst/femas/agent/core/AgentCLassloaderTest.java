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
package com.tencent.tst.femas.agent.core;

import com.tencent.tsf.femas.agent.classloader.AgentClassLoader;
import com.tencent.tsf.femas.agent.config.InterceptPlugin;
import org.junit.Test;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/5/19 17:20
 */
public class AgentCLassloaderTest {

    @Test
    public void testAgentClassLoader() throws ClassNotFoundException {
        try {
            AgentClassLoader cl = AgentClassLoader.getDefault();
            Class clazz = cl.loadClass("com.tencent.tsf.femas.agent.config.InterceptPlugin");
            InterceptPlugin plugin = (InterceptPlugin) clazz.newInstance();
            plugin.getOriginAround();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
    }
}
