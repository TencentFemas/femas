package com.tencent.tsf.femas.config;

import com.google.common.eventbus.EventBus;
import com.tencent.tsf.femas.filter.FemasCoreFilter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * @author leo
 */
@Configuration
@Slf4j
public class GlobalConfig {

    @Bean
    public FemasConfigManager femasConfigManager() {
        return FemasConfigManagerFactory.getConfigManagerInstance();
    }


    @Bean
    @DependsOn("applicationContextHelper")
    public FilterRegistrationBean filterRegistration() {
        FilterRegistrationBean<FemasCoreFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(femasCoreFilter());
        registration.addUrlPatterns("/v1/*");
        registration.setName("femasCoreFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FemasCoreFilter femasCoreFilter() {
        return new FemasCoreFilter();
    }

    @Bean
    public ExecutorService executorService() {
        ExecutorService executorService = new ThreadPoolExecutor(30, 30,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("femas-threadPool");
                        return thread;
                    }
                }, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                if (!executor.isShutdown()) {
                    log.info("femas ThreadPool is busy Executes task r in the caller's thread");
                    r.run();
                }
            }
        });
        return executorService;
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("atom-scheduled-threadPool");
                t.setDaemon(true);
                return t;
            }
        });
    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }
}