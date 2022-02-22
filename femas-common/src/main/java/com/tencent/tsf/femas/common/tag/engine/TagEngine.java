package com.tencent.tsf.femas.common.tag.engine;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.common.tag.TagExpression;
import com.tencent.tsf.femas.common.tag.TagRule;
import com.tencent.tsf.femas.common.tag.constant.TagConstant;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class TagEngine {

    /**
     * Tag规则正则表达式缓存MAP
     */
    private static Map<String, Pattern> PATTERN_CACHE_MAP = new ConcurrentHashMap<>();

    private static Context commonContext = ContextFactory.getContextInstance();

    /**
     * 校验 tag rule 是否匹配命中
     *
     * @return 是否匹配
     */
    public static Boolean checkRuleHitByUpstreamTags(TagRule tagRule) {
        return checkRuleHit(tagRule, Context.getRpcInfo().getAll(), commonContext.getUpstreamTags());
    }

    public static Boolean checkRuleHitByCurrentTags(TagRule tagRule) {
        return checkRuleHit(tagRule, Context.getRpcInfo().getAll(), commonContext.getCurrentTags());
    }

    /**
     * 校验 tag rule 是否匹配命中
     *
     * @return 是否匹配
     */
    public static Boolean checkRuleHit(TagRule tagRule, Map<String, String> sysTagMap, Map<String, String> userTagMap) {
        if (tagRule == null || CollectionUtil.isEmpty(tagRule.getTags())) {
            return true;
        }

        String expression = tagRule.getExpression();

        // 暂时表达式只支持 && 和 ||
        if (TagExpression.RELATION_AND.equalsIgnoreCase(expression)) {
            for (Tag tag : tagRule.getTags()) {
                if (!checkTagHit(tag, sysTagMap, userTagMap)) {
                    return false;
                }
            }

            return true;
        } else if (TagExpression.RELATION_OR.equalsIgnoreCase(expression)) {
            for (Tag tag : tagRule.getTags()) {
                if (checkTagHit(tag, sysTagMap, userTagMap)) {
                    return true;
                }
            }

            return false;
        } else if (TagExpression.RELATION_COMPOSITE.equalsIgnoreCase(expression)) {

        } else {
            throw new IllegalArgumentException("Tag Rule expression must in [AND,OR,COMPOSITE]");
        }

        return true;
    }


    /**
     * 校验 tag 是否匹配命中
     *
     * @return 是否匹配
     */
    public static Boolean checkTagHit(Tag tag, Map<String, String> sysTagMap, Map<String, String> userTagMap) {
        String requestTagValue;

        // 系统标签
        if (StringUtils.equals(tag.getTagType(), TagConstant.TYPE.SYSTEM)) {
            if (sysTagMap == null) {
                return false;
            }

            requestTagValue = sysTagMap.get(tag.getTagField());
        } else {
            // 类型为用户自定义标签，获取相应自定义标签字段取值
            if (userTagMap == null) {
                return false;
            }

            requestTagValue = userTagMap.get(tag.getTagField());
        }

        return TagEngine.matchTag(tag, requestTagValue);
    }

    /**
     * 校验TAG匹配规则是否满足
     *
     * @param tag 规则TAG
     * @param targetTagValue 请求实际TAG取值
     * @return 是否满足匹配规则
     */
    private static boolean matchTag(Tag tag, String targetTagValue) {
        // targetTagValue 为 null，有可能不等于成立

        String tagOperator = tag.getTagOperator();
        String tagValue = tag.getTagValue();

        if (StringUtils.equals(tagOperator, TagConstant.OPERATOR.EQUAL)) {
            // 匹配关系  等于
            return StringUtils.equals(tagValue, targetTagValue);
        } else if (StringUtils.equals(tagOperator, TagConstant.OPERATOR.NOT_EQUAL)) {
            // 匹配关系  不等于
            return !StringUtils.equals(tagValue, targetTagValue);
        } else if (StringUtils.equals(tagOperator, TagConstant.OPERATOR.IN)) {
            // 匹配关系  包含
            Set<String> routeTagValueSet = new HashSet<>(Arrays.asList(tagValue.split("\\s*,\\s*")));
            return routeTagValueSet.contains(targetTagValue);
        } else if (StringUtils.equals(tagOperator, TagConstant.OPERATOR.NOT_IN)) {
            // 匹配关系  不包含
            Set<String> routeTagValueSet = new HashSet<>(Arrays.asList(tagValue.split("\\s*,\\s*")));
            return !routeTagValueSet.contains(targetTagValue);
        } else if (StringUtils.equals(tagOperator, TagConstant.OPERATOR.REGEX)) {
            // 匹配关系  正则
            Pattern pattern = PATTERN_CACHE_MAP.get(tagValue);
            if (pattern == null) {
                pattern = Pattern.compile(tagValue);
                PATTERN_CACHE_MAP.putIfAbsent(targetTagValue, pattern);
            }

            return pattern.matcher(targetTagValue).matches();
        } else {
            return false;
        }
    }
}
