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

package com.femas.example.springcloud2020.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author leoziltong
 * @Date: 2021/5/11 17:10
 */
@RestController
public class ProviderController {

    private static final Logger LOG = LoggerFactory.getLogger(ProviderController.class);

    @Value("${spring.application.name:}")
    private String applicationName;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello() {
        String echoHello = "say hello";
        LOG.info(echoHello);
        return echoHello;
    }

    @RequestMapping(value = "/echo/{param}", method = RequestMethod.GET)
    public String echo(@PathVariable String param) {
        LOG.info("request param: [" + param + "]");
        String result = "request param: " + param + ", response from " + applicationName;
        LOG.info("provider config name: [" + applicationName + ']');
        LOG.info("response info: [" + result + "]");
        return result;
    }

    @RequestMapping(value = "/echo/error/{param}", method = RequestMethod.GET)
    public String echoError(@PathVariable String param) {
        LOG.info("Error request param: [" + param + "], throw exception");

        throw new RuntimeException("mock-ex");
    }

    /**
     * 延迟返回
     *
     * @param param 参数
     * @param delay 延时时间，单位毫秒
     * @throws InterruptedException
     */
    @RequestMapping(value = "/echo/slow/{param}", method = RequestMethod.GET)
    public String echoSlow(@PathVariable String param, @RequestParam(required = false) Integer delay)
            throws InterruptedException {
        int sleepTime = delay == null ? 1000 : delay;
        LOG.info("slow request param: [" + param + "], Start sleep: [" + sleepTime + "]ms");
        Thread.sleep(sleepTime);
        LOG.info("slow request param: [" + param + "], End sleep: [" + sleepTime + "]ms");

        String result = "request param: " + param
                + ", slow response from " + applicationName
                + ", sleep: [" + sleepTime + "]ms";
        return result;
    }

}
