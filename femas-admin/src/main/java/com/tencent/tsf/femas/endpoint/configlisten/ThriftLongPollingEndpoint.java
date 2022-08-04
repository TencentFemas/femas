package com.tencent.tsf.femas.endpoint.configlisten;

import com.tencent.tsf.femas.config.thrift.paas.*;
import com.tencent.tsf.femas.context.ApplicationContextHelper;
import com.tencent.tsf.femas.service.namespace.NamespaceMangerService;
import com.tencent.tsf.femas.service.registry.ServiceManagerService;
import com.tencent.tsf.femas.service.rule.ConvertService;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;

/**
 * @author huyuanxin
 */
@Slf4j
public class ThriftLongPollingEndpoint implements PaasPolling.Iface {

    ConvertService convertService;

    ServiceManagerService serviceManagerService;

    NamespaceMangerService namespaceMangerService;

    public ThriftLongPollingEndpoint() {
        convertService = ApplicationContextHelper.getBean(ConvertService.class);
        serviceManagerService = ApplicationContextHelper.getBean(ServiceManagerService.class);
        namespaceMangerService = ApplicationContextHelper.getBean(NamespaceMangerService.class);
    }


    @Override
    public PollingResult fetchBreakerRule(SimpleParam simpleParam) throws TException {
        log.info("调用fetchBreakerRule接口");
        String fetchBreakerRule = convertService.convert(simpleParam.getParam());
        PollingResult result;
        if (fetchBreakerRule != null) {
            result = ThriftHelper.ok(fetchBreakerRule);
        } else {
            result = ThriftHelper.ok();
        }
        return result;
    }

    @Override
    public PollingResult reportServiceApi(ServiceApiRequest serviceApiRequest) throws TException {
        log.info("调用reportServiceApi接口");
        try {
            serviceManagerService.reportServiceApi(serviceApiRequest.getNamespaceId(),
                    serviceApiRequest.getServiceName(), serviceApiRequest.getApplicationVersion(),
                    serviceApiRequest.getData());
            return ThriftHelper.ok();
        } catch (Exception e) {
            return ThriftHelper.fail(e.getMessage());
        }
    }

    @Override
    public PollingResult reportServiceEvent(ReportEventRequest reportEventRequest) throws TException {
        log.info("调用reportServiceEvent接口");
        try {
            serviceManagerService.reportServiceEvent(reportEventRequest.getNamespaceId(), reportEventRequest.getServiceName(), reportEventRequest.getEventId(), reportEventRequest.getData());
            return ThriftHelper.ok();
        } catch (Exception e) {
            return ThriftHelper.fail(e.getMessage());
        }
    }

    @Override
    public PollingResult initNamespace(InitNamespaceRequest initNamespaceRequest) throws TException {
        log.info("调用initNamespace接口");
        try {
            namespaceMangerService.initNamespace(initNamespaceRequest.getRegistryAddress(), initNamespaceRequest.getNamespaceId());
            return ThriftHelper.ok();
        } catch (Exception e) {
            return ThriftHelper.fail(e.getMessage());
        }
    }

}
