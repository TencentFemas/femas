package com.tencent.tsf.femas.service;


import com.tencent.tsf.femas.exception.IDQuantityInvalidException;
import java.util.List;

/**
 * TEM标准ID生成器服务接口
 *
 * @author hongweizhu
 */
public interface IIDGeneratorService {

    /**
     * 获取下一个经过hash的ID
     *
     * @return 下一个ID值
     */
    public String nextHashId();

    /**
     * 获取下N个经过hash的ID
     *
     * @param qty 需要获取ID的数量
     * @return 下qty个ID值列表
     * @throws IDQuantityInvalidException 没有填写qty或qty <= 0时抛出
     */
    public List<String> nextHashIds(Integer qty) throws IDQuantityInvalidException;

    /**
     * Gen next N integer ID number
     *
     * @param qty increasement of ingeter ID number
     * @return next N integer ID number list
     * @throws IDQuantityInvalidException if qty is null or nagetive or zero
     */
    public List<Long> nextIds(Integer qty) throws IDQuantityInvalidException;

    /**
     * Gen next integer ID number
     *
     * @return next integer ID number list
     */
    public Long nextId();

}
