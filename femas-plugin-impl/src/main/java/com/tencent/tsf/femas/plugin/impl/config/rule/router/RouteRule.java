package com.tencent.tsf.femas.plugin.impl.config.rule.router;

import com.tencent.tsf.femas.common.tag.TagRule;

import java.io.Serializable;
import java.util.List;

/**
 * 路由规则
 */
public class RouteRule implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -1886125299472426511L;
    /**
     * 路由规则项包含的匹配条件列表
     */
    private TagRule tagRule;
    /**
     * 路由规则项包含的目的列表
     */
    private List<RouteDest> destList;

    /**
     * 空构造函数
     */
    public RouteRule() {
    }

    public TagRule getTagRule() {
        return tagRule;
    }

    public void setTagRule(TagRule tagRule) {
        this.tagRule = tagRule;
    }

    public List<RouteDest> getDestList() {
        return destList;
    }

    public void setDestList(List<RouteDest> destList) {
        this.destList = destList;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RouteRule{");
        sb.append(", tagRule=").append(tagRule);
        sb.append(", destList=").append(destList);
        sb.append('}');
        return sb.toString();
    }
}
