@echo off

if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! & EXIT /B 1
set "JAVA=%JAVA_HOME%\bin\java.exe"

setlocal enabledelayedexpansion

set BASE_DIR=%~dp0

set BASE_DIR=%BASE_DIR:~0,-5%


set CUSTOM_SEARCH_LOCATIONS=file:%BASE_DIR%\conf


set "FAMES_JVM_OPTS=-server -Xms2g -Xmx2g -Xmn1g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%BASE_DIR%\logs\java_heapdump.hprof -XX:-UseLargePages"


set dbType=%1
rem 判断是否传入
if not defined dbType set dbType=inner
if %dbType%==external set %dbType%=external
if %dbType%==inner (
    set JAVA_OPTS=-Dspring.config.location=%BASE_DIR%\conf\bootstrap.yaml -jar %BASE_DIR%\femas-admin.jar
)
if %dbType%==external (
    set JAVA_OPTS=-DdbType=external -Dspring.config.location=%BASE_DIR%\conf\bootstrap.yaml -jar %BASE_DIR%\femas-admin.jar
)
rem 传入非external
if %dbType% neq external (
    set JAVA_OPTS=-Dspring.config.location=%BASE_DIR%\conf\bootstrap.yaml -jar %BASE_DIR%\femas-admin.jar
)
set "FAMES_CONFIG_OPTS=--spring.config.additional-location=%CUSTOM_SEARCH_LOCATIONS%"

set "FAMES_LOG4J_OPTS=--logging.config=%BASE_DIR%\conf\logback-spring.xml"


set COMMAND="%JAVA%" %FAMES_JVM_OPTS% %JAVA_OPTS% %FAMES_CONFIG_OPTS% %FAMES_LOG4J_OPTS% fames.fames %*
echo %COMMAND%
rem start fames command
%COMMAND%
