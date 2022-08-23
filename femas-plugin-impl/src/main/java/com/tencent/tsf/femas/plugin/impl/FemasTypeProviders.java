package com.tencent.tsf.femas.plugin.impl;



import com.tencent.tsf.femas.plugin.Attribute;
import com.tencent.tsf.femas.plugin.Plugin;
import com.tencent.tsf.femas.plugin.PluginProvider;

import java.util.ArrayList;
import java.util.List;

public class FemasTypeProviders implements PluginProvider {

    @Override
    public List<Class<? extends Plugin>> getPluginTypes() {
        List<Class<? extends Plugin>> types = new ArrayList<>();
        for (SPIPluginType type : SPIPluginType.values()) {
            types.add(type.getInterfaces());
        }
        return types;
    }

    @Override
    public Attribute getAttr() {
        return new Attribute(Attribute.Implement.FEMAS, "femasPluginMenu");
    }

    @Override
    public String getType() {
        return this.getClass().getTypeName();
    }

}
