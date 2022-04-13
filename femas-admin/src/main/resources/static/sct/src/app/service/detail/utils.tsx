import * as React from 'react';
import { Text } from 'tea-component';
import { RouteRule } from './route/types';
import { OPERATOR, OPERATOR_MAP, SYSTEM_TAG, SYSTEM_TAG_MAP, TAG_TYPE } from './tagTable/types';

// 拼接限流规则
export async function pingTagRuleContent(tags) {
  const tagsJsx = [];
  if (!tags?.length) return tagsJsx;

  for (let index = 0; index < tags.length; index++) {
    const tag = tags[index];
    const description = await getTagDescription(tag);
    tagsJsx.push(
      <Text key={`tag${index}`}>
        {index !== 0 ? <Text style={{ fontWeight: 'bolder' }}> 且 </Text> : ''}
        {description}
      </Text>,
    );
  }
  return tagsJsx;
}

export async function getTagDescription(tag) {
  const { tagField, tagOperator, tagType, tagValue } = tag;

  const isSystem = tagType === TAG_TYPE.SYSTEM;

  const tagFieldDesc = isSystem ? SYSTEM_TAG_MAP[tagField] : tagField,
    tagOperatorDesc = OPERATOR_MAP[tagOperator];
  let tagValueDesc = '';

  if (isSystem) {
    if (tagField === SYSTEM_TAG.TSF_LOCAL_SERVICE) {
      // 是服务
      tagValueDesc = await getGeneralValue(tagOperator, tagValue, getServiceName);
    } else {
      tagValueDesc = tagValue;
    }
  } else {
    tagValueDesc = tagValue;
  }
  return (
    <Text>
      {tagFieldDesc}
      &nbsp;
      {tagOperatorDesc}
      &nbsp;
      <Text theme={'warning'}>{tagValueDesc}</Text>
    </Text>
  );
}

async function getGeneralValue(tagOperator, idStr, handler) {
  if (tagOperator === OPERATOR.REGEX) {
    return idStr;
  } else if (idStr.indexOf(',') >= 0) {
    const idList = idStr.split(',');
    const nameList = [];

    for (const id of idList) {
      nameList.push(await handler(id));
    }

    return nameList.join(',');
  } else {
    return handler(idStr);
  }
}

async function getServiceName(serviceNameStr) {
  // [namespaceId & serviceName]
  const [serviceName] = serviceNameStr.split('/').reverse();
  return serviceName;
}

// 根据类型拼接路由规则内容
export async function pingRuleFullContent(routeRule: RouteRule) {
  const routeTagRule = await pingTagRuleContent(routeRule.tags);
  const destRule = await asyncPingRuleContent(routeRule);
  return (
    <span>
      {routeRule.tags?.length ? '流量来源满足' : '全部流量'}
      {routeTagRule}
      &nbsp;&nbsp;&nbsp;&nbsp;
      {destRule}
    </span>
  );
}

// 异步完整版
export async function asyncPingRuleContent(routeRule: RouteRule) {
  const routeRuleList = [];

  if (!routeRule.destTag?.length) return routeRuleList;

  for (let index = 0; index < routeRule.destTag.length; index++) {
    const item = routeRule.destTag[index];
    routeRuleList.push(
      <Text key={`dest${index}`}>
        服务版本号：{item.serviceVersion}
        &nbsp;&nbsp;
        <Text theme={'warning'}>
          ({item.weight}
          %)
        </Text>
        {index !== routeRule.destTag.length - 1 && '，'}
      </Text>,
    );
  }
  return (
    <Text>
      分配给
      {routeRuleList}
    </Text>
  );
}

// 同步简化版
export function pingRuleContent(routeRule: RouteRule) {
  return (
    <Text>
      路由目的地：
      {routeRule.destTag.map((item, index) => (
        <Text key={`dest-desc${index}`}>
          服务版本号 {item.serviceVersion}
          <Text theme={'warning'}>
            ({item.weight}
            %)
          </Text>
          {index !== routeRule.destTag.length - 1 && '，'}
        </Text>
      ))}
    </Text>
  );
}
