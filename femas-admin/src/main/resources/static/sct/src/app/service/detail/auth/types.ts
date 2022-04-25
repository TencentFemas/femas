import { RULE_STATUS, Tag } from './../../types';

export interface AuthItem {
  id: string;
  isEnabled: RULE_STATUS;
  createTime: string;
  ruleId: string;
  ruleName: string;
  ruleType: string;
  serviceName: string;
  namespaceId: string;
  availableTime: string;
  tags: Array<Tag>;
  registryId: string;
  description: JSX.Element[];
  target: string;
}

export enum AUTH_TYPE {
  close = 'CLOSE',
  white = 'WHITE',
  black = 'BLACK',
}

export const AUTH_TYPE_MAP = {
  [AUTH_TYPE.close]: '不启用',
  [AUTH_TYPE.white]: '白名单',
  [AUTH_TYPE.black]: '黑名单',
};

export enum TARGET {
  all = 'ALL',
  part = 'PART',
}

export const TARGET_MAP = {
  [TARGET.all]: '所有接口',
  [TARGET.part]: '指定接口',
};
