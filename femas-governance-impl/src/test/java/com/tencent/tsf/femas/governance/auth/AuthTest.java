package com.tencent.tsf.femas.governance.auth;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.common.tag.TagRule;
import com.tencent.tsf.femas.common.tag.constant.TagConstant;
import com.tencent.tsf.femas.governance.auth.constant.AuthConstant;
import com.tencent.tsf.femas.governance.auth.entity.AuthRuleGroup;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class AuthTest {

    public static Service service = new Service("default_ns", "provider-demo");

    /**
     * 测试黑名单
     */
    @Test
    public void test01() {
        AuthorizationManager.disableAuthRuleGroup(null);
        AuthorizationManager.disableAuthRuleGroup(service);

        AuthRuleGroup authRuleGroup = new AuthRuleGroup();
        authRuleGroup.setType(AuthConstant.BLACK_LIST);

        Tag tag = new Tag();
        tag.setTagField("zone");
        tag.setTagType(TagConstant.TYPE.SYSTEM);
        tag.setTagValue("1");
        tag.setTagOperator(TagConstant.OPERATOR.EQUAL);

        List<Tag> tags = new ArrayList<>();
        tags.add(tag);

        TagRule tagRule = new TagRule();
        tagRule.setTags(tags);
        List<TagRule> rules = new ArrayList<>();
        rules.add(tagRule);

        authRuleGroup.setRules(rules);

        AuthorizationManager.refreshAuthRuleGroup(service, authRuleGroup);

        Context.getRpcInfo().put("zone", "1");
        Assert.assertFalse(AuthorizationManager.authenticate(service));

        Context.getRpcInfo().put("zone", "2");
        Assert.assertTrue(AuthorizationManager.authenticate(service));

        AuthorizationManager.disableAuthRuleGroup(service);
    }

    /**
     * 测试白名单
     */
    @Test
    public void test02() {
        AuthorizationManager.disableAuthRuleGroup(null);
        AuthorizationManager.disableAuthRuleGroup(service);

        AuthRuleGroup authRuleGroup = new AuthRuleGroup();
        authRuleGroup.setType(AuthConstant.WHITE_LIST);

        Tag tag = new Tag();
        tag.setTagField("zone");
        tag.setTagType(TagConstant.TYPE.SYSTEM);
        tag.setTagValue("1");
        tag.setTagOperator(TagConstant.OPERATOR.EQUAL);

        List<Tag> tags = new ArrayList<>();
        tags.add(tag);

        TagRule tagRule = new TagRule();
        tagRule.setTags(tags);
        List<TagRule> rules = new ArrayList<>();
        rules.add(tagRule);

        authRuleGroup.setRules(rules);

        AuthorizationManager.refreshAuthRuleGroup(service, authRuleGroup);

        Context.getRpcInfo().put("zone", "1");
        Assert.assertTrue(AuthorizationManager.authenticate(service));

        Context.getRpcInfo().put("zone", "2");
        Assert.assertFalse(AuthorizationManager.authenticate(service));

        AuthorizationManager.disableAuthRuleGroup(service);
    }

    /**
     * 测试disable
     */
    @Test
    public void test03() {
        AuthorizationManager.disableAuthRuleGroup(null);
        AuthorizationManager.disableAuthRuleGroup(service);

        AuthRuleGroup authRuleGroup = new AuthRuleGroup();
        authRuleGroup.setType(AuthConstant.DISABLED);

        Tag tag = new Tag();
        tag.setTagField("zone");
        tag.setTagType(TagConstant.TYPE.SYSTEM);
        tag.setTagValue("1");
        tag.setTagOperator(TagConstant.OPERATOR.EQUAL);

        List<Tag> tags = new ArrayList<>();
        tags.add(tag);

        TagRule tagRule = new TagRule();
        tagRule.setTags(tags);
        List<TagRule> rules = new ArrayList<>();
        rules.add(tagRule);

        authRuleGroup.setRules(rules);

        AuthorizationManager.refreshAuthRuleGroup(service, authRuleGroup);

        Context.getRpcInfo().put("zone", "1");
        Assert.assertTrue(AuthorizationManager.authenticate(service));

        Context.getRpcInfo().put("zone", "2");
        Assert.assertTrue(AuthorizationManager.authenticate(service));

        AuthorizationManager.disableAuthRuleGroup(service);
    }
}
