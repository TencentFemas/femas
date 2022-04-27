package com.tencent.tsf.femas.governance.plugin.context;

import com.tencent.tsf.femas.agent.classloader.AgentClassLoader;
import com.tencent.tsf.femas.agent.classloader.InterceptorClassLoaderCache;
import com.tencent.tsf.femas.common.context.AgentConfig;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.plugin.DefaultConfigurablePluginHolder;
import com.tencent.tsf.femas.governance.plugin.Plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import static com.tencent.tsf.femas.common.context.ContextConstant.START_AGENT_FEMAS;

/**
 * 插件容器
 *
 * @author leoziltong
 */
public class ConfigRefreshableContext implements AbstractSDKContext {

    /**
     * Map<插件类型:限流、熔断,<具体插件名,插件名对象的插件实例对象>
     * ex: Map<RateLimit.class,<'SlideWindow','SlidingWindowRateLimiter'>>
     */
    private Map<Class<? extends Plugin>, Map<String, Plugin>> typedPlugins = new ConcurrentHashMap<>();

    @Override
    public void initPlugins(ConfigContext context, List<Class<? extends Plugin>> types) throws FemasRuntimeException {
        for (Class<? extends Plugin> pluginType : types) {
            Map<String, Plugin> plugins = new HashMap<>();
            typedPlugins.put(pluginType, plugins);
            //spi加载器加载不到agent class的问题
            if (AgentConfig.doGetProperty(START_AGENT_FEMAS) != null && (Boolean) AgentConfig.doGetProperty(START_AGENT_FEMAS)) {
                AgentClassLoader agentClassLoader;
                try {
                    agentClassLoader = InterceptorClassLoaderCache.getAgentClassLoader(ConfigRefreshableContext.class.getClassLoader());
                } catch (Exception e) {
                    agentClassLoader = InterceptorClassLoaderCache.getAgentClassLoader(Thread.currentThread().getContextClassLoader());
                }
                Thread.currentThread().setContextClassLoader(agentClassLoader);
            }
            ServiceLoader<? extends Plugin> loader = ServiceLoader.load(pluginType);
            Iterator<? extends Plugin> iterator = loader.iterator();
            while (iterator.hasNext()) {
                Plugin plugin = iterator.next();
                String name = plugin.getName();
                if (StringUtils.isBlank(name) || plugins.containsKey(name)) {
                    throw new FemasRuntimeException(
                            String.format("duplicated name for plugin(name=%s, type=%s) not found",
                                    name, pluginType.getCanonicalName()));
                }
                plugin.init(context);
                plugins.put(name, plugin);
            }
        }
    }

    public void register() {

    }

    public void onRefresh(Class<? extends Plugin> type, String name, final Object config) {
        Plugin plugin = this.getPlugin(type, name);
        plugin.freshFactoryBySpecifyConfig(config);
    }


    /**
     * 销毁已初始化的插件列表
     */
    @Override
    public void destroyPlugins() {
        for (Map.Entry<Class<? extends Plugin>, Map<String, Plugin>> typedEntry : typedPlugins.entrySet()) {
            Map<String, Plugin> plugins = typedEntry.getValue();
            for (Map.Entry<String, Plugin> plugin : plugins.entrySet()) {
                plugin.getValue().destroy();
            }
        }
    }

    @Override
    public Plugin getPlugin(Class<? extends Plugin> type, String name) throws FemasRuntimeException {
        if (!typedPlugins.containsKey(type)) {
            throw new FemasRuntimeException(
                    String.format("plugins(type=%s) not found", type.getCanonicalName()));
        }
        Map<String, Plugin> plugins = typedPlugins.get(type);
        if (!plugins.containsKey(name)) {
            throw new FemasRuntimeException(
                    String.format("plugin(name=%s, type=%s) not found", name, type.getCanonicalName()));
        }
        return plugins.get(name);
    }


}
