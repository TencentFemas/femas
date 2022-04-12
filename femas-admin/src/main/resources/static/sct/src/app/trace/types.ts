import { FilterTime } from '@src/common/types';
import { Tag } from '../apm/requestDetail/types';

export enum TAB {
  SEARCH = 'search',
  SPAN = 'span',
}

export const TRACE_LS_KEY = {
  startTime: 'atom-trace-startTime',
  endTime: 'atom-trace-endTime',
  service: 'atom-trace-service',
  traceId: 'atom-trace-traceId',
  callStatus: 'atom-trace-callStatus',
  api: 'atom-trace-calleeApi',
  callType: 'atom-trace-callType',
  httpCode: 'atom-trace-http',
  minDuration: 'atom-trace-minDuration',
  maxDuration: 'atom-trace-maxDuration',
  orderType: 'atom-trace-orderType',
};

export const HIGH_TRACE_LS = [TRACE_LS_KEY.api, TRACE_LS_KEY.callType, TRACE_LS_KEY.httpCode, TRACE_LS_KEY.orderType];

export const SPAN_LS_KEY = {
  startTime: 'atom-span-startTime',
  endTime: 'atom-span-endTime',
  client: 'atom-span-client',
  server: 'atom-span-server',
  resultStatus: 'atom-span-resultStatus',
  clientApi: 'atom-span-clientApi',
  serverApi: 'atom-span-serverApi',
  callType: 'atom-span-callType',
  httpCode: 'atom-span-http',
  minDuration: 'atom-span-minDuration',
  maxDuration: 'atom-span-maxDuration',
  kind: 'atom-span-kind',
  tags: 'atom-span-tags',
};

export const HIGH_SPAN_LS = [
  SPAN_LS_KEY.clientApi,
  SPAN_LS_KEY.serverApi,
  SPAN_LS_KEY.callType,
  SPAN_LS_KEY.httpCode,
  SPAN_LS_KEY.kind,
];

export interface SpanParams {
  startTime?: string;
  endTime?: string;
  filterTime?: FilterTime;
  tags?: Array<Tag>;
  entry?: boolean;
  traceId?: string;
  spanId?: string;
  durationMin?: number;
  durationMax?: number;
  includeService?: string;
  clientService: string;
  serverService: string;
  clientOperation: string;
  serverOperation: string;
  clientInstanceIp: string;
  serverInstanceIp: string;
  notNeedFilterTag?: boolean;
}

export interface TraceParams {
  startTimestamp?: number;
  endTimestamp?: number;
  tags?: Array<Tag>;
  traceId?: string;
  minTraceDuration?: number;
  maxTraceDuration?: number;
  serviceName?: string; //途径服务
  endpointName?: string; // 入口接口
  traceState?: 'ALL' | 'SUCCESS' | 'ERROR';
}
