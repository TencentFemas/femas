import { Tag } from '../../types';

export interface LimitRuleItem {
  ruleName: string; //限流规则名,
  ruleId: string;
  id: string; //限流规则Id,
  serviceName: string;
  namespaceId: string;
  registryId: string;
  duration: number; //限流单位时间
  totalQuota: number; //限流请求数
  status: number;
  type: string;
  dimensionsDesc: JSX.Element[];
  tags: Array<Tag>;
  updateTime: number;
  desc: string;
  hasGlobalRule?: boolean;
}

/**
 * 请求来源类型声明
 *
 * @export
 * @enum {number}
 */
export enum SOURCE {
  // 全局限流
  ALL = 'GLOBAL',
  // 基于标签限流
  PART = 'PART',
}

export const SOURCE_MAP = {
  [SOURCE.ALL]: '全局限流',
  [SOURCE.PART]: '局部限流',
};

export enum LIMIT_RANGE {
  CLUSTER = 'CLUSTER',
  SINGLE = 'SINGLE',
}

export const LIMIT_RANGE_MAP = {
  [LIMIT_RANGE.CLUSTER]: '集群限流',
  [LIMIT_RANGE.SINGLE]: '单机限流',
};

/**
 * 表单键名声明
 *
 */
export const nameKey = 'ruleName';
export const sourceKey = 'type';
export const rangeKey = 'range';
export const descriptionKey = 'desc';
export const statusKey = 'status';
export const durationQuotaKey = 'totalQuota';
export const durationSecondKey = 'duration';
export const rulesKey = 'tags';
