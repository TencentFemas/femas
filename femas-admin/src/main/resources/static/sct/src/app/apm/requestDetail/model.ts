/* eslint-disable prettier/prettier */
import { LAYER_TYPE, SPAN_TYPE, Tag } from './types';
import formatDate from '@src/common/util/formatDate';
import { traceRequest } from '@src/app/trace/model';

const handleExistSpan = (span, existSpan) => {
  // spanId时，取server的name
  existSpan.name = span.isClient ? existSpan.name : span.name;

  // 保存合并后被覆盖的spanId，用于保存被覆盖span相关联的ref
  existSpan.coveredSpanId = span.spanId;
  existSpan.coveredSegmentId = span.segmentId;

  // PS：span和exitSpan的isClient关系
  if (span.isClient) {
    existSpan.client = span;
  } else {
    existSpan.server = span;
  }
};

/**
 * 拉取所有spans
 */
export async function fetchTraceDetail(params: Params) {
  try {
    let { spans: list } = (await traceRequest({
      action: 'describeTrace',
      data: params,
    })) || { spans: [] };

    const totalLevel = { max: 1 };
    const serviceNames = [];
    const spanLevelMap = {};
    const rootList = [];

    list = list.sort(function (a, b) {
      return +a.startTime - +b.startTime;
    });

    // 计算 起始时间和耗时
    let minTime = Infinity;
    let maxTime = 0;

    for (const span of list) {
      if (span.startTime && span.startTime < minTime) minTime = span.startTime;
      if (span.startTime + span.duration > maxTime) maxTime = span.startTime + span.duration;
    }

    const startTime = minTime ? formatDate(+minTime) : 0;
    const duration = parseFloat((maxTime - minTime).toFixed(2));

    list.forEach(d => {
      d.isClient = d.type === SPAN_TYPE.client;
      d.durationRatio = +(d.duration / duration).toFixed(2);
      //计算时间抽开始百分比
      d.beginRatio = +((d.startTime - minTime) / duration).toFixed(2);
      // 处理spanId+segmentId相同的情况
      const existSpan = spanLevelMap[`${d.spanId}-${d.segmentId}`];
      if (existSpan) {
        handleExistSpan(d, existSpan);
        spanLevelMap[`${d.spanId}-${d.segmentId}`] = existSpan;
      } else {
        spanLevelMap[`${d.spanId}-${d.segmentId}`] = d;
      }
    });

    let rootCount = 1;

    // 生成树形，添加children内容
    for (const key in spanLevelMap) {
      const span = spanLevelMap[key];
      const parentId = span?.refs?.[0]?.parentSpanId;
      const parentSegmentId = span?.refs?.[0]?.parentSegmentId;
      serviceNames.push(span.serviceCode);
      const parentSpan = spanLevelMap[`${parentId}-${parentSegmentId}`];

      if (parentSegmentId && parentSpan) {
        if (!parentSpan.children) {
          parentSpan.children = [];
        }
        // sw协议，客户端和服务端是父子关系，当前的span要和parentSpan做对比
        // parantSpanId以及parentSegmentId相同&type不同，且client端layer为RPCFramework或http，合并span
        let existSpan = null;
        const parentIsClient = parentSpan.type === SPAN_TYPE.client;
        if (
          parentIsClient !== span.isClient &&
          parentIsClient &&
          [LAYER_TYPE.http, LAYER_TYPE.rpcframe].indexOf(parentSpan.layer) > -1
        ) {
          existSpan = parentSpan;
        }
        if (existSpan) {
          handleExistSpan(span, existSpan);
        } else {
          parentSpan.children.push(span);
        }
      } else {
        span.level = 0;
        span.levelId = '' + rootCount++;
        rootList.push(span);
      }
    }

    // 处理children
    rootList.forEach(r => {
      handleChildren(r, spanLevelMap, totalLevel);
    });

    const serviceNum = Array.from(new Set(serviceNames.filter(v => v))).length;
    const spanNum = list.length;

    return {
      startTime,
      duration,
      serviceNum,
      spanNum,
      depth: totalLevel.max,
      list: rootList,
    };
  } catch (error) {
    throw error;
  }
}

// children合并，处理refs缺失问题, 生成level等信息
const handleChildren = (data, spanLevelMap, totalLevel) => {
  if (!data?.children?.length) return;
  data.children.forEach((c, i) => {
    // spanId可能是数字0
    if (c.coveredSegmentId) {
      if (!c.children?.length) {
        c.children = [];
      }
      if (!c.refs?.length) {
        c.refs = [];
      }
      c.children.push(...(spanLevelMap[`${c.coveredSpanId}-${c.coveredSegmentId}`]?.children || []));
      c.refs.push(...(spanLevelMap[`${c.coveredSpanId}-${c.coveredSegmentId}`]?.refs || []));
    }
    c.level = data.level + 1;
    totalLevel.max = Math.max(c.level + 1, totalLevel.max);
    c.levelId = data.levelId + '.' + (i + 1);
    handleChildren(c, spanLevelMap, totalLevel);
  });
};

interface Params {
  traceId?: string;
  spanId?: string;
  tags?: Array<Tag>;
  pageNo?: number;
  pageSize?: number;
}
