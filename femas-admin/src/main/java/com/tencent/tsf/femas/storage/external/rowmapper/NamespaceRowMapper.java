package com.tencent.tsf.femas.storage.external.rowmapper;

import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.entity.namespace.Namespace;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Cody
 * @date 2021 2021/8/17 5:31 下午
 */
public class NamespaceRowMapper implements RowMapper<Namespace> {

    @Override
    public Namespace mapRow(ResultSet rs, int rowNum) throws SQLException {
        Namespace namespace = new Namespace();
        namespace.setNamespaceId(rs.getString("namespace_id"));
        namespace.setRegistryId(JSONSerializer.deserializeStr2List(String.class, rs.getString("registry_id")));
        namespace.setName(rs.getString("name"));
        namespace.setDesc(rs.getString("desc"));
        return namespace;
    }
}
