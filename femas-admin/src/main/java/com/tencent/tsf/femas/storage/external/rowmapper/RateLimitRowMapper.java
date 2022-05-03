package com.tencent.tsf.femas.storage.external.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.entity.rule.FemasLimitRule;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Cody
 * @date 2021 2021/8/4 11:43 上午
 */
public class RateLimitRowMapper implements RowMapper<FemasLimitRule> {

    private final static ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public FemasLimitRule mapRow(ResultSet rs, int rowNum) throws SQLException {
        FemasLimitRule limitRule = new FemasLimitRule();
        limitRule.setRuleId(rs.getString("rule_id"));
        limitRule.setNamespaceId(rs.getString("namespace_id"));
        limitRule.setServiceName(rs.getString("service_name"));
        limitRule.setRuleName(rs.getString("rule_name"));
        limitRule.setType(FemasLimitRule.Type.valueOf(rs.getString("type")));

//        limitRule.setTags(JSONSerializer.deserializeStr2List(Tag.class, rs.getString("tags")));
        String tags = rs.getString("tags");
        List<Tag> tagList = mapper.readValue(tags, new TypeReference<List<Tag>>() {});
        limitRule.setTags(tagList);

        limitRule.setDuration(rs.getInt("duration"));
        limitRule.setTotalQuota(rs.getInt("total_quota"));
        limitRule.setStatus(rs.getInt("status"));
        limitRule.setUpdateTime(rs.getLong("update_time"));
        limitRule.setDesc(rs.getString("desc"));
        return limitRule;
    }
}
