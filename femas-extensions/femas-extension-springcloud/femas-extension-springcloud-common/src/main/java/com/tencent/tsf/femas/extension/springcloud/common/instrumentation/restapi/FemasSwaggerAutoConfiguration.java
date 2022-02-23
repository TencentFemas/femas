/*
 * Tencent is pleased to support the open source community by making Femas available.
 *
 * Copyright (C) 2021, a Tencent company. All rights reserved.
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

package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.restapi;

import static com.google.common.base.Optional.fromNullable;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

@EnableSwagger2
@EnableWebMvc
@Configuration
@ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.EnableWebMvc")
@ConditionalOnProperty(value = "femas.swagger.enabled", havingValue = "true", matchIfMissing = true)
public class FemasSwaggerAutoConfiguration {

    private static final String splitter = ",";
    private static final String VERSION = "1.0.0";
    private static Logger logger = LoggerFactory.getLogger(FemasSwaggerAutoConfiguration.class);
    @Value("${femas.swagger.basePackage:}")
    private String basePackage;
    @Value("${femas.swagger.excludePath:}")
    private String excludePath;
    @Value("${femas.swagger.enabled:true}")
    private boolean enabled;
    @Value("${femas.swagger.group:default}")
    private String groupName;
    @Value("${femas.swagger.basePath:/**}")
    private String basePath;

    private static Predicate<RequestHandler> basePackage(final String basePackage) {
        return input -> declaringClass(input).transform(handlerPackage(basePackage)).or(false);
    }

    private static Function<Class<?>, Boolean> handlerPackage(final String basePackage) {
        return input -> {
            if (StringUtils.isEmpty(basePackage)) {
                return false;
            }
            String[] packages = basePackage.trim().split(splitter);
            // 循环判断匹配
            for (String strPackage : packages) {
                if (input == null) {
                    continue;
                }
                boolean isMatch = input.getPackage().getName().startsWith(strPackage);
                if (isMatch) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Optional<Class<?>> declaringClass(RequestHandler input) {
        if (input == null) {
            return Optional.absent();
        }
        return fromNullable(input.declaringClass());
    }

    @Bean
    public Docket femasDocket() {
        // exclude-path处理
        List<Predicate<String>> excludePath = getExcludePaths();
        List<Predicate<String>> basePathList = new ArrayList<Predicate<String>>();
        if (StringUtils.isEmpty(basePath)) {
            basePathList.add(PathSelectors.ant("/**"));
        } else {
            String[] basePaths = getBasePath().split(splitter);
            for (String basePath : basePaths) {
                if (!StringUtils.isEmpty(basePath)) {
                    basePathList.add(PathSelectors.ant(basePath));
                }
            }
        }
        String basePackage = PackageScanFilter.scanPackage(this.basePackage, splitter);
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(basePackage(basePackage))
                .paths(Predicates.and(
                        Predicates.not(Predicates.or(excludePath)),
                        Predicates.or(basePathList)
                ))
                .build()
                .groupName(groupName)
                .enable(enabled)
                .directModelSubstitute(LocalDate.class, Date.class)
                .apiInfo(apiInfo());
    }

    @Bean
    @ConditionalOnBean(value = {Docket.class})
    public FemasApiMetadataGrapher femasApiMetadataGrapher(DocumentationCache documentationCache,
            ServiceModelToSwagger2Mapper swagger2Mapper, JsonSerializer jsonSerializer, ApplicationContext context) {
        return new FemasApiMetadataGrapher(documentationCache, swagger2Mapper, jsonSerializer, groupName, context);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Swagger API")
                .description("This is to femas show api description")
                .license("Apache 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .termsOfServiceUrl("")
                .version(VERSION)
                .contact(new Contact("", "", ""))
                .build();
    }

    private List<Predicate<String>> getExcludePaths() {
        List<Predicate<String>> excludes = new ArrayList<>();
        if (excludePath == null) {
            return excludes;
        }
        String[] exs = excludePath.split(",");
        Arrays.stream(exs).filter(ex -> !StringUtils.isEmpty(ex)).forEach(ex -> excludes.add(PathSelectors.ant(ex)));
        return excludes;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public String getExcludePath() {
        return excludePath;
    }

    public void setExcludePath(String excludePath) {
        this.excludePath = excludePath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
