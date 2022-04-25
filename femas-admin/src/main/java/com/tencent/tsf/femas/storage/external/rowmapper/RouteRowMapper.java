package com.tencent.tsf.femas.storage.external.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.entity.rule.FemasRouteRule;
import com.tencent.tsf.femas.entity.rule.route.RouteTag;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Cody
 * @date 2021 2021/8/4 11:42 上午
 */
public class RouteRowMapper implements RowMapper<FemasRouteRule> {

    private final static ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public FemasRouteRule mapRow(ResultSet rs, int rowNum) {
        FemasRouteRule routeRule = new FemasRouteRule();
        routeRule.setRuleId(rs.getString("rule_id"));
        routeRule.setNamespaceId(rs.getString("namespace_id"));
        routeRule.setServiceName(rs.getString("service_name"));
        routeRule.setRuleName(rs.getString("rule_name"));
        routeRule.setStatus(rs.getString("status"));

//        routeRule.setRouteTag(JSONSerializer.deserializeStr2List(RouteTag.class, rs.getString("route_tag")));
        String routeTag = rs.getString("route_tag");
        List<RouteTag> list = mapper.readValue(routeTag, new TypeReference<List<RouteTag>>() {});
        routeRule.setRouteTag(list);

        routeRule.setCreateTime(rs.getLong("create_time"));
        routeRule.setUpdateTime(rs.getLong("update_time"));
        routeRule.setDesc(rs.getString("desc"));
        return routeRule;
    }
}
