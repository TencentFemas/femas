package com.tencent.tsf.femas.service.http;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tencent.tsf.femas.event.ConfigDataChangedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author mroccyen
 */
@Service
public class HttpLongPollingDataUpdateService implements ConfigDataChangedListener {

    private static final Logger LOG = LoggerFactory.getLogger(HttpLongPollingDataUpdateService.class);

    private static final long SERVER_MAX_HOLD_TIMEOUT = TimeUnit.SECONDS.toMillis(60);

    private final BlockingQueue<HttpLongPollingClient> clients;

    private final ScheduledExecutorService scheduler;

    public HttpLongPollingDataUpdateService() {
        this.clients = new ArrayBlockingQueue<>(1024);
        this.scheduler = new ScheduledThreadPoolExecutor(1,
                (new ThreadFactoryBuilder()).setNameFormat("httpLongPollingExecutor-%d").setDaemon(true).build());
    }

    @Override
    public void onChanged(String key, String updateData) {
        doSendUpdatedData(key, updateData);
    }

    public void doLongPolling(String key, final HttpServletRequest request) {
        final AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0L);
        scheduler.execute(new HttpLongPollingClient(asyncContext, key, SERVER_MAX_HOLD_TIMEOUT));
    }

    public void doSendUpdatedData(final String key, final String updatedData) {
        scheduler.execute(new DataUpdateNotifier(key, updatedData));
    }

    private void generateResponse(final HttpServletResponse response, final String updatedData) {
        try {
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Cache-Control", "no-cache,no-store");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(updatedData);
        } catch (IOException ex) {
            LOG.error("sending response failed.", ex);
        }
    }

    class HttpLongPollingClient implements Runnable {

        private final Logger log = LoggerFactory.getLogger(HttpLongPollingClient.class);

        private final AsyncContext asyncContext;

        private final String key;

        private final long timeoutTime;

        private Future<?> asyncTimeoutFuture;

        HttpLongPollingClient(final AsyncContext ac, final String key, final long timeoutTime) {
            this.asyncContext = ac;
            this.key = key;
            this.timeoutTime = timeoutTime;
        }

        @Override
        public void run() {
            try {
                this.asyncTimeoutFuture = scheduler.schedule(() -> {
                    clients.remove(HttpLongPollingClient.this);
                    //到超时时间了还是没有数据更新，则返回空，告诉客户端可以进行下一次请求了
                    sendResponse("NONE");
                }, timeoutTime, TimeUnit.MILLISECONDS);
                clients.add(this);
            } catch (Exception ex) {
                log.error("add http long polling client error", ex);
            }
        }

        private void sendResponse(final String updatedData) {
            // cancel scheduler
            if (null != asyncTimeoutFuture) {
                asyncTimeoutFuture.cancel(false);
            }
            generateResponse((HttpServletResponse) asyncContext.getResponse(), updatedData);
            asyncContext.complete();
        }
    }

    class DataUpdateNotifier implements Runnable {

        private final String key;
        private final String updatedData;

        private final long changeTime = System.currentTimeMillis();

        DataUpdateNotifier(final String key, final String updatedData) {
            this.key = key;
            this.updatedData = updatedData;
        }

        @Override
        public void run() {
            doRun(clients);
        }

        private void doRun(final Collection<HttpLongPollingClient> clients) {
            for (Iterator<HttpLongPollingClient> iter = clients.iterator(); iter.hasNext(); ) {
                HttpLongPollingClient client = iter.next();
                if (client.key.equals(key)) {
                    iter.remove();
                    client.sendResponse(updatedData);
                    LOG.info("send response with the updated data,key={}, changeTime={}", key, changeTime);
                }
            }
        }
    }
}
