package com.tencent.tsf.femas.entity.metrix.model;

import com.tencent.tsf.femas.common.entity.Service;
import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/8/15 9:53 下午
 */
public class ServiceMetricModel {

    private List<Service> services;

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }
}
