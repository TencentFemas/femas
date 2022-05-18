<img src="https://user-images.githubusercontent.com/22976760/153148783-0ce5ae2f-cc36-4217-8f80-b29a2930b593.png" width="400px">

# Femas: Cloud native multi-runtime microservice framework

## [Show me femas](http://106.53.107.83:8080/femas)
> username:admin

> password:123456

**If you like,star fork it and join us**

`English` | **[`简体中文`](./README-zh.md)**

- [Introduction](#introduction)
    - [ability](#ability)
    - [Feature](#feature)
- [Quick start](#quick-start)
    - [Install the server](#install-the-server)
    - [Stand-alone deployment](#stand-alone-deployment)
    - [Cluster deployment](#cluster-deployment)
    - [Springcloud access](#springcloud-access)
        - [Sample](#sample)
        - [Add dependency](#add-dependency)
        - [Configuration file](#configuration-file)
        - [Start service command](#start-service-command)
- [Dubbo access](#dubbo-access)
    - [Sample](#sample)
- [Documentation](#documentation)
    - [Official Document](#official-document)
    - [FAQ](#faq)
    - [Contribution Manual](#contribution-manual)
    - [Code of Conduct](#code-of-conduct)
- [Participate in Contribution](#participate-in-contribution)
- [contact us](#contact-us)
- [License](#license)

## Introduction
**Femas is an open source product form of Tencent Cloud's microservice platform [TSF](https://cloud.tencent.com/product/tsf) . It focuses on the running state of microservices and provides one-stop microservice management and control capabilities such as multi-frame unified service discovery, north-south and east-west traffic management, service observability, and configuration management. In the service architecture transformation, the core issues are difficult to reuse heterogeneous frameworks, difficult to manage and control surge traffic, and time-consuming for troubleshooting and recovery.**
> - data plane: Femas uses the multi-runtime architecture design to standardize and modularize the underlying core capabilities of microservices, and assemble the basic components split in the microservice field through a reasonable architecture to meet diversified microservice scenarios. , lightweight, portable, low-cost, cloud-free vendor binding.
> - control plane: Femas provides a unified control plane standard protocol, a set of governance protocols, and multi-language and multi-data plane distribution.
### ability
![image](https://user-images.githubusercontent.com/22976760/153156369-6fa5626e-f0a5-452b-8519-fe84013b5186.png)

- Registry manage:
Femas implements the management of open source registries (currently supports `Consul, nacos, eureka`), including cluster management and service management. Users can configure the registry cluster on the Paas platform to view the cluster status and service list.
- Service governance: **`Authentication`, `API management`, `Fuse downgrade`, `Access current limit`, `Service registration discovery`, `Service routing`, `Service event`**.
- Service configuration: application configuration management, configuration hot update, Femas implements a set of standard configuration API interface, configuration is divided into `governance rules`,`application configuration`, **open source side supports directly issuing `governance rules through Paas platform `, do not rely on other third-party components**.
- Service registration discovery: Femas implements a set of standard registration discovery API interfaces, and users can directly use the SDK provided by Femas to register and discover to mainstream open source registry centers.
- Service monitoring:
    - > Metrics: Femas implements a set of standard API interfaces for business metrics. Femas uses `micrometer` to implement business metrics statistics by default.
    - > Tracing: Femas implements a set of standard tracing API interfaces. The SDK side is responsible for formulating `OpenTracing` log specifications and link collection. By default, Opentelemtry is used to collect Tracing

### Feature

- Provide SDKs for `Java` and `Go` to help users realize **multi-language** unified management on the same Paas platform.
- Femas standardizes and encapsulates microservice capabilities and provides unified access to the `Layer` layer of irrelevant protocols, which facilitates the integration of a full set of capabilities into any protocol, and realizes the unified management of **multi-protocol**.
- Femas abstracts the capabilities that a microservice application may need to use in the running process into standard `API` components, which are convenient for expansion and compatible with other open source component ecosystems.
- The underlying capabilities of Femas are `plug-in` and `standardization`, and users can flexibly combine and match the microservice capability matrix according to their needs.
- Femas is not bound to any specific components, as long as the components that conform to the Femas standardized protocol can be managed on the Femas platform, it is convenient for users at all levels to learn, use and develop secondary.
![image](https://user-images.githubusercontent.com/22976760/158731778-0005ff57-433b-4001-b135-b6879cd7ede8.png)
- **Sink-type non-intrusive access, zero cost for user transformation**.
> - `Agent` bytecode injection (`TODO`)
> - `ServiceMesh` service mesh
- Femas hopes to summarize Tencent's microservice product center's understanding of microservices into a platform to help users quickly build an enterprise-level microservice ecosystem.
- Femas supports Tencent's internal ecosystem of billions of users.
## Quick start

### Code Structure
```
.
├── femas-adaptor # adaptation layer plugin for paas plateform
│   └── femas-adaptor-opensource-admin #  The PAAS platform adaptation layer is adapted to the open source platform by default. If you want to connect to other control surfaces, you can plug-in an adapter. Secondly, you can assemble the capability matrix required by the platform here
├── femas-admin # admin 
├── femas-admin-starter # starter for admin
├── femas-agent # java agent module
│   ├── femas-agent-core # javaagent bytebuddy core module
│   ├── femas-agent-example 
│   ├── femas-agent-plugin # byte-code plugin module
│   ├── femas-agent-starter # premain entrance
│   └── femas-agent-tools 
├── femas-api # the abstract layer of microservice life cycle, is convenient for users to connect with heterogeneous RPC framework
├── femas-benchmark
├── femas-common # toolkit
├── femas-config #  abstraction layer of configuration module
├── femas-config-impl # implementation layer of configuration module
│   ├── femas-config-consul # consul for femas-config
│   ├── femas-config-nacos # nacos for femas-config
│   └── femas-config-paas # pass for femas-config
├── femas-dependencies-bom # Unified management of femas dependent versions
├── femas-example # demo
│   ├── feams-example-springcloud-hoxton
│   ├── femas-example-alibaba-dubbo-consumer
│   ├── femas-example-alibaba-dubbo-provider
│   ├── femas-example-springcloud-2020-consumer
│   ├── femas-example-springcloud-2020-provider
│   ├── femas-example-springcloud-greenwich-consumer
│   ├── femas-example-springcloud-greenwich-gateway
│   ├── femas-example-springcloud-greenwich-provider
│   └── femas-example-springcloud-greenwich-zuul
├── femas-extensions # rpc layer docking framework SDK
│   ├── femas-c-dubbo # extension for dubbo
│   └── femas-extension-springcloud #  extension for springcloud
├── femas-governance # abstraction layer of governance module
├── femas-governance-impl # implementation layer of governance module
├── femas-helm
├── femas-registry # abstraction layer of registry module
├── femas-registry-impl # implementation layer of registry module
│   ├── femas-registry-consul
│   ├── femas-registry-etcd
│   ├── femas-registry-eureka
│   ├── femas-registry-k8s
│   ├── femas-registry-nacos
│   └── femas-registry-polaris
├── femas-starters # Starter dependency of user's SDK
│   ├── femas-dubbo-starters
│   └── femas-springcloud-starters
└── jacoco-aggregate
```


### Install the server

Runtime environment dependencies:

> 64 bit OS, support Linux/Unix/Mac/Windows, script startup supports Linux/Unix/Mac;

> 64 bit JDK 1.8+;

> Maven 3.2.x+;

> External database Mysql (optional)

### Stand-alone deployment

```
mvn -Dmaven.test.skip=true clean install -U

cd femas-admin-starter/target/femas-admin-starter-$version/femas-admin/bin

sh startup.sh
```

The console configuration mainly includes:
- Service port
- Database configuration (no configuration required if using embedded database)
- nacos address configuration (if you use configuration management, you need to configure)
- skywalking web address configuration (requires configuration to obtain link information)
- grafana address configuration (requires configuration to obtain metrics information)

Femas default Start with the embedded database:
> The embedded database only supports single-machine deployment, and does not support cluster deployment. The embedded database data disk path is `${user.home}/rocksdb/femas/data/`

To use the monitoring capability, the following configuration is required:
````
#Configure skywalking backend address
Femas:
  trace:
    backend:
      addr: http://skywalking WEB IP:PORT
#Configure Metrics grafana address
  metrics:
    grafana:
      addr: http://IP:PORT
````

### Cluster deployment

Cluster deployment is the same as stand-alone deployment. The only difference is that the data source must be an external data source, so that the server side of Femas supports stateless horizontal expansion.

Configuration file configuration data source
````
spring:
  datasource:
    url: jdbc:mysql://IP:3306/adminDb?useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true&useSSL=false&serverTimezone=Asia/Shanghai
    username: username
    password: password
    hikari:
      driver-class-name: com.mysql.cj.jdbc.Driver
````

The start command is
> sh startup.sh external

**Visit `http://localhost:8080/index` to see the console page**
> Login user name: admin, password: 123456, the user name and password are hard-coded, and the open source side does not impose any permission restrictions.

### Preparation before the service is connected to the paas platform

#### step 1. Configure the registry(Before this, you must have a deployed registry cluster, Femas is not bound to any registry, you can host any registry to the Femas platform)
<img width="804" alt="image" src="https://user-images.githubusercontent.com/22976760/156726829-ff8380d1-0a28-426a-8cbb-1398a69f9cb4.png">

> Cluster addresses support IP:prot comma-separated, or domain name mode

#### step 2. Create namespace
<img width="1643" alt="image" src="https://user-images.githubusercontent.com/22976760/156727253-834f560f-e147-4217-9203-4b0cbd4e5575.png">

> Registry for namespace binding configuration

> The service list of the femas governance center is logically isolated in the namespace dimension. The service list is pulled from the third-party registration center. The pull condition is to access FemasSDK and the name of the service tag (namespace ID written by -D below) The space is the same as the namespace selected in the list.

After completing the above two steps, you can then access femas through the SDK to manage the service to the paas platform.


### Springcloud access

##### [Sample](./)

Execute the script under the femas parent pom:
> mvn -Dmaven.test.skip=true clean install -U 

##### Add dependency
```
<!-- Native dependency of registry -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
    <version>2.1.2.RELEASE</version>
</dependency>
 
<!-- femas middleware dependency -->
<dependency>
    <groupId>com.tencent.tsf</groupId>
    <artifactId>femas-extension-springcloud-greenwich-starter</artifactId>
    <version>${femas.latest.version}</version>
</dependency>
```
Currently Femas supports starter list directory: cd femas-starters/

The default supported version components are:
- springcloud greenwich
- springcloud 2020
- springcloud gateway
- springcloud zuul

##### Configuration file
##### Business application native configuration file, the path is: resources/bootstrap.yaml
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
# nacos:
# discovery:
# server-addr: 127.0.0.1:8848
 ```
 ##### Femas component configuration file path: resources/femas.conf (Ymal format, used to configure femas-related local configuration, such as paas address, custom registry cluster, custom governance rules, etc.)
 ```
# Configure the paas background address, if not configured, get the rules from the local configuration file
paas_server_address: http://127.0.0.1:8080
 
# Use the method provided by Femas to access the registration center, how to use dubbo or self-developed protocol
femas_registry_ip: 127.0.0.1 //Registry center cluster address
femas_registry_port: 8500 //Registry center port number
femas_registry_type: consul //registry type

#The following configuration is optional, which is used to configure and load the basic component type and local governance rules. If not, load the default configuration of femas.
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
    -femasCircuitBreaker
```

##### Start service command
```
-javaagent:"{skywalking agent absolute path}/agent/skywalking-agent.jar"
-Dfemas_namespace_id=Namespace ID
-Dfemas_prog_version=Service version
-Dskywalking.agent.service_name=The name of the service registered on skywalking, which needs to be consistent with the name of the registration center
-Dskywalking.collector.backend_service=skywalking backend address, which can override the agent's conf configuration
```
> 1. Reference agent probe of skywalking
> 2. The service needs to specify its own namespace
> 3. Specify the group to which the service belongs, and cooperate with the realization of service governance on the SDK side
> 4. The service name registered to skywalking must be the same as the name registered to the registration center, otherwise the tracing link observation will not find the corresponding service.

### Dubbo access

##### [Sample](./)
> For details, see the official document below

## Documentation
#### [Official Document](http://femas.io/)
#### [FAQ](http://femas.io/doc/community/FAQ.html)
#### [Contribution Manual](./CONTRIBUTING.md)
#### [Code of Conduct](./Code-of-Conduct.md)

## Participate in Contribution
> - Actively participate in the discussion of the Issue, such as answering questions, providing ideas, or reporting unsolvable errors (Issue)
> - Write and improve project documentation (Wiki)
> - Submit patch optimization code (Coding)

**You will get**
> - Join the list of contributors to Tencent open source projects and display them on Tencent open source official website
> - Write [CONTRIBUTING.md](./CONTRIBUTING.md) for specific items
> - Tencent open source contributor certificate ([electronic version & paper](https://opensource.tencent.com/img/example.jpg))
> - Become a special guest of offline technology conference/salon
> - Q coins and souvenirs

## contact us
![image](https://user-images.githubusercontent.com/22976760/153156454-9e1718e8-e676-4f1b-838b-bbe95e9237f7.png)

![image](https://user-images.githubusercontent.com/22976760/160102120-e40069d2-bc28-456c-bfb0-5bed570c5484.png)


## License
[LICENSE.](./LICENSE)

