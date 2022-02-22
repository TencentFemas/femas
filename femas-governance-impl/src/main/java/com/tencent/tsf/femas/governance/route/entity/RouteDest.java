package com.tencent.tsf.femas.governance.route.entity;

import com.tencent.tsf.femas.common.tag.TagRule;
import java.io.Serializable;

/**
 *
 */
public class RouteDest implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 984582541720418394L;
    /**
     * 路由目标权重
     */
    private Integer destWeight;
    /**
     * 路由目标匹配条件列表
     */
    private TagRule destItemList;

    /**
     * 空构造函数
     */
    public RouteDest() {
    }

    public Integer getDestWeight() {
        return destWeight;
    }

    public void setDestWeight(Integer destWeight) {
        this.destWeight = destWeight;
    }

    public TagRule getDestItemList() {
        return destItemList;
    }

    public void setDestItemList(TagRule destItemList) {
        this.destItemList = destItemList;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RouteDest{");
        sb.append(", destWeight=").append(destWeight);
        sb.append(", destItemList=").append(destItemList);
        sb.append('}');
        return sb.toString();
    }
}
