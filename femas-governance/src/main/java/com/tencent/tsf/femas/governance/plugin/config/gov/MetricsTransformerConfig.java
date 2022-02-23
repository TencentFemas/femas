package com.tencent.tsf.femas.governance.plugin.config.gov;

import com.tencent.tsf.femas.governance.plugin.config.PluginConfig;
import com.tencent.tsf.femas.governance.plugin.config.verify.Verifier;

/**
 * @Author p_mtluo
 * @Date 2021-11-09 16:26
 * @Description metrics transformer config
 **/
public interface MetricsTransformerConfig extends PluginConfig, Verifier {

    String getType();
}
