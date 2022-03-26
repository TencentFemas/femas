package com.tencent.tsf.femas.governance.metrics.micrometer;

import com.tencent.tsf.femas.common.monitor.Endpoint;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author willJo
 * @since 2022/3/25
 */
public class EndPointMetricsContextTest extends TestCase {

    @Test
    public void testRegister1() {

        Endpoint endpoint = getEndpoint("order", "save");
        Endpoint endpoint1 = getEndpoint("order", "save");

        MeterRegistry registry = new SimpleMeterRegistry();
        Counter counter = registry.counter("orderNum");
        counter.increment();


        EndPointMetricsContext.EndPointMetrics metrics = getEndPointMetrics(endpoint, counter);
        EndPointMetricsContext.EndPointMetrics metrics1 = getEndPointMetrics(endpoint1, counter);

        EndPointMetricsContext.register(metrics);
        EndPointMetricsContext.register(metrics1);

        Assert.assertEquals(1, EndPointMetricsContext.getEndPointMetricsContextCache().size());
    }

    @Test
    public void testRegister2() {

        Endpoint endpoint = getEndpoint("order", "save");
        Endpoint endpoint1 = getEndpoint("product", "update");


        MeterRegistry registry = new SimpleMeterRegistry();
        Counter counter = registry.counter("orderNum");
        counter.increment();


        EndPointMetricsContext.EndPointMetrics metrics = getEndPointMetrics(endpoint, counter);
        EndPointMetricsContext.EndPointMetrics metrics1 = getEndPointMetrics(endpoint1, counter);

        EndPointMetricsContext.register(metrics);
        EndPointMetricsContext.register(metrics1);

        Assert.assertEquals(2, EndPointMetricsContext.getEndPointMetricsContextCache().size());
    }

    private EndPointMetricsContext.EndPointMetrics getEndPointMetrics(Endpoint endpoint, Counter counter) {
        EndPointMetricsContext.EndPointMetrics metrics = new EndPointMetricsContext.EndPointMetrics();
        metrics.setEndpoint(endpoint);
        metrics.setAuthBlockedCounter(counter);
        return metrics;
    }

    private Endpoint getEndpoint(String serviceName, String interfaceName) {
        Endpoint endpoint1 = new Endpoint();
        endpoint1.setInterfaceName(interfaceName);
        endpoint1.setServiceName(serviceName);
        return endpoint1;
    }

    @Test
    public void testRemove() {
        Endpoint endpoint = getEndpoint("order", "save");
        Endpoint endpoint1 = getEndpoint("order", "save");

        MeterRegistry registry = new SimpleMeterRegistry();
        Counter counter = registry.counter("orderNum");
        counter.increment();


        EndPointMetricsContext.EndPointMetrics metrics = getEndPointMetrics(endpoint, counter);
        EndPointMetricsContext.EndPointMetrics metrics1 = getEndPointMetrics(endpoint1, counter);

        EndPointMetricsContext.register(metrics);


        EndPointMetricsContext.remove(metrics1.getEndpoint());

        Assert.assertEquals(0, EndPointMetricsContext.getEndPointMetricsContextCache().size());
    }


}