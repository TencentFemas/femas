package com.tencent.tsf.femas.service.impl;

import com.tencent.tsf.femas.constant.Types;
import com.tencent.tsf.femas.exception.IDQuantityInvalidException;
import com.tencent.tsf.femas.service.IIDGeneratorService;
import com.tencent.tsf.femas.storage.DataOperation;
import com.tencent.tsf.femas.util.ResultCheck;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.math.NumberUtils;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IDGeneratorService implements IIDGeneratorService {


    /**
     * ID Hash工具，生成8位包含0-9a-z字符的ID。
     */
    private static Hashids HASH_IDS = new Hashids("FEMAS", 8, "0123456789abcdefghijklmnopqrstuvwxyz");
    @Autowired
    private DataOperation dataOperation;

    @Override
    public String nextHashId() {
        return this.nextHashIds(1).get(0);
    }

    @Override
    public List<String> nextHashIds(Integer qty) throws IDQuantityInvalidException {
        // 入参校验，数量必须是正整数
        if (null == qty || qty <= 0) {
            throw new IDQuantityInvalidException();
        }
        List<String> ids = new ArrayList<>();
        // 先更新锁定行
        /*if (idGeneratorDao.updateID(Types.HASH_ID, qty) > 0) {
            Long lastId = idGeneratorDao.findID(Types.HASH_ID);
            for (int i = 0; i < qty; i++) {
                ids.add(HASH_IDS.encode(lastId - qty + i));
            }
        }*/
        for (Long i : this.nextIds(qty)) {
            ids.add(HASH_IDS.encode(i));
        }
        return ids;
    }

    @Override
    public List<Long> nextIds(Integer qty) throws IDQuantityInvalidException {
        List<Long> ids = new ArrayList<>();
        Long newValue = NumberUtils.toLong(updateQty(qty));
        Long preId = newValue - qty;
        for (long i = newValue; i > preId; i--) {
            ids.add(i);
        }
        // TODO else
        return ids;
    }


    private synchronized String updateQty(Integer qty) {
        String data = dataOperation.fetchConfig(Types.HASH_ID_KEY);
        String value = "0";
        try {
            value = data == null ? setKV(value) : data;
        } catch (Exception e) {
            throw new IDQuantityInvalidException("update HASH_ID_KEY failed");
        }
        return setKV(String.valueOf(NumberUtils.toInt(value) + qty));
    }

    private String setKV(String value) {
        int res = dataOperation.configureConfig(Types.HASH_ID_KEY, value);
        if (ResultCheck.checkCount(res)) {
            return value;
        }
        throw new IDQuantityInvalidException("set HASH_ID_KEY failed");
    }

    @Override
    public Long nextId() {
        // TODO isEmpty
        return this.nextIds(1).get(0);
    }


}
