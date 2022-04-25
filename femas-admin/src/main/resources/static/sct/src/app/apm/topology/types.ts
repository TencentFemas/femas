import { LayoutType } from '@src/common/qcm-chart';

// 用于获取BIG ICON的地址
const BIG_ICON_BASE_URL = window['FEMAS_BASE_PATH'] + '/icon/';

export enum BIG_ICON_NAME {
  CAFKA = 'cafka',
  KAFKA = 'kafka',
  ES = 'es',
  MYSQL = 'mysql',
  CMQ = 'cmq',
  API = 'api',
  MONGODB = 'mongodb',
  REDIS = 'redis',
  USER = 'user',
  MSGW = 'msgw',
  EXTERNAL = 'external',
  POST = 'postgresql',
  ROCKET = 'rocketmq',
  COMMON = 'common',
}

export const BIG_ICON_MAP = {
  [BIG_ICON_NAME.CAFKA]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.CAFKA}.svg`,
  [BIG_ICON_NAME.KAFKA]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.KAFKA}.svg`,
  [BIG_ICON_NAME.ES]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.ES}.svg`,
  [BIG_ICON_NAME.MYSQL]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.MYSQL}.svg`,
  [BIG_ICON_NAME.CMQ]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.CMQ}.svg`,
  [BIG_ICON_NAME.API]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.API}.svg`,
  [BIG_ICON_NAME.MONGODB]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.MONGODB}.svg`,
  // [BIG_ICON_NAME.TSF]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.TSF}.svg`,
  [BIG_ICON_NAME.REDIS]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.REDIS}.svg`,
  [BIG_ICON_NAME.USER]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.USER}.svg`,
  [BIG_ICON_NAME.MSGW]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.MSGW}.svg`,
  [BIG_ICON_NAME.POST]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.POST}.svg`,
  [BIG_ICON_NAME.ROCKET]: `${BIG_ICON_BASE_URL}${BIG_ICON_NAME.ROCKET}.svg`,
  [BIG_ICON_NAME.COMMON]: `${BIG_ICON_BASE_URL}common.svg`,
  // 添加external，在_convertNodeData及_initCircleData中排除
  [BIG_ICON_NAME.EXTERNAL]: 'external',
};

export const LayoutTypeText = {
  [LayoutType.force]: '力导图布局',
  [LayoutType.dagre]: '顺排布局',
};

export const LAYOUT_ICON_MAP = {
  [LayoutType.dagre]: `${BIG_ICON_BASE_URL}${LayoutType.dagre}.svg`,
  [LayoutType.force]: `${BIG_ICON_BASE_URL}${LayoutType.force}.svg`,
};

export interface Disable {
  disabled?: boolean;
  disabledPromt?: string;
}

export enum NodeCalMode {
  none = 'none',
  errorRate = 'errorRate',
  requestCount = 'requestVolume',
  avgDuration = 'averageDuration',
}

export const NodeCalModeName = {
  [NodeCalMode.none]: '无，所有节点大小一致',
  [NodeCalMode.errorRate]: '根据运行状况计算节点大小',
  [NodeCalMode.requestCount]: '根据请求量计算节点大小',
  [NodeCalMode.avgDuration]: '根据平均响应耗时计算节点大小',
};

export enum TraceShowMode {
  none = 'none',
  maxDuration = 'maxDuration',
  requestCount = 'requestCount',
  avgDuration = 'reqAvgDuration',
  errorCount = 'errorCount',
}

export const TraceShowModeName = {
  [TraceShowMode.avgDuration]: '展示平均耗时',
  [TraceShowMode.maxDuration]: '展示最大耗时',
  [TraceShowMode.requestCount]: '展示调用次数',
  [TraceShowMode.errorCount]: '展示错误次数',
  [TraceShowMode.none]: '不展示数据',
};

export const TraceShowModeUnit = {
  [TraceShowMode.avgDuration]: 'ms',
  [TraceShowMode.maxDuration]: 'ms',
  [TraceShowMode.requestCount]: '次',
  [TraceShowMode.errorCount]: '次',
  [TraceShowMode.none]: '',
};

export const SERVICE_TYPE = 'microservice';
