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

//    @Override
//    public boolean equals(Object o) {
//        if (o == this) {
//            return true;
//        } else if (!(o instanceof RouteDest)) {
//            return false;
//        } else {
//            RouteDest other = (RouteDest)o;
//            if (!other.canEqual(this)) {
//                return false;
//            } else {
//                Object this$destWeight = this.getDestWeight();
//                Object other$destWeight = other.getDestWeight();
//                if (this$destWeight == null) {
//                    if (other$destWeight != null) {
//                        return false;
//                    }
//                } else if (!this$destWeight.equals(other$destWeight)) {
//                    return false;
//                }
//
//                Object this$destItemList = this.getDestItemList();
//                Object other$destItemList = other.getDestItemList();
//                if (this$destItemList == null) {
//                    if (other$destItemList != null) {
//                        return false;
//                    }
//                } else if (!this$destItemList.equals(other$destItemList)) {
//                    return false;
//                }
//
//                return true;
//            }
//        }
//    }
//
//    protected boolean canEqual(Object other) {
//        return other instanceof RouteDest;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = 1;
//        Object destWeight = this.getDestWeight();
//        result = result * 59 + (destWeight == null ? 43 : destWeight.hashCode());
//        Object destItemList = this.getDestItemList();
//        result = result * 59 + (destItemList == null ? 43 : destItemList.hashCode());
//        return result;
//    }
}
