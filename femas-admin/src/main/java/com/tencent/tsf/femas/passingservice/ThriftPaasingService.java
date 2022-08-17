package com.tencent.tsf.femas.passingservice;

import com.tencent.tsf.femas.config.thrift.paas.PaasPolling;
import com.tencent.tsf.femas.config.thrift.paas.ThriftHelper;
import com.tencent.tsf.femas.endpoint.configlisten.ThriftLongPollingEndpoint;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.layered.TFramedTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author huyuanxin
 */
public class ThriftPaasingService implements PaasingService {

    private final Logger logger = LoggerFactory.getLogger(ThriftPaasingService.class);

    @Override
    public void doStart() {
        try {
            int thriftListenPort = ThriftHelper.getThriftListenPort();
            TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(thriftListenPort);
            //参数设置
            THsHaServer.Args arg = new THsHaServer.Args(serverSocket).minWorkerThreads(2).maxWorkerThreads(4);
            //处理器
            PaasPolling.Processor<PaasPolling.Iface> processor = new PaasPolling.Processor<>(new ThriftLongPollingEndpoint());
            arg.protocolFactory(new TCompactProtocol.Factory());
            arg.transportFactory(new TFramedTransport.Factory());
            arg.processorFactory(new TProcessorFactory(processor));
            TServer server = new TNonblockingServer(arg);
            server.serve();
        } catch (Exception e) {
            logger.error("Error with staring thrift paasing server:{0}", e);
        }

    }

    @Override
    public String getType() {
        return "ThriftPaasingService";
    }

}
