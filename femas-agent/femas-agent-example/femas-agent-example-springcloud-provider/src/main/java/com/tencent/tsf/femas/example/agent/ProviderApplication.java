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

package com.tencent.tsf.femas.example.agent;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * @Author leoziltong
 * @Date: 2021/5/11 17:05
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("femas-remote-threadPool");
                t.setDaemon(true);
                return t;
            }
        });
    }

    /**
     * 监听nacos配置 start
     */
    @Component
    class NacosConfigSampleRunner implements ApplicationRunner {

        @Autowired
        private NacosConfigManager nacosConfigManager;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            nacosConfigManager.getConfigService().addListener(
                    "femas-springcloud-provider.yaml", "DEFAULT_GROUP", new Listener() {

                        @Override
                        public void receiveConfigInfo(String configInfo) {
                            Properties properties = new Properties();
                            try {
                                properties.load(new StringReader(configInfo));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println("config changed: " + properties);
                        }

                        @Override
                        public Executor getExecutor() {
                            return null;
                        }
                    });
        }

    }

    //
//    
    // 监听bootstrap.yaml指定的命名空间namespace下的配置，比如：femas-springcloud-provider.properties
    @RestController
    @RefreshScope
    class NacosSampleController {

        @Value("${user.name:yy}")
        String userName;

        @RequestMapping("/user")
        public String simple() {
            return "Hello Nacos Config!" + "Hello " + userName;
        }
    }
    /** 监听nacos配置 end */
}
