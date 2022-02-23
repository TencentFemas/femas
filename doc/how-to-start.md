# How to Start 如何开始

## 依赖环境

OS Linux
docker 版本>18.09

## 通过docker编译代码

```
$ make build-image
```

output:

```
docker build -f Dockerfile -t hub.grepcode.cn/femas/all:2ab9fcb .
Sending build context to Docker daemon  414.5MB
Step 1/10 : FROM ubuntu:20.04
 ---> ba6acccedd29
Step 2/10 : LABEL maintainer "minghhou <minghhou>"
 ---> Using cache
 ---> 2108b4c8b195
Step 3/10 : ENV BUILD_MODE="docker"
 ---> Using cache
 ---> 9081076086b3
Step 4/10 : RUN sed -i 's/archive.ubuntu.com/mirrors.tencent.com/g' /etc/apt/sources.list &&     sed -i 's/security.ubuntu.com/mirrors.tencent.com/g' /etc/apt/sources.list
 ---> Using cache
 ---> d0f24d9b58d7
Step 5/10 : RUN apt update && apt install openjdk-8-jdk maven iproute2 -y
 ---> Using cache
 ---> 9a861a740a0c
Step 6/10 : COPY ./ /usr/local/src/femas
......
......
......
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  12:26 min
[INFO] Finished at: 2021-11-10T08:44:36Z
[INFO] ------------------------------------------------------------------------
Removing intermediate container cb7040afc28e
 ---> 5db511e02bbe
Step 8/10 : COPY ./entrypoint.sh /entrypoint.sh
 ---> bcf224bafd0d
Step 9/10 : RUN chmod +x /entrypoint.sh
 ---> Running in cfe50e129ea1
Removing intermediate container cfe50e129ea1
 ---> 154e082ef1d4
Step 10/10 : ENTRYPOINT /entrypoint.sh
 ---> Running in 01911c9edd0d
Removing intermediate container 01911c9edd0d
 ---> fd8a205585c9
Successfully built fd8a205585c9
Successfully tagged hub.grepcode.cn/femas/all:2ab9fcb
```

## 运行项目

```
$ make run-image
```

output

```
docker run -d --name femas-runner -w /usr/local/src/femas \
-p 8708:8080 \
-e VERSION=2ab9fcb \
-v /home/minghhou/workspace/femas/logs:/usr/local/src/femas/femas-admin-starter/target/femas-admin-starter-0.3.0-2ab9fcb/femas-admin/logs hub.grepcode.cn/femas/all:2ab9fcb
1ed73deba3ebb05669d97b58f28e2cd6ff77402aa4976dbdfa10f920d432cd40
```

## helm安装各种依赖服务

### 安装nacos

参考链接 [官方文档安装nacos](https://github.com/nacos-group/nacos-k8s/blob/master/README-CN.md)
