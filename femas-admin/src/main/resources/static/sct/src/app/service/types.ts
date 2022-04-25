import { OPERATOR, TAG_TYPE } from './detail/tagTable/types';

export enum ServiceStatus {
  UP = 'UP',
  INITIALIZING = 'INITIALIZING',
  CLOSING = 'CLOSING',
  DOWN = 'DOWN',
  OUT_OF_CONTACT = 'OUT_OF_CONTACT',
  UNKNOWN = 'UNKNOWN',
}

export interface ServiceItem {
  id: string;
  instanceNum: number;
  serviceName: string;
  status: ServiceStatus;
  versionNum: string;
  registryId: string;
  namespaceId: string;
  namespaceName: string;
  instancesCount: number;
  liveInstanceCount: number;
  versions: Array<string>;
}

export const ServiceStatusName = {
  [ServiceStatus.UP]: '在线',
  [ServiceStatus.DOWN]: '离线',
};

export const ServiceStatusTheme = {
  [ServiceStatus.UP]: 'success',
  [ServiceStatus.DOWN]: 'danger',
};

export interface InstanceItem {
  id: string;
  name: string;
  host: string;
  port: number;
  status: string;
  serviceName: string;
  namespaceId: string;
  lastUpdateTime: number;
  clientVersion: string;
  serviceVersion: string;
}

export interface Tag {
  tagType: TAG_TYPE;
  tagField: string;
  tagOperator: OPERATOR;
  tagValue: string;
}

export const SELECT_ALL = { text: '所有', value: '' };

export enum RULE_STATUS {
  CLOSE,
  OPEN,
}

export const RULE_STATUS_MAP = {
  [RULE_STATUS.CLOSE]: '已关闭',
  [RULE_STATUS.OPEN]: '已启用',
};
