package com.tencent.tsf.femas.storage.external.rowmapper;

import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.entity.rule.FemasEventData;
import com.tencent.tsf.femas.entity.service.EventTypeEnum;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Cody
 * @date 2021 2021/8/4 8:11 下午
 */
public class ServiceEventRowMapper implements RowMapper<FemasEventData> {

    @Override
    public FemasEventData mapRow(ResultSet rs, int rowNum) throws SQLException {
        FemasEventData eventData = new FemasEventData();
        eventData.setNamespaceId(rs.getString("namespace_id"));
        eventData.setServiceName(rs.getString("service_name"));
        eventData.setEventType(EventTypeEnum.valueOf(rs.getString("event_type")));
        eventData.setUpstream(rs.getString("upstream"));
        eventData.setDownstream(rs.getString("downstream"));
        eventData.setInstanceId(rs.getString("instance_id"));
        eventData.setOccurTime(rs.getLong("occur_time"));
        eventData.setAdditionalMsg(JSONSerializer.deserializeStr(Map.class, rs.getString("additional_msg")));
        return eventData;
    }
}
