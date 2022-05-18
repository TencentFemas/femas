package com.tencent.tsf.femas.extensions.dubbo.util;

import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;
import com.tencent.tsf.femas.governance.lane.LaneService;
import org.apache.dubbo.rpc.RpcContext;

import java.util.HashMap;
import java.util.Map;

public class ApacheDubboAttachmentUtils extends AbstractRequestMetaUtils {

    private final RpcContext rpcContext;

    private ApacheDubboAttachmentUtils(RpcContext rpcContext){
        this.rpcContext = rpcContext;
    }

    public static ApacheDubboAttachmentUtils create (RpcContext rpcContext) {
        return new ApacheDubboAttachmentUtils(rpcContext);
    }

    @Override
    public void preprocess() {
        LaneService.headerPreprocess();
    }

    @Override
    public void setRequestMeta(String name, String value) {
        rpcContext.setAttachment(name, value);
    }

    @Override
    public String getRequestMeta(String name) {
        return rpcContext.getAttachment(name);
    }

    @Override
    public Map<String, String> getPrefixRequestMetas(String prefix) {
        Map<String, String> result = new HashMap<>();
        for(Map.Entry<String, String> attachment: rpcContext.getAttachments().entrySet()) {
            String key = attachment.getKey();
            if (key.startsWith(prefix)) {
                result.put(key, attachment.getValue());
            }
        }
        return result;
    }

    @Override
    public void getUniqueInfo() {

    }
}
