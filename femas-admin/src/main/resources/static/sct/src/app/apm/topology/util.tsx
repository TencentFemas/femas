// @ts-nocheck
import * as d3 from 'd3';
import { svgToPng } from '@src/common/qcm-chart/core/toolbar/icon';
import { nodeHeight, nodeWidth } from '@src/common/qcm-chart/core/lib/helper';
import { LayoutType } from '@src/common/qcm-chart';
import { labelColors, lineColors } from '@src/common/qcm-chart/theme/theme';

export const formatPerMin = v => {
  if (!v) {
    return '-';
  }
  switch (true) {
    case v > 1000 * 100:
      return Math.round(v / 1000) + 'k';
    case v > 1000:
      return Math.round(v);
    default:
      return v;
  }
};

/**
 * 处理数字
 * @param value
 */
export function formatLargeNumber(value) {
  const unitArr = [
    { multiplier: 1, unit: '' },
    { multiplier: 1e4, unit: '万' },
    { multiplier: 1e6, unit: '百万' },
    { multiplier: 1e7, unit: '千万' },
    { multiplier: 1e8, unit: '亿' },
    { multiplier: 1e12, unit: '万亿' },
    { multiplier: 1e15, unit: '千万亿' },
  ];
  let matchUnit = unitArr[0];
  for (const unit of unitArr) {
    if (value < unit.multiplier) {
      break;
    } else if (value / unit.multiplier > 1) {
      matchUnit = unit;
    }
  }
  const v = +(value / matchUnit.multiplier).toFixed(2);

  if (typeof matchUnit.unit === 'function') {
    return matchUnit.unit(v);
  }
  return `${v}${matchUnit.unit}`;
}

export const formatTime = v => {
  if (v === undefined || v === null) {
    return '-';
  }
  v = Number(v);
  switch (true) {
    case v > 1000:
      return (v / 1000).toFixed(2) + 's';
    case v > 100:
      return Math.round(v) + 'ms';
    default:
      return v.toFixed(2) + 'ms';
  }
};

// 根据apdex获取连线及label颜色
export const getColors = (apdex, type = '') => {
  const origin = type === 'line' ? lineColors : labelColors;
  return apdex > 0.75 ? origin[0] : apdex > 0.25 ? origin[1] : origin[2];
};

// 拼接id
// type 默认为ms
export const generateNodeId = (namespaceId, name) => {
  return `${namespaceId}&${name}`;
};

// topo导出为图片
export const exportTopoToPng = layoutType => {
  const container = d3.select('div.tsf-topology');
  const svgSelection = container.select('svg.topo-svg');
  const svgGroup = svgSelection.select('g.svg-container');
  const groupWidth = nodeWidth(svgGroup.node());
  const groupHeight = nodeHeight(svgGroup.node());
  const diagramWidth = Math.max(svgSelection.node().clientWidth, groupWidth);
  const diagramHeight = Math.max(svgSelection.node().clientHeight, groupHeight);
  const offsetX = (diagramWidth - groupWidth) / 2;
  const offsetY = (diagramHeight - groupHeight) / 2;
  const tempSvg = container
    .append('div')
    .attr('class', 'test-div')
    .style('display', 'none')
    .append('svg')
    .html(svgSelection.html())
    .attr('width', diagramWidth)
    .attr('height', diagramHeight);

  tempSvg
    .select('g.svg-container')
    .attr(
      'transform',
      `translate(${layoutType === LayoutType.force ? diagramWidth / 2 : Math.max(offsetX, 0)}, ${
        layoutType === LayoutType.force ? diagramHeight / 2 : Math.max(offsetY, 0)
      }) scale(${layoutType === LayoutType.force ? 0.8 : 1})`,
    );

  svgToPng(tempSvg, diagramWidth, diagramHeight, 'service_topological_graph');

  // 移除元素
  container.select('div.test-div').remove();
};
