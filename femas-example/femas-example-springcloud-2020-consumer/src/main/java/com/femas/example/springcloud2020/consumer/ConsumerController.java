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

package com.femas.example.springcloud2020.consumer;

import com.femas.example.springcloud2020.consumer.proxy.service.ProviderService;
import com.tencent.tsf.femas.common.context.FemasContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author leoziltong
 * @Date: 2021/5/11 17:11
 */
@RestController
public class ConsumerController {

    @Autowired
    private LoadBalancerClient loadBalancer;
    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    private ProviderService providerService;
    @Autowired
    private ScheduledExecutorService scheduledExecutorService;
    @Autowired
    private ExecutorService executorService;

    @Autowired
    private RestTemplate restTemplate;

    private Future future;

    private AtomicBoolean isActive = new AtomicBoolean(false);


    @RequestMapping("/services")
    public Object services() {
        return discoveryClient.getInstances("femas-example-springcloud2020-provider");
    }

    @RequestMapping("/discover")
    public Object discover() {
        return loadBalancer.choose("femas-example-springcloud2020-provider").getUri().toString();
    }


    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String feignHelloProvider(@RequestParam(required = false) String tagName,
            @RequestParam(required = false) String tagValue) {
        if (!StringUtils.isEmpty(tagName)) {
            FemasContext.putTag(tagName, tagValue);
        }
        return providerService.hello();
    }

    @GetMapping("/feignSchedule")
    public String feignSchedule() {
        if (this.isActive.compareAndSet(false, true)) {
            this.future = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    System.out.println(providerService.echo("i need polling"));
                }
            }, 1000, 1000, TimeUnit.MILLISECONDS);
        }
        return "ok";
    }

    @GetMapping("/feignScheduleShutDown")
    public String scheduleShutDown() {
        if (this.isActive.compareAndSet(true, false)) {
            future.cancel(true);
        }
        return "ok";
    }

    @RequestMapping(value = "/feign-echo/{str}", method = RequestMethod.GET)
    public String feignEchoProvider(@PathVariable String str,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String tagValue) {
        if (!StringUtils.isEmpty(tagName)) {
            FemasContext.putTag(tagName, tagValue);
        }
        return providerService.echo(str);
    }

    @RequestMapping(value = "/rest-echo/{str}", method = RequestMethod.GET)
    public String restEchoProvider(@PathVariable String str,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String tagValue) {
        if (!StringUtils.isEmpty(tagName)) {
            FemasContext.putTag(tagName, tagValue);
        }

        return restTemplate.getForObject("http://femas-springcloud-provider/echo/" + str, String.class);
    }

    @RequestMapping(value = "/rest-echo-error/{str}", method = RequestMethod.GET)
    public String restEchoErrorProvider(@PathVariable String str,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String tagValue) {
        if (!StringUtils.isEmpty(tagName)) {
            FemasContext.putTag(tagName, tagValue);
        }
        return restTemplate.getForObject("http://provider-demo/echo/error/" + str, String.class);
    }

    @RequestMapping(value = "/feign-echo-error/{str}", method = RequestMethod.GET)
    public String feignEchoErrorProvider(@PathVariable String str,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String tagValue) {
        if (!StringUtils.isEmpty(tagName)) {
            FemasContext.putTag(tagName, tagValue);
        }
        return providerService.echoError(str);
    }

    @RequestMapping(value = "/feign-echo-slow/{str}", method = RequestMethod.GET)
    public String feignEchoSlowProvider(@PathVariable String str,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String tagValue) {
        if (!StringUtils.isEmpty(tagName)) {
            FemasContext.putTag(tagName, tagValue);
        }
        return providerService.echoSlow(str);
    }
}
