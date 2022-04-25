package com.tencent.tsf.femas.entity.rule.route;

import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.entity.rule.DestTag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

public class RouteTag {

    @ApiModelProperty("流量来源规则标签")
    private List<Tag> tags;

    @ApiModelProperty("流量目的地")
    private List<DestTag> destTag;

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<DestTag> getDestTag() {
        return destTag;
    }

    public void setDestTag(List<DestTag> destTag) {
        this.destTag = destTag;
    }

}
