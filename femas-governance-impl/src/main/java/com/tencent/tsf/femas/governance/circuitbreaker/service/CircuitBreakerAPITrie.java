package com.tencent.tsf.femas.governance.circuitbreaker.service;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.governance.circuitbreaker.ICircuitBreakerService;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreaker;
import com.tencent.tsf.femas.governance.circuitbreaker.core.CircuitBreakerFactory;
import com.tencent.tsf.femas.governance.circuitbreaker.core.utils.CircuitBreakerUtil;
import com.tencent.tsf.femas.plugin.impl.config.rule.circuitbreaker.CircuitBreakerApi;
import com.tencent.tsf.femas.plugin.impl.config.rule.circuitbreaker.CircuitBreakerRule;
import com.tencent.tsf.femas.plugin.impl.config.rule.circuitbreaker.CircuitBreakerStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CircuitBreakerAPITrie {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerAPITrie.class);

    private TrieNode root;

    public CircuitBreakerAPITrie() {
        root = new TrieNode("");
    }

    public void buildTrie(CircuitBreakerRule rule) {
        Service service = rule.getTargetService();

        for (CircuitBreakerStrategy strategy : rule.getStrategyList()) {
            if (!strategy.validate()) {
                logger.warn("[FEMAS CIRCUIT BREAKER WARN] Strategy is not valid. Service = {}, {}", service,
                        strategy.toString());
                continue;
            }

            for (CircuitBreakerApi api : strategy.getApiList()) {
                try {
                    CircuitBreaker circuitBreaker = CircuitBreakerFactory
                            .newCircuitBreaker(CircuitBreakerUtil.append(service.toString(), api.toString()), rule,
                                    strategy);
                    circuitBreaker.setCircuitBreakerTargetObject(api.getMethod());

                    String method = api.getMethod();
                    String[] apiPath = method.split("/");

                    TrieNode node = root;
                    // 跳过第一个为空的str
                    for (int i = 0; i < apiPath.length; i++) {
                        node = node.getOrCreateSubNode(apiPath[i]);

                        // 叶子节点，需要塞入熔断器
                        if (i == apiPath.length - 1) {
                            node.circuitBreaker = new SingleFemasCircuitBreakerService(circuitBreaker);
                        }
                    }

                    logger.info("[FEMAS CIRCUIT BREAKER] Service {} API {} circuit breaker strategy updated. {}", service,
                            api, strategy.toString());
                } catch (Exception e) {
                    logger.error("[FEMAS CIRCUIT BREAKER ERROR] Service " + service + " API " + api
                            + " circuit breaker strategy update fail.", e);
                }
            }
        }
    }

    /**
     * 对于 /echo/这种最后为空的请求，不会匹配通配符 /echo/{param}，因为/echo/这种请求对于服务端是/echo
     *
     * @param method
     * @return
     */
    public ICircuitBreakerService search(String method) {
        String[] methodPath = method.split("/");

        TrieNode node = root;
        for (int i = 0; i < methodPath.length; i++) {
            if (node == null) {
                return null;
            }

            node = node.getSubNode(methodPath[i]);
            // 叶子节点
            if (i == methodPath.length - 1) {
                if (node == null) {
                    return null;
                } else {
                    return node.circuitBreaker;
                }
            }
        }

        return null;
    }

    private class TrieNode {

        // 增加非法字符，确保不会和前端传入的参数相同
        private static final String FEMAS_WILDCARD = "#femas_wildcard#";
        Map<String, TrieNode> children;
        String path;
        // 只有叶子节点才有熔断器
        ICircuitBreakerService circuitBreaker;

        // root 构造器
        public TrieNode(String path) {
            this.path = path;
            this.children = new HashMap<>();
        }

        TrieNode getSubNode(String nodeKey) {
            if (children.containsKey(nodeKey)) {
                return children.get(nodeKey);
            } else if (children.containsKey(FEMAS_WILDCARD)) {
                return children.get(FEMAS_WILDCARD);
            }

            return null;
        }

        // only for build trie
        public TrieNode getOrCreateSubNode(String path) {
            if (path.startsWith("{") && path.endsWith("}")) {
                path = FEMAS_WILDCARD;
            }

            if (!children.containsKey(path)) {
                children.putIfAbsent(path, new TrieNode(path));
            }

            return children.get(path);
        }
    }
}
