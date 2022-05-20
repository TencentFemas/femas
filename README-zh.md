<img src="https://user-images.githubusercontent.com/22976760/153148783-0ce5ae2f-cc36-4217-8f80-b29a2930b593.png" width="400px">

# Femas：云原生多运行时微服务框架

## [Femas体验环境](http://106.53.107.83:8080/femas)
> 用户名：admin

> 密码：123456

**觉得不错的话，star fork下，欢迎社区开发者共建腾讯开源**

**[`English`](./README.md) | `简体中文`**


- [简介](#简介)
     - [能力](#能力)
     - [特色](#特色)
- [快速入门](#快速入门)
     - [安装服务端](#安装服务端)
     - [单机部署](#单机部署)
     - [集群部署](#集群部署)
     - [Springcloud接入](#springcloud接入)
         - [样例](#样例)
         - [添加依赖](#添加依赖)
         - [配置文件](#配置文件)
         - [启动服务命令](#启动服务命令)
- [Dubbo接入](#dubbo接入)
     - [样例](#样例)
- [文档](#文档)
     - [官方文档](#官方文档)
     - [常见问题](#faq)
     - [贡献手册](#贡献手册)
     - [行为准则](#行为准则)
- [参与贡献](#参与贡献)
- [联系我们](#联系我们)
- [许可证](#许可证)

## 简介
**Femas 是腾讯云微服务平台[TSF](https://cloud.tencent.com/product/tsf) 的开源产品形态，聚焦微服务运行时，提供给多框架统一服务发现、南北及东西流量治理、服务可观测、配置管理等一站式微服务管控能力，解决企业微服务架构转型中异构框架复用难、 激增流量管控难、排障恢复耗时长等核心问题。**

> - 治理数据面：Femas运用Multi-runtime的架构设计，将微服务底层的核心能力标准化、模块化，将微服务领域割裂的基础组件通过合理的架构组装在一起，来满足多元化的微服务场景，轻量化、可移植、低成本、无云厂商绑定。
> - 微服务控制面：Femas提供统一的控制面标准协议，一套治理协议，多语言、多数据面下发。

### 能力
![image](https://user-images.githubusercontent.com/22976760/153163444-aa444552-8fe9-486d-9e6e-4d1cd2f00836.png)

- 注册中心管理：
Femas实现了对主流开源注册中心(目前支持`Consul、nacos、eureka`)的管理，包括集群管理，服务管理，用户在Paas平台配置注册中心集群，即可查看集群状态和服务列表。
- 服务治理：**`鉴权`、`API管理`、`熔断降级`、`访问限流`、`服务注册发现`、`服务路由`、`服务事件`**。
- 服务配置：应用配置管理、配置热更新，Femas实现了一套标准的配置API接口，配置分为`治理规则`、`应用配置`，**开源侧支持通过Paas平台直接下发`治理规则`，不依赖其他三方组件**。
- 服务注册发现：Femas实现了一套标准的注册发现API接口，用户可以直接使用Femas提供的SDK注册发现到主流的开源注册中心。
- 服务监控：
    - > Metrics:Femas实现了一套标准的业务Metrics指标的API接口，Femas默认使用`micrometer`实现业务Metrics统计；
    - > Tracing:实现了一套标准的tracing API接口，SDK侧负责制定OpenTracing日志规范和链路采集。由于业界可观测的统一标准Opentelemtry在发展阶段，第一阶段默认使用SkyWalking agent采集Tracing信息。

### 特色

- 提供 `Java` 与 `Go` 的 SDK，帮助用户在同一套Paas平台上实现**多语言**统一管理。
- Femas将微服务能力标准化封装，提供无关协议的统一接入`Layer`层，方便将全套能力接入任意协议，实现**多协议的统一管理**。
- Femas将一个微服务应用在运行过程中可能需要用到的能力抽象成了一个个标准`API`组件，方便扩展，兼容其他开源组件生态。
- Femas的底层能力`插件化`，`标准化`，用户可按需要灵活组合搭配微服务能力矩阵。
- Femas不绑定任何特定组件，只要是符合Femas标准化协议的组件都能纳管到Femas平台，方便各个层级的用户学习、使用以及二次开发。
![image](https://user-images.githubusercontent.com/22976760/157235354-27819b3c-69f1-4ad1-95e0-c82d2be99272.png)

- **下沉式无侵入接入，用户改造零成本**。
	> - `Agent`字节码注入（`TODO`）
	> - `ServiceMesh`服务网格
- Femas希望能够将腾讯微服务产品中心对微服务的理解总结成平台，帮助用户快速搭建企业级微服务生态。
- Femas支撑了腾讯内部亿级用户生态。
## 快速入门

### 代码结构

```
.
├── femas-adaptor # paas平台插件化适配层
│   └── femas-adaptor-opensource-admin # paas平台适配层，这里默认是跟开源平台的适配，如果要对接其他控制面，可以插件化实现一个adaptor，其次在这里可以组装平台所需要的能力矩阵
├── femas-admin # 控制台
├── femas-admin-starter # 控制台打包部署文件
├── femas-agent # java agent模块
│   ├── femas-agent-core # javaagent bytebuddy封装模块
│   ├── femas-agent-example
│   ├── femas-agent-plugin #字节码插装插件模块
│   ├── femas-agent-starter #premain入口
│   └── femas-agent-tools
├── femas-api #微服务生命周期抽象层，方便用户对接异构rpc框架
├── femas-benchmark
├── femas-common #工具包
├── femas-config #配置模块插件化的抽象层
├── femas-config-impl #配置模块的实现层
│   ├── femas-config-consul #consul配置实现
│   ├── femas-config-nacos #nacos配置实现层
│   └── femas-config-paas #开源控制台的配置实现层，开源数据面和控制面的治理规则交互
├── femas-dependencies-bom # 统一管理femas依赖版本
├── femas-example #示例
│   ├── feams-example-springcloud-hoxton
│   ├── femas-example-alibaba-dubbo-consumer
│   ├── femas-example-alibaba-dubbo-provider
│   ├── femas-example-springcloud-2020-consumer
│   ├── femas-example-springcloud-2020-provider
│   ├── femas-example-springcloud-greenwich-consumer
│   ├── femas-example-springcloud-greenwich-gateway
│   ├── femas-example-springcloud-greenwich-provider
│   └── femas-example-springcloud-greenwich-zuul
├── femas-extensions #sdk对接RPC框架层
│   ├── femas-extension-dubbo #对接dubbo
│   └── femas-extension-springcloud #对接springcloud
├── femas-governance #治理模块的插件化抽象层
├── femas-governance-impl #治理模块的实现层
├── femas-helm
├── femas-registry #注册中心插件化抽象层
├── femas-registry-impl #注册中心插件化的实现层
│   ├── femas-registry-consul
│   ├── femas-registry-etcd
│   ├── femas-registry-eureka
│   ├── femas-registry-k8s
│   ├── femas-registry-nacos
│   └── femas-registry-polaris
├── femas-starters #用户的sdk的starter依赖
│   ├── femas-dubbo-starters
│   └── femas-springcloud-starters
└── jacoco-aggregate
```

### 安装服务端

运行环境依赖：

> 64 bit OS，支持 Linux/Unix/Mac/Windows，脚本启动支持Linux/Unix/Mac；

> 64 bit JDK 1.8+；

> Maven 3.2.x+；

> 外接数据库Mysql（可选）

### 单机部署

源码编译方式启动：
```
mvn -Dmaven.test.skip=true clean install -U

cd femas-admin-starter/target/femas-admin-starter-$version/femas-admin/bin

sh startup.sh
```

控制台配置：
项目配置文件在`femas-admin/conf`目录下
> cd femas-admin-starter-$version/femas-admin/conf

控制台配置主要包含：
- 服务端口
- 数据库配置(如果使用内嵌数据库则不需要配置)
- nacos地址配置(如果使用配置管理则需要配置)
- skywalking web地址配置(获取链路信息需要配置)
- grafana地址配置(获取metrics信息需要配置)

默认使用内嵌数据库启动，一键开箱即用，无需依赖第三方存储组件:
> 内嵌数据库仅支持单机部署，暂不支持集群部署，内嵌数据库数据存储的磁盘路径为`${user.home}/rocksdb/femas/data/`

> 启动脚本:sh startup.sh

需要用到监控能力则需要以下配置：
```
#配置skywalking后端地址（在此之前你必须有部署好的Skywalking集群）
femas:
  trace:
    backend:
      addr: http://skywalking WEB IP:PORT
#配置Metrics grafana地址 （在此之前你必须有部署好的grafana、promethus）
  metrics:
    grafana:
      addr: http://IP:PORT
```

### 集群部署

集群部署同单机部署，唯一区别是数据源必须是外接数据源，使femas的server端支持无状态水平扩展

配置文件配置数据源
```
spring:
  datasource:
    url: jdbc:mysql://IP:3306/adminDb?useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true&useSSL=false&serverTimezone=Asia/Shanghai
    username: username
    password: password
    hikari:
      driver-class-name: com.mysql.cj.jdbc.Driver
```

启动命令为
> sh startup.sh external 


**访问`http://localhost:8080/index`即可看到控制台页面**
> 登陆用户名:admin，密码:123456，用户名密码写死，开源侧不做任何权限限制。

### 服务接入paas平台前准备

#### step 1.配置注册中心 (在此之前你必须有已经部署好的注册中心集群，Femas不绑定任何注册中心，你可以将任意注册中心托管到Femas平台)
<img width="804" alt="image" src="https://user-images.githubusercontent.com/22976760/156726829-ff8380d1-0a28-426a-8cbb-1398a69f9cb4.png">

> 集群地址支持IP:prot逗号隔开，或者域名方式

#### step 2.创建命名空间
<img width="1643" alt="image" src="https://user-images.githubusercontent.com/22976760/156727253-834f560f-e147-4217-9203-4b0cbd4e5575.png">

> 命名空间绑定配置的注册中心

> femas治理中心的服务列表是以命名空间维度逻辑隔离，服务列表从第三方注册中心拉取，拉取条件是接入FemasSDK且服务标记（下文中通过—D写入的命名空间ID）的命名空间和列表选取的命名空间一致。

完成以上两步之后，接下来就可以通过SDK接入femas将服务纳管到paas平台了。


### Springcloud接入

##### [样例](./femas-example/)

在femas父pom下执行脚本：
> mvn -Dmaven.test.skip=true clean install -U 


##### 添加依赖
```
<!-- 注册中心原生依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
    <version>2.1.2.RELEASE</version>
</dependency>
 
<!-- femas中间件依赖 -->
<dependency>
    <groupId>com.tencent.tsf</groupId>
    <artifactId>femas-springcloud-greenwich-starter</artifactId>
    <version>${femas.latest.version}</version>
</dependency>
```
目前femas支持starter列表目录：cd femas-starters/

默认支持的版本组件有：
- springcloud greenwich
- springcloud 2020
- springcloud gateway
- springcloud zuul

#### 配置文件
##### 业务应用原生配置文件，路径为:resources/bootstrap.yaml
```
server:
  port: 18001
spring:
  application:
    name:
      femas-springcloud-consumer
  cloud:
    consul:
      host: 127.0.0.1
      port: 8500
      discovery:
        serviceName: femas-springcloud-consumer
        instanceId: ${spring.application.name}-${server.port}-${spring.cloud.client.hostname}
        heartbeat:
          enabled: true
#    nacos:
#      discovery:
#        server-addr: 127.0.0.1:8848
 ```
 ##### Femas组件配置文件路径：resources/femas.conf（Ymal格式，用于配置femas相关本地配置，如paas地址、自定义注册中心集群、自定义治理规则等）
 ```
# 配置paas后台地址，如果没有配置，则从本地配置文件获取规则
paas_server_address: http://127.0.0.1:8080
 
# 使用Femas提供的方式接入注册中心，dubbo或者自研协议的使用方式
femas_registry_ip: 127.0.0.1  //注册中心集群地址
femas_registry_port: 8500	//注册中心端口号	
femas_registry_type: consul	//注册中心类型

#以下配置可选，用于配置加载基础组件类型及本地治理规则，不加则加载femas默认配置。
rateLimit:
  type: femasRateLimit
authenticate:
  type: femasAuthenticate
serviceRouter:
  chain:
    - FemasDefaultRoute
loadbalancer:
  type: random
circuitBreaker:
  enable: true
  chain:
    - femasCircuitBreaker
```

##### 启动服务命令
```
# 接入femas必须参数
-Dfemas_namespace_id=命名空间ID  //服务需要指定所属命名空间
-Dfemas_prog_version=服务版本   //指定服务所属的版本（部署组），配合sdk侧的实现服务治理，如流量分拨路由

# 有监控需求的用户按需添加，非必须
-javaagent:"{skywalking agent绝对路径}/agent/skywalking-agent.jar"  // 引用skywalking的agent探针
-Dskywalking.agent.service_name=注册到skywalking上的服务名，需要跟注册中心名称一致,否则tracing链路观测会找不到相应服务。
-Dskywalking.collector.backend_service=skywalking 后端地址，可以覆盖agent的conf配置
```

### Dubbo接入

##### [样例暂时未放开](./)
> 详情参见下文官方文档

## 文档
#### [官方文档](http://femas.io/)
#### [FAQ](http://femas.io/doc/community/FAQ.html)
#### [贡献手册](./CONTRIBUTING.md)
#### [行为准则](./Code-of-Conduct.md) 


## 参与贡献
> - 积极参与 Issue 的讨论，如答疑解惑、提供想法或报告无法解决的错误（Issue）
> - 撰写和改进项目的文档（Wiki）
> - 提交补丁优化代码（Coding）

**你将获得**
> - 加入腾讯开源项目贡献者名单，并展现在腾讯开源官网
> - 写入具体项目的 [CONTRIBUTING.md](./CONTRIBUTING.md)
> - 腾讯开源贡献者证书（[电子版&纸质](https://opensource.tencent.com/img/example.jpg)）
> - 成为线下技术大会/沙龙特邀嘉宾
> - Q币及纪念品

## 联系我们
![image](https://user-images.githubusercontent.com/22976760/153163498-07f62802-18b3-4e74-94ff-32855d542281.png)

添加小Q妹微信，备注【Femas】

![image](https://user-images.githubusercontent.com/22976760/160102336-bffd2c4b-b3c7-4830-8623-92b39f102fb4.png)

## 许可证
[LICENSE.](./LICENSE)
