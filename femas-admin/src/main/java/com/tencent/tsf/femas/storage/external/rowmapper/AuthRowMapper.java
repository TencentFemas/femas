package com.tencent.tsf.femas.storage.external.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.entity.rule.FemasAuthRule;
import com.tencent.tsf.femas.entity.rule.auth.RuleTypeEnum;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Cody
 * @date 2021 2021/8/4 11:39 上午
 */
public class AuthRowMapper implements RowMapper<FemasAuthRule> {

    private final static ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public FemasAuthRule mapRow(ResultSet rs, int rowNum) throws SQLException {
        FemasAuthRule authRule = new FemasAuthRule();
        authRule.setRuleId(rs.getString("rule_id"));
        authRule.setNamespaceId(rs.getString("namespace_id"));
        authRule.setServiceName(rs.getString("service_name"));
        authRule.setRuleName(rs.getString("rule_name"));
        authRule.setIsEnabled(rs.getString("is_enable"));
        authRule.setRuleType(RuleTypeEnum.valueOf(rs.getString("rule_type")));
        authRule.setCreateTime(rs.getLong("create_time"));
        authRule.setAvailableTime(rs.getLong("available_time"));

//        authRule.setTags(JSONSerializer.deserializeStr2List(Tag.class, rs.getString("tags")));
        String tags = rs.getString("tags");
        List<Tag> tagList = mapper.readValue(tags, new TypeReference<List<Tag>>() {});
        authRule.setTags(tagList);

        authRule.setTagProgram(rs.getString("tag_program"));
        authRule.setTarget(rs.getString("target"));
        authRule.setDesc(rs.getString("desc"));
        return authRule;
    }
}
