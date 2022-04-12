package com.tencent.tsf.femas.storage.external;

import com.tencent.tsf.femas.exception.FemasException;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Cody
 * @date 2021 2021/7/28 5:19 下午
 */
@Component
public class JdbcTemplateContext {

    private static JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public JdbcTemplateContext(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static JdbcTemplate getTemplate() {
        return jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        if (dataSource == null) {
            throw new FemasException("datasource not available");
        }
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setMaxRows(100000);
        jdbcTemplate.setQueryTimeout(5000);
    }
}
