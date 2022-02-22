package com.tencent.tsf.femas.common.rule;

import com.tencent.tsf.femas.common.context.Context;

public abstract class AbstractRuleManager<T> {

    /**
     * 根据Rule Id匹配对象
     *
     * @param ruleId
     * @return
     */
    protected abstract T match(String ruleId);

    /**
     * 根据上下文Context匹配对象
     *
     * @param context
     * @return
     */
    protected abstract T match(Context context);

    /**
     * 将Rule装载到manager中
     *
     * @param rule
     */
    protected abstract void load(Rule rule);
}
