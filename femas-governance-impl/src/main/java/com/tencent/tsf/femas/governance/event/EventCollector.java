package com.tencent.tsf.femas.governance.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.util.id.UIdGenerator;
import com.tencent.tsf.femas.config.AbstractConfigHttpClientManager;
import com.tencent.tsf.femas.config.AbstractConfigHttpClientManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Cody
 * @date 2021 2021/7/13 3:05 下午
 */
public class EventCollector {


    protected static final Integer QUEUE_THRESHOLD = 1000;
    protected static final BlockingQueue<FemasEventData> eventQueue = new LinkedBlockingQueue(QUEUE_THRESHOLD);
    protected static final Integer MAX_BATCH_SIZE = 50;
    protected static final String UPSTREAM_NAMESPACE_ID_KEY = "upstream_namespace";
    protected static final String DOWNSTREAM_NAMESPACE_ID_KEY = "downstream_namespace";
    protected static final String DETAIL = "detail";
    private static final Logger LOGGER = LoggerFactory.getLogger(EventCollector.class);
    protected static AbstractConfigHttpClientManager manager = AbstractConfigHttpClientManagerFactory
            .getConfigHttpClientManager();
    protected static  final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    protected static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    protected static final String instanceId = Context.getSystemTag("instance.id");
    protected static final String namespaceId = Context.getSystemTag("namespace.id");

    // 定时发事件
    static {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            while (true) {
                if (eventQueue.isEmpty()) {
                    break;
                }
                List<FemasEventData> eventDataList = new ArrayList<>();
                eventQueue.drainTo(eventDataList, MAX_BATCH_SIZE);
                postEvent(eventDataList);
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private static void postEvent(List<FemasEventData> eventData) {
        try {
            String serviceName = Context.getSystemTag("service.name");
            String namespace = Context.getSystemTag("namespace.id");
            Service service = new Service(namespace, serviceName);
            manager.reportEvent(service, UIdGenerator.generateUid(), gson.toJson(eventData));
        } catch (Exception e) {
            LOGGER.warn("[FEMAS CIRCUIT BREAKER EVENT COLLECTOR] Report to event-master failed.", e.getClass());
        }
    }


}
