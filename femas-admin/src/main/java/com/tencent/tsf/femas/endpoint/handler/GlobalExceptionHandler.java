package com.tencent.tsf.femas.endpoint.handler;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.exception.FemasException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;


/**
 * 此处为全局异常配置发现类，在此处配置需要全局捕获某一特定异常
 * 不能使用ControllerAdvice注解
 *
 * @author leo
 */
//@ControllerAdvice
@Component
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 缓存handler异常和方法隐射，以提高反射性能
     */
    private static Map<String, Method> handlerMapCache = new ConcurrentHashMap<>();

    static {
        Method[] methods = GlobalExceptionHandler.class.getDeclaredMethods();
        for (Method m : methods) {
            ExceptionHandler handler = m.getAnnotation(ExceptionHandler.class);
            if (handler == null) {
                continue;
            }
            Class<Exception>[] classes = (Class<Exception>[]) handler.value();
            handlerMapCache.putIfAbsent(classes[0].getName(), m);
        }
    }

    /**
     * 全局异常两种实现方式
     * 1.spring原生aop方式
     * 2.策略模式自实现方式,如果走策略模式，则自动添加附加功能，如全局异常，操作日志等
     *
     * @param e
     * @return
     */
    @SneakyThrows
    public <T> T handler(Exception e) {
        if (handlerMapCache.containsKey(e.getClass().getName())) {
            try {
                return (T) handlerMapCache.get(e.getClass().getName()).invoke(this, e);
            } catch (IllegalAccessException illegalAccessException) {
                logger.error("global exception handler illegalAccessException ,{}", illegalAccessException);
            } catch (InvocationTargetException invocationTargetException) {
                logger.error("global exception handler invocationTargetException ,{}",
                        invocationTargetException.getTargetException().getCause());
                return (T) Result
                        .errorData(invocationTargetException.getTargetException().getMessage(), e.getMessage());
            }
        }
        logger.error("[femas-admin] got {} error message: {}", e.getClass().getName(), e.getMessage(), e);
        return handleException(e);
    }

    /**
     * @param e
     * @return Result
     */
    @ExceptionHandler(FemasException.class)
    public <T> T handleFemasException(FemasException e) {
        logger.error("[femas-admin] got FemasException. error code {},error message {}", e.getErrorCode(),
                e.getErrorMessage(), e.getMessage());
        return (T) Result.errorMessage(e.getMessage());
    }

    /**
     * @param e
     * @return Result
     */
    @ExceptionHandler(Exception.class)
    public <T> T handleException(Exception e) {
        logger.error("[femas-admin] got exception: {}", e.getMessage(), e);
        return (T) Result.errorMessage(e.getMessage());
    }

}
