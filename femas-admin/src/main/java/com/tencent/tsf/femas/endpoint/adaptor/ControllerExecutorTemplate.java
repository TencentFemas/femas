package com.tencent.tsf.femas.endpoint.adaptor;

import static com.tencent.tsf.femas.constant.AdminConstants.OpenApiEndpoint.END_POINT_SUFFIX;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.constant.ModuleConstants;
import com.tencent.tsf.femas.context.ApplicationContextHelper;
import com.tencent.tsf.femas.endpoint.handler.GlobalExceptionHandler;
import com.tencent.tsf.femas.entity.Record;
import com.tencent.tsf.femas.enums.ServiceInvokeEnum;
import com.tencent.tsf.femas.service.ServiceExecutor;
import com.tencent.tsf.femas.service.ServiceExecutorPool;
import com.tencent.tsf.femas.service.impl.IDGeneratorService;
import com.tencent.tsf.femas.storage.DataOperation;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author leoziltong
 */
@Component
public class ControllerExecutorTemplate {

    private final static Logger log = LoggerFactory.getLogger(ControllerExecutorTemplate.class);

    private final GlobalExceptionHandler exceptionHandler;

    private final Map<String, Method> methodMapCache = new ConcurrentHashMap<>();

    private final Map<String, String> endPointUrlMappingMapCache = new ConcurrentHashMap<>();

    private final DataOperation dataOperation;

    private final IDGeneratorService idGeneratorService;

    public ControllerExecutorTemplate(GlobalExceptionHandler exceptionHandler, DataOperation dataOperation,
            IDGeneratorService idGeneratorService) {
        this.exceptionHandler = exceptionHandler;
        this.dataOperation = dataOperation;
        this.idGeneratorService = idGeneratorService;
    }

    private void operateRecord(Object res, Object... param) {
        if(res instanceof Result){
            Result result = (Result) res;
            String code = result.getCode();
            if (StringUtils.isBlank(code) || !StringUtils.equals(code, Result.SUCCESS)) {
                operateRecord(false, param);
                return;
            }
        }
        operateRecord(true, param);
    }

    private void operateRecord(boolean status, Object... param) {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        for (StackTraceElement element : stack) {
            if (element.getClassName().endsWith(END_POINT_SUFFIX)) {
                try {
                    String trace = "";
                    Class clazz = Class.forName(element.getClassName());
                    String mappingUrl = element.getClassName().concat("#").concat(element.getMethodName());
                    if (endPointUrlMappingMapCache.containsKey(mappingUrl)) {
                        trace = endPointUrlMappingMapCache.get(mappingUrl);
//                        service.store(trace);
                    }
                    Annotation[] parentAnnotations = clazz.getDeclaredAnnotations();
//                    String[] parentMapping = parentRequestMapping.value();
                    Method[] method = clazz.getMethods();
                    for (Method m : method) {
                        if (m.getName().equals(element.getMethodName())) {
                            // 存储操作日志
                            saveRecord(status, element.getClassName(), m.getName(), param);
                            Annotation[] childAnnotations = m.getDeclaredAnnotations();
                        }
                    }
//                    String[] childMapping = childRequestMapping.value();
//                    trace = parentMapping.concat(childMapping);
                    endPointUrlMappingMapCache.putIfAbsent(mappingUrl, trace);
                    //service.store(trace);
                } catch (Exception e) {
                    log.warn("operate record failed,{}", e);
                }
            }
        }
    }

    private void saveRecord(boolean status, String endPointPath, String method, Object... param) {
        String endPointName = endPointPath.substring(endPointPath.lastIndexOf('.') + 1);
        String module = ModuleConstants.map.get(endPointName);
        if (module == null) {
            return;
        }
        Record record = new Record();
        record.setTime(new Date().getTime());
        record.setStatus(status);
        record.setModule(module);
        record.setUser("admin");
        record.setLogId("log-" + idGeneratorService.nextHashId());
        if (method.startsWith("delete")) {
            record.setType("删除" + record.getModule());
        } else if (method.startsWith("configure")) {
            record.setType("编辑" + record.getModule());
        } else {
            return;
        }
        if (param != null) {
            record.setDetail(param[0].toString());
        }
        log.info("option log: {}", record);
        dataOperation.configureRecord(record);
    }

    public <T> T process(Processor<T> process) {
        T result;
        try {
            result = process.execute();
            operateRecord(result, null);
        } catch (Exception e) {
            operateRecord(false, null);
            result = exceptionHandler.handler(e);
        }
        return result;
    }

    public <T> T invoke(ServiceInvokeEnum.ApiInvokeEnum invokeEnum, Object... param) {
        T result;
        try {
            ServiceExecutor executor =
                    duplicate(invokeEnum);
            //方法重载，就不做缓存了
            Method method = executor.getClass().getDeclaredMethod(invokeEnum.getMethod(), invokeEnum.getParamClazz());
            Object res = method.invoke(executor, param);
            operateRecord(res, param);
            return (T) res;
        } catch (Exception e) {
            operateRecord(false, param);
            result = exceptionHandler.handler(e);
        }
        return result;
    }

    public <T> T invoke(ServiceInvokeEnum.ApiInvokeEnum invokeEnum) {
        T result;
        try {
            ServiceExecutor executor =
                    duplicate(invokeEnum);
            Method method = methodMapCache.get(invokeEnum.getMethod());
            if (method == null) {
                method = executor.getClass().getDeclaredMethod(invokeEnum.getMethod());
                methodMapCache.putIfAbsent(invokeEnum.getMethod(), method);
            }
            Object res = method.invoke(executor);
            operateRecord(res, null);
            return (T) res;
        } catch (Exception e) {
            operateRecord(false, null);
            result = exceptionHandler.handler(e);
        }
        return result;
    }

    private ServiceExecutor duplicate(ServiceInvokeEnum.ApiInvokeEnum invokeEnum) {
        ServiceExecutorPool serviceExecutorPool = ApplicationContextHelper.getBean(ServiceExecutorPool.class);
        ServiceExecutor executor = serviceExecutorPool.selectOne(invokeEnum.getServiceInvokeEnum());
        StackTraceElement[] stack = new Throwable().getStackTrace();
        if (StringUtils.isEmpty(invokeEnum.getMethod())) {
            for (StackTraceElement element : stack) {
                if (element.getClassName().endsWith(END_POINT_SUFFIX)) {
                    invokeEnum.setMethod(element.getMethodName());
                }
            }
        }
        return executor;
    }

}
