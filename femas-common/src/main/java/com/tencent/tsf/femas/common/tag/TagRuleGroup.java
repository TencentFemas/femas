package com.tencent.tsf.femas.common.tag;

import java.util.List;

/**
 * TODO
 * 暂时又各个治理模块自己实现
 */
public class TagRuleGroup {

    List<TagRule> rules;

    /**
     * 规则运算表达式
     * 默认是或
     */
    private String expression;
}
