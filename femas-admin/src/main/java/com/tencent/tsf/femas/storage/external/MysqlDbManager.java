package com.tencent.tsf.femas.storage.external;

import com.tencent.tsf.femas.entity.PageService;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * @author Cody
 * @date 2021 2021/7/28 5:41 下午
 */
@Component
@DependsOn("jdbcTemplateContext")
public class MysqlDbManager {

    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        jdbcTemplate = JdbcTemplateContext.getTemplate();
    }

    public int update(String sql, Object... obj) {
        return jdbcTemplate.update(sql, obj);
    }

    public int deleteById(String table, String column, String value) {
        String sql = "delete from " + table + " where " + column + "= ?";
        return jdbcTemplate.update(sql, value);
    }

    public <T> List<T> selectListPojo(String sql, Class<T> t, Object... obj) {
        List<Object> list = jdbcTemplate.query(sql, obj, (RowMapper<Object>) new BeanPropertyRowMapper<T>(t));
        return (List<T>) list;

    }

    public <T> T selectPojo(String sql, Class<T> t, Object... obj) {
        Object res = null;
        try {
            res = jdbcTemplate.queryForObject(sql, obj, new BeanPropertyRowMapper<T>(t));
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw e;
        }
        return (T) res;
    }

    public <T> T selectPojoByMapper(RowMapper<T> rowMapper, String sql, Object... obj) {
        Object res = null;
        try {
            res = jdbcTemplate.queryForObject(sql, obj, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw e;
        }
        return (T) res;
    }


    public <T> List<Map<String, Object>> selectListMap(String sql, Object... obj) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, obj);
        return (List) list;
    }

    public int selectInteger(String sql, Object... obj) {
        Integer res = 0;
        try {
            res = jdbcTemplate.queryForObject(sql, Integer.class, obj);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw e;
        }
        return res;
    }

    public String selectString(String sql, Object... obj) {
        String res = null;
        try {
            res = jdbcTemplate.queryForObject(sql, String.class, obj);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw e;
        }
        return res;
    }

    public <T> List selectListString(String sql, Object... obj) {
        List<String> list = jdbcTemplate.queryForList(sql, String.class, obj);
        return (List) list;
    }

    public <T> List<T> selectListPojoByMapper(RowMapper<T> mapper, String sql, Object... params) {
        List<T> list = jdbcTemplate.query(sql, params, mapper);
        return list;
    }

    public <T> T selectById(RowMapper<T> mapper, String table, String column, String value) {
        String sql = "select * from " + table + " where " + column + "=?";
        return selectPojoByMapper(mapper, sql, value);
    }

    // 分页查询
    public <T> PageService<T> selectByPages(RowMapper<T> rowMapper, String sql, int pageNo, int pageSize,
            Object... params) {
        int offset = (pageNo - 1) * pageSize;
        int totalCount = getSQLCount(sql, params);
        String pagesSql = sql + " limit " + offset + "," + pageSize;
        List<T> data = selectListPojoByMapper(rowMapper, pagesSql, params);
        PageService<T> pageService = new PageService<T>();
        pageService.setCount(totalCount);
        pageService.setData(data);
        return pageService;
    }

    // 带排序分页查询
    public <T> PageService<T> selectByPagesOrdered(RowMapper<T> rowMapper, String sql, int pageNo, int pageSize,
            OrderedType orderedType, String column, Object... params) {
        int offset = (pageNo - 1) * pageSize;
        int totalCount = getSQLCount(sql, params);
        String pagesSql = sql + " order by " + column + " " + orderedType.name() + " limit " + offset + "," + pageSize;
        List<T> data = selectListPojoByMapper(rowMapper, pagesSql, params);
        PageService<T> pageService = new PageService<T>();
        pageService.setCount(totalCount);
        pageService.setData(data);
        return pageService;
    }

    // 获取sql操作条数
    private int getSQLCount(String sql, Object... params) {
        String baseSql = sql.toLowerCase();
        String searchValue = "from";
        String sqlCount = "select count(1) from " + sql.substring(baseSql.indexOf(searchValue) + searchValue.length());
        return selectInteger(sqlCount, params);
    }

    // 批量插入
    public void batchInsert(String sql, List<Object[]> params) {
        jdbcTemplate.batchUpdate(sql, params);
    }

    public enum OrderedType {
        // 降序排列
        DESC,

        // 升序排列
        ASC
    }
}
