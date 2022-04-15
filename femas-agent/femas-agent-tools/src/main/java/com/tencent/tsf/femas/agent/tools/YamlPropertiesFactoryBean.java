package com.tencent.tsf.femas.agent.tools;

import java.util.Properties;

public class YamlPropertiesFactoryBean extends YamlProcessor {

    private boolean singleton = true;

    private Properties properties;

    public boolean isSingleton() {
        return this.singleton;
    }

    /**
     * Set if a singleton should be created, or a new object on each request
     * otherwise. Default is {@code true} (a singleton).
     */
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public void afterPropertiesSet() {
        if (isSingleton()) {
            this.properties = createProperties();
        }
    }

    public Properties getObject() {
        return (this.properties != null ? this.properties : createProperties());
    }

    public Class<?> getObjectType() {
        return Properties.class;
    }


    /**
     * Template method that subclasses may override to construct the object
     * returned by this factory. The default implementation returns a
     * properties with the content of all resources.
     * <p>Invoked lazily the first time {@link #getObject()} is invoked in
     * case of a shared singleton; else, on each {@link #getObject()} call.
     *
     * @return the object returned by this factory
     * @see #process(MatchCallback) ()
     */
    protected Properties createProperties() {
        Properties result = createStringAdaptingProperties();
        process((properties, map) -> result.putAll(properties));
        return result;
    }

    private Properties createStringAdaptingProperties() {
        return new Properties() {
            @Override
            public String getProperty(String key) {
                Object value = get(key);
                return (value != null ? value.toString() : null);
            }
        };
    }
}
