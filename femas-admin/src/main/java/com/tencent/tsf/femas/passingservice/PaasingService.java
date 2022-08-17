package com.tencent.tsf.femas.passingservice;


import com.tencent.tsf.femas.common.annotation.SPI;

/**
 * @author huyuanxin
 */
@SPI
public interface PaasingService {

    /**
     * 启动
     */
    default void start(){
        new Thread(this::doStart).start();
    }

    /**
     * 返回服务类型
     *
     * @return 服务类型
     */
    String getType();

    /**
     * 启动实现
     */
    void doStart();

}
