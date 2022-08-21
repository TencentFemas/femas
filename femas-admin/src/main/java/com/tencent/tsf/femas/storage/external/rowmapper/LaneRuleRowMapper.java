package com.tencent.tsf.femas.storage.external.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.entity.rule.lane.GrayTypeEnum;
import com.tencent.tsf.femas.entity.rule.lane.LaneRule;
import com.tencent.tsf.femas.entity.rule.lane.LaneRuleTag;
import com.tencent.tsf.femas.entity.rule.lane.RuleTagRelationship;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: cody
 * @Date: 2022/7/26
 * @Descriptioin
 */
public class LaneRuleRowMapper implements RowMapper<LaneRule> {

    private final static ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public LaneRule mapRow(ResultSet rs, int rowNum) throws SQLException  {
        LaneRule laneRule = new LaneRule();
        laneRule.setRuleId(rs.getString("rule_id"));
        laneRule.setRuleName(rs.getString("rule_name"));
        laneRule.setCreateTime(rs.getLong("create_time"));
        laneRule.setRemark(rs.getString("remark"));
        laneRule.setUpdateTime(rs.getLong("update_time"));
        HashMap<String, Integer> relativeLane = mapper.readValue(rs.getString("relative_lane"), new TypeReference<HashMap<String, Integer>>() {});
        laneRule.setRelativeLane(relativeLane);
        laneRule.setGrayType(GrayTypeEnum.valueOf(rs.getString("gray_type")));
        laneRule.setEnable(rs.getInt("enable"));
        laneRule.setRuleTagRelationship(RuleTagRelationship.valueOf(rs.getString("rule_tag_relationship")));
        String ruleTagListStr = rs.getString("rule_tag_list");
        List<LaneRuleTag> ruleTagList = mapper.readValue(ruleTagListStr, new TypeReference<List<LaneRuleTag>>() {});
        laneRule.setRuleTagList(ruleTagList);
        return laneRule;
    }
}
