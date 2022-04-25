package com.tencent.tsf.femas.entity.registry.eureka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * @author Leo
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EurekaService {

    private String name;

    private List<EurekaInstance> instance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EurekaInstance> getInstance() {
        return instance;
    }

    public void setInstance(List<EurekaInstance> instance) {
        this.instance = instance;
    }

}
