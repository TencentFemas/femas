package com.tencent.tsf.femas.entity.metrix.prom;

/**
 * @author Cody
 * @date 2021 2021/8/12 11:03 上午
 *         prometheus Result
 */
public class PromResponse<T> {

    private String status;

    private PromDataInfo<T> data;

    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PromDataInfo getData() {
        return data;
    }

    public void setData(PromDataInfo data) {
        this.data = data;
    }

}
