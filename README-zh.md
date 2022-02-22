<img src="https://user-images.githubusercontent.com/22976760/153148783-0ce5ae2f-cc36-4217-8f80-b29a2930b593.png" width="400px">

# Femas：云原生多运行时微服务框架

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
**Femas 是腾讯云微服务平台[TSF](https://cloud.tencent.com/product/tsf) 的开源产品形态，聚焦微服务运行态，提供给多框架统一服务发现、南北及东西流量治理、服务可观测、配置管理等一站式微服务管控能力，解决企业微服务架构转型中异构框架复用难、 激增流量管控难、排障恢复耗时长等核心问题。**

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
    - > Metrics:Femas实现了一套标准的业务Metrics指标的API接口，Femas默认使用`micrometer`实现业务Metrics统计
    - > Tracing:Femas实现了一套标准的tracing的API接口，SDK侧负责制定`OpenTracing`日志规范和链路采集，默认使用Opentelemtry采集Tracing

### 特色

- 提供 `Java` 与 `Go` 的 SDK，帮助用户在同一套Paas平台上实现**多语言**统一管理。
- Femas将微服务能力标准化封装，提供无关协议的统一接入`Layer`层，方便将全套能力接入任意协议，实现**多协议的统一管理**。
- Femas将一个微服务应用在运行过程中可能需要用到的能力抽象成了一个个标准`API`组件，方便扩展，兼容其他开源组件生态。
- Femas不绑定任何其他组件，方便各个层级的用户学习、使用以及二次开发。
- Femas的底层能力**插件化，方便扩展**，用户可按需要灵活组合搭配微服务能力矩阵。
- **下沉式无侵入接入，用户改造零成本**。
	> - `Agent`字节码注入（`TODO`）
	> - `ServiceMesh`服务网格
- Femas希望能够将腾讯微服务产品中心对微服务的理解总结成平台，帮助用户快速搭建企业级微服务生态。
- Femas支撑了腾讯内部亿级用户生态。
## 快速入门

### 安装服务端

运行环境依赖：

> 64 bit OS，支持 Linux/Unix/Mac/Windows，脚本启动支持Linux/Unix/Mac；

> 64 bit JDK 1.8+；

> Maven 3.2.x+；

> APM监控工具Skywalking

> Metrics监控工具promethus、grafana

> 外接数据库Mysql（可选）

### 单机部署

源码编译方式
> mvn -Dmaven.test.skip=true clean install -U

> cd femas-admin-starter/target/femas-admin-starter-$version/femas-admin/bin

使用内嵌数据库启动:内嵌数据库仅支持单机部署，暂不支持集群部署，内嵌数据库数据磁盘路径为`${user.home}/rocksdb/femas/data/`
> sh startup.sh

使用外接数据库启动:
> sh startup.sh external


下载压缩包加压方式
加压文件
> tar -zxvf femas-admin-starter-$version.tar.gz

> cd femas-admin-starter-$version/femas-admin/bin

启动脚本，内嵌数据库
> sh startup.sh

配置文件：

项目配置文件在`femas-admin/conf`目录下
> cd femas-admin-starter-$version/femas-admin/conf

配置skywalking后端地址
```
femas:
  trace:
    backend:
      addr: http://IP:PORT
#配置Metrics grafana地址
  metrics:
    grafana:
      addr: http://IP:PORT
```

### 集群部署

集群部署同单机部署，唯一区别是数据源必须是外接数据源
启动命令为
> sh startup.sh external 

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

**`访问http://localhost:8080/index`**

### Springcloud接入

##### [样例](./)

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
    <artifactId>femas-extension-springcloud-greenwich-starter</artifactId>
    <version>${femas.latest.version}</version>
</dependency>
```

##### 配置文件
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
 
 
# 是配置paas后台如果没有配置，则从本地配置文件获取规则
paas_server_address: http://127.0.0.1:8080
 
# 使用Femas提供的方式接入注册中心
femas_registry_ip: 127.0.0.1
femas_registry_port: 8500
femas_registry_type: consul
```

##### 启动服务命令
```
-javaagent:"{skywalking agent绝对路径}/agent/skywalking-agent.jar" 
-Dfemas_namespace_id=命名空间ID
-Dfemas_prog_version=服务版本
-Dskywalking.agent.service_name=注册到skywalking上的服务名，需要跟注册中心名称一致
-Dskywalking.collector.backend_service=skywalking后端地址，可以覆盖agent的conf配置
```
> 1.引用skywalking的agent探针

> 2.服务需要指定所属命名空间

> 3.指定服务所属的组别，配合sdk侧的实现服务治理

> 4.注册到skywalking上面的服务名，必须和注册到注册中心的名称一致，否则tracing链路观测会找不到相应服务。

### Dubbo接入

##### [样例](./)
> 详情参见下文官方文档

## 文档
#### [官方文档]()
#### [FAQ]()
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


## 许可证
[BSD v3.](./LICENSE)
