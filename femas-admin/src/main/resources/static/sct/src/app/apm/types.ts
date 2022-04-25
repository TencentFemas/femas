import moment from 'moment';
import * as d3 from 'd3';
import { FilterTime } from '@src/common/types';

export const MidValue = 1000;

export const colorMapV1 = d3
  .scaleLinear()
  .domain([0, MidValue])
  .range(['#1ada17', 'yellow'] as any);

export const colorMapV2 = d3
  .scaleLinear()
  .domain([MidValue, 5000])
  .range(['yellow', '#da1717'] as any);

export enum CallTypeMap {
  http = 'HTTP',
  rpc = 'RPC',
  sql = 'SQL',
  nosql = 'NOSQL',
  mq = 'MQ',
  scheduled = 'SCHEDULED',
}

export const CallType = [
  CallTypeMap.http,
  CallTypeMap.rpc,
  CallTypeMap.sql,
  CallTypeMap.nosql,
  CallTypeMap.mq,
  CallTypeMap.scheduled,
];

export const CallTypeList = [
  { text: '全部', value: '' },
  ...CallType.map(value => ({
    value,
    text: value,
  })),
];

export const MQType = ['KAFKA', 'RabbitMQ', 'RocketMQ', 'ActiveMQ'];
export const SqlType = ['MYSQL'];
export const NosqlType = ['REDIS'];

export enum Period {
  oneMin = '60',
  fiveMin = '300',
  oneHour = '3600',
  oneDay = '86400',
}

export const PeriodName = {
  [Period.oneMin]: '1分钟',
  [Period.fiveMin]: '5分钟',
  [Period.oneHour]: '1小时',
  [Period.oneDay]: '1天',
};

export const FullTimeOption = [
  {
    value: Period.oneMin,
    text: '1分钟',
  },
  {
    value: Period.oneHour,
    text: '1小时',
  },
  {
    value: Period.oneDay,
    text: '1天',
  },
];

interface CommonResult {
  period: number; // 时间粒度
  startTime: number; //  查询开始时间，UTC时间，精确到毫秒
  endTime: number; // 查询截止时间，UTC时间，精确到毫秒
}

// 单值result
export interface SingleValueResult extends CommonResult {
  dataPoints: Array<SingleValueDataPoints>; // 数据点集合
}

interface SingleValueDataPoints {
  metricName: string;
  dimensions: Array<Dimensions>;
  points: Array<number>;
}

// 散点图result
export interface MultiMonitorResult extends CommonResult {
  dataPoints: Array<MultiDataPoint>;
}

export interface MultiDataPoint {
  metricName: string;
  dimensions: Array<Dimensions>;
  points: Array<MultiValue>;
  pointKeys: Array<string>; // 多值数据点key列表 => 我们需要的value
}

interface MultiValue {
  values: Array<number>;
}

// 直方图result对象
export interface MultiValueDataPoint {
  metricName: string;
  pointKeys: Array<string>; // 多值数据点key列表 => 我们需要的label
  point: Array<number>;
}

// 单值统计数据result 对象，如：大盘数据
export interface Metric {
  metricName: string;
  point: number;
}

// 监控数据传参
export interface MonitorParams {
  metricNames?: Array<string>;
  startTime: string;
  endTime: string;
  timeSelectors?: Array<TimeSelect>;
  period?: number;
  dimensionSets: Array<DimensionSets>;
  nameMap?: any;
  searchType: string;
  // excludeType?: Array<Monitor>;
  // includeType?: Array<Monitor>;
  // 可用于指定响应耗时展示的metric
  durationMetricNames?: Array<string>;
}

interface TimeSelect {
  from: string;
  to: string;
}

export interface DimensionSets {
  dimensions: Array<Dimensions>;
}

export interface Dimensions {
  dimensionName: string;
  dimensionValue: string;
}

export interface MultiValueDimension {
  dimensionName: string;
  dimensionValues: Array<string>;
}

export interface MonitorData {
  name: string;
  value: Array<{ label: string; value: number }>;
  unit?: string;
}

export const getDateTimeSelectRange = () => ({
  min: moment().subtract(30, 'd'),
  max: moment(),
});

export enum Kind {
  client = 'CLIENT',
  server = 'SERVER',
}

export const KindName = {
  [Kind.client]: '客户端',
  [Kind.server]: '服务端',
};

// Tag定义文档：http://tapd.oa.com/MiddlewareProduct/markdown_wikis/show/#1210159661001668317
export enum TagMap {
  kind = '__kind', // 'CLIENT' or 'SERVER'
  type = '__type', // 调用类型
  instance = '__instance', // 实例信息
  localComponent = '__local.component', // 本地组件类型
  localService = '__local.service', // 本地服务名
  localOperation = '__local.operation', // 本地操作名
  localIp = '__local.ip', // 本地ip
  localPort = '__local.port', // 本地端口
  peerComponent = '__peer.component', // 远程组件类型
  peerService = '__peer.service', // 远程服务名
  peerOperation = '__peer.operation', // 远程操作名
  peerIp = '__peer.ip', // 远程ip
  peerPort = '__peer.port', // 远程端口
  status = '__status', // 状态
  errorKind = '__error.kind', // 错误类型
  errorObject = '__error.object', // 异常对象
  sample = '__sample', // 是否采样
  httpMethod = '__http.method', // http请求方法，','分割
  httpStatus = '__http.status_code', // http返回码
  httpUrl = '__http.url', // http请求url
  rpcStatus = '__rpc.status_code', // rpc状态码
  sqlInstance = '__sql.instance', //数据库实例名称
  sqlStatment = '__sql.statement', // 数据库访问语句
  nosqlInstance = '__nosql.instance', // 数据库实例名称
  nosqlStatment = '__nosql.statement', // 数据库访问语句
  mqDestination = '__mq.destination', // 消息投递或交换地址
}

export enum MetricsMap {
  requestCount = 'count_req', // 请求量
  maxDuration = 'max_duration', // 最长响应耗时
  errorRate = 'pct_error_req', // 异常率
  errorCount = 'count_error_req', // 异常数
  avgDuration = 'avg_duration', // 平均响应耗时
  serviceCount = 'count_service', // 总服务数
  entryReq = 'count_entry_req', // 入口请求数
  entryErrorReq = 'count_entry_error_req', // 入口请求失败数
  p50 = 'percentile_duration.0.5', // p50
  p75 = 'percentile_duration.0.75', // p75
  p90 = 'percentile_duration.0.9', // p90
  p99 = 'percentile_duration.0.99', // p99
  codeCount = 'map_count_http_status_code', // http状态分布
  code2xx = 'map_count_http_status_code.2xx', // 2xx
  code3xx = 'map_count_http_status_code.3xx', // 3xx
  code4xx = 'map_count_http_status_code.4xx', // 4xx
  code5xx = 'map_count_http_status_code.5xx', // 5xx
  rangeCount = 'range_count_duration', // 响应统计
  mapType = 'map_count_type', // 调用方式
  // 单调用方式
  mapTypeHttp = 'map_count_type.HTTP', // http
  mapTypeMq = 'map_count_type.MQ', // MQ
  mapTypeNosql = 'map_count_type.NOSQL', // NOSQL
  mapTypeRpc = 'map_count_type.RPC', // RPC
  mapTypeScheduled = 'map_count_type.SCHEDULED', // SCHEDULED
  mapTypeSql = 'map_count_type.SQL', // SQL
  mapTypeUnknown = 'map_count_type.UNKNOWN', // UNKNOWN
}

export const MetricsName = {
  [MetricsMap.requestCount]: '请求量',
  [MetricsMap.errorRate]: '错误率',
  [MetricsMap.errorCount]: '错误请求数',
  [MetricsMap.maxDuration]: '最长响应耗时',
  [MetricsMap.avgDuration]: '平均响应耗时',
};

export const MetricsUnit = {
  [MetricsMap.requestCount]: '次',
  [MetricsMap.errorRate]: '%',
  [MetricsMap.errorCount]: '次',
  [MetricsMap.maxDuration]: 'ms',
  [MetricsMap.avgDuration]: 'ms',
};

export const MonitorMetricsName = {
  [MetricsMap.requestCount]: '请求量',
  [MetricsMap.errorRate]: '错误率',
  [MetricsMap.avgDuration]: '平均值',
  [MetricsMap.errorCount]: '异常次数',
  [MetricsMap.p50]: 'p50',
  [MetricsMap.p75]: 'p75',
  [MetricsMap.p90]: 'p90',
  [MetricsMap.p99]: 'p99',
  [MetricsMap.code2xx]: '2xx',
  [MetricsMap.code3xx]: '3xx',
  [MetricsMap.code4xx]: '4xx',
  [MetricsMap.code5xx]: '5xx',
  [MetricsMap.rangeCount]: '响应统计',
  [MetricsMap.mapTypeHttp]: 'HTTP',
  [MetricsMap.mapTypeMq]: 'MQ',
  [MetricsMap.mapTypeNosql]: 'NOSQL',
  [MetricsMap.mapTypeRpc]: 'RPC',
  [MetricsMap.mapTypeScheduled]: 'SCHEDULED',
  [MetricsMap.mapTypeSql]: 'SQL',
  [MetricsMap.mapTypeUnknown]: 'UNKNOWN',
};

export const TAG_SUFFIX = 'tags.';

export const ServerDimension = [
  {
    dimensionName: TAG_SUFFIX + TagMap.kind,
    dimensionValue: Kind.server,
  },
];

export const ClientDimension = [
  {
    dimensionName: TAG_SUFFIX + TagMap.kind,
    dimensionValue: Kind.client,
  },
];

export interface Service {
  serviceName: string;
  component: string;
  groupKey?: string;
  value: string;
  text: string;
}

export enum SearchType {
  overview = 'overview', // 概览
  service = 'service', // 服务
  interface = 'interface', // 接口
  component = 'component', // 组件
  serviceInterface = 'service_interface', // 服务-接口
  instance = 'instance', // 实例
  serviceInstance = 'service_instance', // 服务-实例
  error = 'error', // 服务-异常
  serviceError = 'service_error', // 服务-异常
  interfaceError = 'interface_error', // 接口-异常
  instanceError = 'instance_error', // 实例-异常
  sql = 'sql', // SQL
  serviceSql = 'service_sql', // 服务-SQL
  interfaceSql = 'interface_sql', // 接口-SQL
  nosql = 'nosql', // NOSQL
  serviceNosql = 'service_nosql', // 服务-NOSQL
  interfaceNosql = 'interface_nosql', // 接口-NOSQL
  peerInterface = 'peer_service_interface', // 接口上游
  consumer = 'CONSUMER', // 消费者
  producer = 'PRODUCER', // 生产者
  peerService = 'peer_service', // 概览-监控-服务
}

export enum GroupKey {
  service = 'SERVICE',
  component = 'COMPONENT',
  other = 'OTHER',
}

export const SortGroup = ['', GroupKey.service, GroupKey.component] as Array<string>;

export const GroupKeyMap = {
  [GroupKey.service]: '服务',
  [GroupKey.component]: '组件',
  [GroupKey.other]: '其他',
};

export interface ComposedId {
  serviceName: string;
  filterTime?: FilterTime;
  path?: string;
  instanceName?: string;
  period?: string;
}

export interface MultiCompare {
  monitorMap: string;
  nameMap?: any;
  metricNameList?: Array<MetricsMap>;
}

export enum MonitorType {
  monitor = 'monitor',
  statictic = 'statistic',
}

export const MonitorTypeName = {
  [MonitorType.monitor]: '监控',
  [MonitorType.statictic]: '统计',
};
