export interface Instance {
  traceId: string;
  name: string;
  startTime: number;
  duration: number;
  spanId: string;
  refs: Array<Ref>;
  tags: Array<Tag>;
  logs: Array<Log>;
  baggages: Array<Tag>;
  level: number;
  levelId: string;
  durationRatio: number;
  beginRatio: number;
  childrenNum: number;
  startTimestamp: number;
  endTimestamp: number;
  isClient?: boolean;
  /**new */
  start: number;
  isError: boolean;
  endpointName: string;
  serviceCode: string;
  endpointNames: Array<string>;
  entryService: string;
  namespaceId: string;
  registryId: string;
  peer: string; // ip
  localIp: string;
}

export interface Ref {
  traceId: string;
  parentSpanId: string;
  parentSegmentId: string;
}

export interface Tag {
  key: string;
  value: string;
}

export interface Log extends Tag {
  time: number;
  timestamp: number;
  id: string;
  data: Array<Tag>;
  isClient: boolean;
  errorKind: string;
}

export interface TraceDetail {
  timestamp: string;
  duration: number;
  serviceNum: number;
  spanNum: number;
  level: number;
  traceList: Array<any>;
}

export const ANNOTATION_MAP = {
  cs: 'Client Send',
  sr: 'Server Receive',
  ss: 'Server Send',
  cr: 'Client Receive',
};

export const ANNOTATION_SORT = [ANNOTATION_MAP.cs, ANNOTATION_MAP.sr, ANNOTATION_MAP.ss, ANNOTATION_MAP.cr];

export interface Annotation {
  value: string;
  duration: number;
  timestamp: number;
}

export enum SPAN_DETAIL_TAB {
  BASE = 'base',
  TAG = 'tag',
  LOG = 'log',
  META = 'meta',
  ORIGIN = 'origin',
}

export const SPAN_DETAIL_TAB_MAP = {
  [SPAN_DETAIL_TAB.BASE]: '基本信息',
  [SPAN_DETAIL_TAB.TAG]: 'Tag',
  [SPAN_DETAIL_TAB.LOG]: '调用链日志',
  [SPAN_DETAIL_TAB.META]: '自定义Metadata',
  [SPAN_DETAIL_TAB.ORIGIN]: 'Opentracing Format',
};

export const TRACE_STATUS = ['成功', '失败'];
export const TRACE_STATUS_TEXT_THEME = ['success', 'danger'] as Array<any>;

export enum TAG_GROUP {
  system = 'atom_system_tag',
  custom = 'atom_custom_tag',
}

export const TAG_GROUP_NAME = {
  [TAG_GROUP.system]: '系统默认Tag',
  [TAG_GROUP.custom]: '业务Tag',
};

export enum SPAN_TYPE {
  client = 'Exit',
  server = 'Entry',
  local = 'Local',
}

export enum LAYER_TYPE {
  rpcframe = 'RPCFramework',
  http = 'Http',
}
