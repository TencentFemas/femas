package com.tencent.tsf.femas.storage.external.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.entity.rule.FemasCircuitBreakerRule;
import com.tencent.tsf.femas.entity.rule.FemasCircuitBreakerStrategy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Cody
 * @date 2021 2021/8/4 11:41 上午
 */
public class CircuitBreakerRowMapper implements RowMapper<FemasCircuitBreakerRule> {

    private final static ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public FemasCircuitBreakerRule mapRow(ResultSet rs, int rowNum) throws SQLException {
        FemasCircuitBreakerRule circuitBreakerRule = new FemasCircuitBreakerRule();
        circuitBreakerRule.setRuleId(rs.getString("rule_id"));
        circuitBreakerRule.setNamespaceId(rs.getString("namespace_id"));
        circuitBreakerRule.setServiceName(rs.getString("service_name"));
        circuitBreakerRule.setTargetServiceName(rs.getString("rule_name"));
        circuitBreakerRule.setTargetNamespaceId(rs.getString("target_namespace_id"));
        circuitBreakerRule.setRuleName(rs.getString("rule_name"));
        circuitBreakerRule.setIsolationLevel(rs.getString("isolation_level"));

//        circuitBreakerRule.setStrategy(
//                JSONSerializer.deserializeStr2List(FemasCircuitBreakerStrategy.class, rs.getString("strategy")));
        String routeTag = rs.getString("strategy");
        List<FemasCircuitBreakerStrategy> list = mapper.readValue(routeTag, new TypeReference<List<FemasCircuitBreakerStrategy>>() {});
        circuitBreakerRule.setStrategy(list);

        circuitBreakerRule.setIsEnable(rs.getString("is_enable"));
        circuitBreakerRule.setUpdateTime(rs.getLong("update_time"));
        circuitBreakerRule.setDesc(rs.getString("desc"));
        circuitBreakerRule.setTargetServiceName(rs.getString("target_service_name"));
        return circuitBreakerRule;
    }
}
