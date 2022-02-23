package com.tencent.tsf.femas.api;

import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;

public class ExtensionManager {

    private static volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    // 后续支持扩展
    private static IExtensionLayer extensionLayer = new CommonExtensionLayer();

    public static IExtensionLayer getExtensionLayer() {
        return extensionLayer;
    }

}
