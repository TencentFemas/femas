package com.tencent.tsf.femas.storage.external.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.entity.ServiceInfo;
import com.tencent.tsf.femas.entity.rule.lane.LaneInfo;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @Author: cody
 * @Date: 2022/7/26
 * @Descriptioin
 */
public class LaneRowMapper implements RowMapper<LaneInfo> {

    private final static ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public LaneInfo mapRow(ResultSet rs, int rowNum) throws SQLException  {
        LaneInfo laneInfo = new LaneInfo();
        laneInfo.setLaneId(rs.getString("lane_id"));
        laneInfo.setRemark(rs.getString("remark"));
        laneInfo.setLaneName(rs.getString("lane_name"));
        laneInfo.setCreateTime(rs.getLong("create_time"));
        laneInfo.setUpdateTime(rs.getLong("update_time"));
        String laneServiceListStr = rs.getString("lane_service_list");
        List<ServiceInfo> laneServiceList = mapper.readValue(laneServiceListStr, new TypeReference<List<ServiceInfo>>() {});
        laneInfo.setLaneServiceList(laneServiceList);
        return laneInfo;
    }
}
