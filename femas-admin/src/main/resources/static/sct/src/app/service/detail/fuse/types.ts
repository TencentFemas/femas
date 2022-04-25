export interface FuseRuleItem {
  id: string;
  registryId: string;
  isolationLevel: ISOLATION_TYPE;
  namespaceId: string;
  serviceName: string;
  targetNamespaceId: string;
  targetServiceName: string;
  ruleId: string;
  ruleName: string;
  strategy: Array<Strategy>;
  isEnable: string;
  updateTime: number;
}

export interface Strategy {
  api: Array<ApiItem>;
  failureRateThreshold: number;
  maxEjectionPercent: number;
  minimumNumberOfCalls: number;
  slidingWindowSize: number;
  slowCallDurationThreshold: number;
  slowCallRateThreshold: number;
  waitDurationInOpenState: number;
}

export interface ApiItem {
  path: string;
  method: string;
  serviceVersion: string;
  status: string;
  id: string;
}

export enum EnableStatus {
  Enable = '1',
  Disable = '0',
}

export interface ApiList {
  path: string;
  method: string;
  value: string;
}

export const targetServiceKey = 'targetServiceName';
export const levelKey = 'isolationLevel';
export const triggerConditionKey = 'triggerConditionKey';
export const triggerStrategyKey = 'strategyList';
export const minReqKey = 'minimumNumberOfCalls';
export const scrollTimeKey = 'slidingWindowSize';
export const halfOpenKey = 'waitDurationInOpenState';
export const maxRatioKey = 'maxEjectionPercent';
export const apiKey = 'apiList';
export const triggerFailRateKey = 'failureRateThreshold';
export const triggerSlowTimeKey = 'slowCallDurationThreshold';
export const triggerSlowRateKey = 'slowCallRateThreshold';

export enum ISOLATION_TYPE {
  SERVICE = 'SERVICE',
  INSTANCE = 'INSTANCE',
  API = 'API',
}

export const ISOLATION_MAP = {
  [ISOLATION_TYPE.SERVICE]: '服务',
  [ISOLATION_TYPE.INSTANCE]: '实例',
  [ISOLATION_TYPE.API]: 'API',
};

export const ISOLATION_TYPE_LIST = [
  {
    text: ISOLATION_MAP[ISOLATION_TYPE.SERVICE],
    value: ISOLATION_TYPE.SERVICE,
  },
  {
    text: ISOLATION_MAP[ISOLATION_TYPE.INSTANCE],
    value: ISOLATION_TYPE.INSTANCE,
  },
  {
    text: ISOLATION_MAP[ISOLATION_TYPE.API],
    value: ISOLATION_TYPE.API,
  },
];

export enum TRIGGER_TYPE {
  SLOW = '0',
  FAILURE = '1',
}

export const TRIGGER_MAP = {
  [TRIGGER_TYPE.SLOW]: '慢请求率',
  [TRIGGER_TYPE.FAILURE]: '失败请求率',
};

export interface TriggerDefaultProps {
  type: TRIGGER_TYPE;
  [triggerFailRateKey]?: number;
  [triggerSlowTimeKey]?: number;
  [triggerSlowRateKey]?: number;
}

export const TRIGGER_DEFAULT_SLOW: TriggerDefaultProps = {
  type: TRIGGER_TYPE.SLOW,
  [triggerSlowTimeKey]: 60000,
  [triggerSlowRateKey]: 50,
};

export const TRIGGER_DEFAULT_FAILURE: TriggerDefaultProps = {
  type: TRIGGER_TYPE.FAILURE,
  [triggerFailRateKey]: 50,
};

export const TRIGGER_STRATEGY_DEFAULT = {
  [apiKey]: [],
  [minReqKey]: 10,
  [scrollTimeKey]: 10,
  [halfOpenKey]: 60,
  [maxRatioKey]: 50,
  [triggerConditionKey]: [TRIGGER_DEFAULT_FAILURE, TRIGGER_DEFAULT_SLOW],
};
