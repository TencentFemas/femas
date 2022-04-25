import {
  colorMapV1,
  colorMapV2,
  FullTimeOption,
  GroupKey,
  MetricsMap,
  MidValue,
  MonitorMetricsName,
  MonitorParams,
  MultiMonitorResult,
  MultiValueDataPoint,
  Period,
  SingleValueResult,
  TAG_SUFFIX,
} from './types';
// import { Instance } from "./monitorGrid/constants";
import moment from 'moment';
import * as d3 from 'd3';
// import { Monitor } from "./requestOverview/constants";
import formatDate from '@src/common/util/formatDate';
import { caculateTimeDiff } from '@src/common/qcm-chart/core/lib/helper';

export const judgeValueIsEmpty = (data, compareValues = [null], key = 'value') => {
  return !data || data?.every(item => compareValues.indexOf(item[key]) > -1);
};

// 处理单值多维度单指标统计数据
export const convertMetricData = (data: SingleValueResult, NameMap = {}) => {
  if (!data) return [];
  const { startTime, period, dataPoints } = data;
  return (
    dataPoints
      ?.map(d => ({
        name: NameMap ? NameMap[d.metricName] : d.metricName,
        value: d.points?.map((v, i) => {
          return {
            label: formatDate(startTime + period * i * 1000),
            value: v === null ? null : d.metricName === MetricsMap.errorRate ? +(v * 100).toFixed(2) : +v.toFixed(2),
          };
        }),
      }))
      .filter(d => d.value.some(v => v.value !== null)) || []
  );
};

// 处理多值多维度单指标统计数据
// 调用分布 pointKey作为name
export const convertMultiPointKeyData = (data: MultiMonitorResult) => {
  if (!data) return [];
  const { startTime, period, dataPoints } = data;
  const result = [];
  dataPoints?.forEach(d => {
    const pointKeys = d.pointKeys;
    pointKeys?.forEach((key, keyIndex) => {
      const name = key;
      const value = [];
      d.points?.forEach((m, i) => {
        const label = formatDate(startTime + period * i * 1000);
        value.push({ label, value: m?.values?.[keyIndex] ?? null });
      });
      result.push({ name, value: judgeValueIsEmpty(value) ? [] : value });
    });
  });
  return result;
};

// 处理多值多维度单指标统计数据
// 散点图
export const convertMultiPointsData = (data: MultiMonitorResult) => {
  if (!data) return [];
  const { startTime, period, dataPoints } = data;
  return (
    dataPoints?.map(d => {
      const value = [];
      const pointKeys = d.pointKeys;
      let max = 0;
      d.points?.forEach((m, i) => {
        const label = formatDate(startTime + period * i * 1000);
        // [0]为了保证绘制values为null时的横轴时间轴
        (m.values || [null])?.forEach((v, index) => {
          max = Math.max(max, v);
          value.push({
            label,
            value: pointKeys?.[index] ?? null,
            count: v,
            tipName: `${pointKeys?.[index] || '-'}毫秒${
              pointKeys?.[index + 1] ? ` ~ ${pointKeys?.[index + 1]}毫秒` : '及以上'
            }`,
            hideValue: !v,
            max,
          });
        });
      });
      return {
        name: d.metricName,
        value: judgeValueIsEmpty(value, [null], 'count') ? [] : value,
      };
    }) || []
  );
};

// 处理多值统计指标数据
// 直方图
export const convertMultiStatisticData = (data: Array<MultiValueDataPoint>, unit = '') => {
  if (!data?.length) return [];
  return data?.map(d => {
    return {
      name: MonitorMetricsName[d.metricName],
      value:
        d.point?.map((value, i) => ({
          label: d.pointKeys?.[i] + unit,
          value,
          labelValue: +d.pointKeys?.[i],
          tipName: `${d.pointKeys?.[i]}${unit}${d.pointKeys?.[i + 1] ? ` ~ ${d.pointKeys?.[i + 1]}${unit}` : '及以上'}`,
        })) || [],
    };
  });
};

export const formateChartColor = value => {
  return (value > MidValue ? colorMapV2(value) : colorMapV1(value)) || '#2d70f6';
};

export const genScatterTip = (data, color) => {
  return `<div> 
  <div style="display:table;">
    <div style="padding: 1px 0;margin-bottom: 5px;">
      <h4 style="font-size:14px;color:#333;padding-bottom:10px">${data.label}</h4>
      <p style="height:20px;line-height:20px;display:table-row;display: inline-table;width: 100%;">
        <span style="display:table-cell;line-height:20px;vertical-align:middle;padding-right:10px;width: 18px;">
          <i style="display:inline-block;height:10px;width:10px;background:${color};border-radius:50%;vertical-align:middle;"></i>
        </span>
        <span style="display:table-cell;font-size:14px;color:#333;">${data.tipName || data.name}</span>
        <span style="display:table-cell;font-weight:600;color:#333;font-size:14px;padding-left:15px;text-align:right;">${
          data.count
        }</span>
      </p>
    </div>
  </div>
  <div style='border-top: 1.5px solid #E5E5E5; padding: 6px 0;'>
    <p style="height:17px;line-height:15px;margin-top: 6px;font-size:12px;color:#666;">框选图表可查看详情</p>
  </div>
</div>`;
};

// 根据时间返回统计粒度
export const calculatePeriod = (startTime, endTime) => {
  const disTime = caculateTimeDiff(startTime, endTime);
  if (disTime <= 6 * 60 * 60) {
    return +Period.oneMin;
  } else if (disTime <= 7 * 24 * 60 * 60) {
    return +Period.oneHour;
  }
  return +Period.oneDay;
};

export const findInfoByTag = (data, tagKey) => {
  if (!data || !tagKey) return;
  return data?.tags?.find(t => t.key === tagKey)?.value;
};

export const findInfoByDimensionName = (data, dimenKey) => {
  if (!data || !dimenKey) return;
  return data?.dimensions?.find(t => t.dimensionName === TAG_SUFFIX + dimenKey)?.dimensionValue;
};

// 处理列表metrics指标数据
// export const handlerMetric = (data: Instance) => {
//   const result = {};
//   if (!data?.metrics?.length) return result;
//   data?.metrics.forEach((m) => {
//     result[m.metricName] = +m.point;
//   });
//   return result;
// };

// 获取前*天时间
export const getBeforeTime = (time: string, day: number) => {
  return moment(time)
    .subtract(day, 'day')
    .toDate();
};

interface Params {
  startTime: string;
  endTime: string;
  period: string;
}

//计算图period
export const getActualPeriod = (params: Params | MonitorParams) => {
  const { startTime, endTime, period } = params;

  let curPeriod = +period;

  if (!curPeriod) {
    // 计算period
    curPeriod = +calculatePeriod(startTime, endTime);
  }
  return curPeriod;
};

export const getOpacityMap = max =>
  d3
    .scaleLinear()
    .domain([0, max])
    .range([0.1, 1]);

// 获取多时段对比指标等内容
// export const getMuitlCompareInfo = (
//   monitorMap: string,
//   durationMetricNames = []
// ) => {
//   const result = { monitorMap } as MultiCompare;

//   // http
//   if (monitorMap === Monitor.httpCode) {
//     result.metricNameList = [
//       MetricsMap.code2xx,
//       MetricsMap.code3xx,
//       MetricsMap.code4xx,
//       MetricsMap.code5xx,
//     ];
//   }
//   // 响应耗时
//   if (monitorMap === Monitor.duration) {
//     result.nameMap = MonitorMetricsName;
//     result.metricNameList = durationMetricNames?.length
//       ? durationMetricNames
//       : [
//           MetricsMap.avgDuration,
//           MetricsMap.p50,
//           MetricsMap.p75,
//           MetricsMap.p90,
//           MetricsMap.p99,
//         ];
//   }

//   if (monitorMap === Monitor.distribution) {
//     result.metricNameList = [
//       MetricsMap.mapTypeHttp,
//       MetricsMap.mapTypeMq,
//       MetricsMap.mapTypeNosql,
//       MetricsMap.mapTypeRpc,
//       MetricsMap.mapTypeScheduled,
//       MetricsMap.mapTypeSql,
//       MetricsMap.mapTypeUnknown,
//     ];
//   }

//   return result;
// };

export const getTimeOptions = (startTime, endTime) => {
  const disTime = caculateTimeDiff(startTime, endTime);
  let index = -1;
  if (disTime <= 6 * 60 * 60) {
    index = 0;
  } else if (disTime <= 7 * 24 * 60 * 60) {
    index = 1;
  } else {
    index = 2;
  }
  // 加入临界值3分钟、3小时及3天
  return disTime ? FullTimeOption.slice(index).filter(item => disTime >= 3 * +item.value) : [FullTimeOption[0]];
};

const SPLIT_STRING = '$$';

// 拼接拓扑图ID，拼接规则与后台保持一致
export const getTopologyNodeId = data => {
  return data ? `${data.serviceName}${SPLIT_STRING}${data.path}` : '';
};
// 分割拓扑图Name, [serviceName, path]
export const splitTopologyNodeName = name => {
  return name?.split(SPLIT_STRING) || [];
};

// 处理service 分组
export const handleServiceGroupKey = serviceList => {
  return serviceList.map(item => ({
    ...item,
    groupKey: item.value
      ? item.component === GroupKey.service
        ? GroupKey.service
        : item.component
        ? GroupKey.component
        : GroupKey.other
      : '',
  }));
};

export const removeLsItem = KEY => {
  for (const key in KEY) {
    localStorage.removeItem(KEY[key]);
  }
};

// service分组排序
export const handlerServiceSort = list => {
  if (!list?.length) return [];
  const result = [];
  result.push(...list.filter(item => item.component === GroupKey.service));
  result.push(...list.filter(item => item.component && item.component !== GroupKey.service));
  result.push(...list.filter(item => !item.component));
  return result;
};
