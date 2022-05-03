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

package com.tencent.tsf.femas.service.trace;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/8/19 16:53
 */
public abstract class AbstractOpenTracingServer implements OpentracingServer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractOpenTracingServer.class);


    public Properties propertiesPropertySourceLoader(String propertyFileName) {
        InputStreamReader in = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            in = new InputStreamReader(loader.getResourceAsStream(propertyFileName), "UTF-8");
            if (in != null) {
                Properties prop = new Properties();
                prop.load(in);
                return prop;
            }
        } catch (IOException e) {
            logger.error("load {} error!", propertyFileName);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("close {} error!", propertyFileName);
                }
            }
        }
        return null;
    }

}
