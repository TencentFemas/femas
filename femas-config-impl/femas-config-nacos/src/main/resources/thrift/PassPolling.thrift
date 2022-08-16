namespace java com.tencent.tsf.femas.config.thrift.paas

service PaasPolling{ 
    PollingResult fetchBreakerRule(1:SimpleParam simpleParam)
    PollingResult reportServiceApi(1:ServiceApiRequest serviceApiRequest)
    PollingResult reportServiceEvent(1:ReportEventRequest reportEventRequest)
    PollingResult initNamespace(1:InitNamespaceRequest initNamespaceRequest)
}

struct SimpleParam{
   1: required string param;
}


struct ServiceApiRequest{
    1: required string namespaceId;
    2: required string serviceName;
    3: required string applicationVersion;
    4: required string data;
}

struct ReportEventRequest{
    1: required string namespaceId;
    2: required string serviceName;
    3: required string eventId;
    4: required string data;
}

struct InitNamespaceRequest{
    1: required string namespaceId;
    2: required string registryAddress;
}

struct PollingResult{
   1: required string code;
   2: required string message;
   3: required string result;
}
