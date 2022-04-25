import { RULE_STATUS, Tag } from '../../types';

export interface RouteRuleItem {
  id: string;
  serviceName: string;
  registryId: string;
  namespaceId: string;
  ruleId: string;
  ruleName: string;
  status?: RULE_STATUS; //路由规则生效状态
  routeTag: Array<RouteRule>; //路由规则包含路由规则项列表
  tags: Array<Tag>;
  routeDesc?: any; //路由规则描述信息
  updateTime: number;
  createTime: number;
  checked?: boolean;
}

export interface RouteRule {
  //路由规则项包含目标列表
  destTag: Array<RouteRuleDest>;

  //路由规则项包含TAG列表
  tags?: Array<Tag>;
}

//路由规则项包含目标
export interface RouteRuleDest {
  // 服务版本号
  serviceVersion?: string;
  // 路由目标权重
  weight: number;
}

export interface TolerantItem {
  isTolerant: RULE_STATUS;
  serviceName: string;
  namespaceId: string;
}

export enum SYSTEM_TAG {
  TSF_PROG_VERSION = 'TSF_PROG_VERSION',
}

export enum MATCH_RULE {
  IN = 'IN',
  NOT_IN = 'NOT_IN',
  EQUAL = 'EQUAL',
  NOT_EQUAL = 'NOT_EQUAL',
  REGEX = 'REGEX',
}

export const MATCH_RULE_MAP = {
  [MATCH_RULE.IN]: '包含',
  [MATCH_RULE.NOT_IN]: '不包含',
  [MATCH_RULE.EQUAL]: '相等',
  [MATCH_RULE.NOT_EQUAL]: '不相等',
  [MATCH_RULE.REGEX]: '正则表达式',
};

export enum TAG_SOURCE_TYPE {
  SYSTEM = 'S',
  CUSTOM = 'U',
}

export const TAG_SOURCE_TYPE_MAP = {
  [TAG_SOURCE_TYPE.SYSTEM]: '系统标签',
  [TAG_SOURCE_TYPE.CUSTOM]: '自定义标签',
};

export const TSF_ROUTE_DEST_ELSE = 'TSF_ROUTE_DEST_ELSE';
