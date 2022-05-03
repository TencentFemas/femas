/* eslint-disable prettier/prettier */
/* eslint-disable @typescript-eslint/camelcase */
import {GraphConfig} from '../../theme/theme';
import {toolbarDefaultOptions} from '../../type/chart';
import workerContent from './convert.worker';

export function nodeWidth(node: SVGGraphicsElement) {
  return node?.getBBox()?.width || 0;
}

export function nodeHeight(node: SVGGraphicsElement) {
  return node?.getBBox()?.height || 0;
}

// 获取统计数据粒度
// 返回period，单位为秒
export function getStatisticPeriod(startTime, endTime) {
  const disTime = caculateTimeDiff(startTime, endTime);
  // 1天以内粒度1分钟；1天-3天粒度1小时；3天-7天粒度1天
  if (disTime <= 24 * 60 * 60) {
    return 60;
  }
  if (disTime <= 3 * 24 * 60 * 60) {
    return 3600;
  }
  return 24 * 3600;
}

//计算时间相差的秒数
export function caculateTimeDiff(startTime, endTime) {
  return (+new Date(endTime) - +new Date(startTime)) / 1000;
}

// 根据时间获取compare型label
export function getCompareAxisLabel(startTime, time, period) {
  return (time - startTime) / 1000 / period;
}

export function calTooltipPre(width, height, mouse_x, mouse_y, tooltipWidth, tooltipHeight) {
  let y = 1;
  let x = 1;

  if (mouse_y > height - tooltipHeight - GraphConfig.PADDING) {
    y = -1;
  }

  if (mouse_x > width - tooltipWidth) {
    x = -1;
  }

  return {
    y,
    x,
  };
}

// 计算tooltip位置
export function calTooltipPos(
    tooltipContainer,
    diagramWidth,
    diagramHeight,
    parentId,
    mouse_x,
    mouse_y,
    toolTipClass = 'tooltip',
    needCompare = true,
) {
  tooltipContainer.style('display', 'block').style('clear', 'both');

  const tooltipContent: any = document.querySelector(`#${parentId} .${toolTipClass} .tea-chart-tooltip`);
  if (!tooltipContent) return;
  const tooltipWidth = tooltipContent?.clientWidth;
  const tooltipHeight = Math.max(tooltipContent?.clientHeight || 0, 80);
  const {y, x} = calTooltipPre(diagramWidth, diagramHeight, mouse_x, mouse_y, tooltipWidth, tooltipHeight);

  tooltipContainer
  .select('.tea-chart-tooltip')
  .style('left', `${mouse_x - (needCompare ? (x < 0 ? tooltipWidth : 0) : 0)}px`)
  .style('top', `${mouse_y + (needCompare ? (y < 0 ? y * tooltipHeight : tooltipHeight / 2 + 10) : 0)}px`);
}

export const judgeTopCondition = context => {
  let {toolBar} = context.props;
  const {titleOpt} = context;
  toolBar = {...toolbarDefaultOptions, ...toolBar};
  return toolBar.show || titleOpt.text;
};

export const generateParentId = key => {
  let d = new Date().getTime();
  if (window.performance && typeof window.performance.now === 'function') {
    d += performance.now(); //use high-precision timer if available
  }
  const uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = (d + Math.random() * 16) % 16 | 0;
    d = Math.floor(d / 16);
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });
  return key + uuid;
};

export const convertDate = date => {
  const dateString = date.replace(/\-/g, '/');
  return new Date(dateString);
};

export const convertTranslate = t => {
  if (!t) return [];
  return t
  .substring(t.indexOf('(') + 1, t.indexOf(')'))
  .split(',')
  .map(v => +v);
};

export function calIconTooltipPos(tooltipContainer, mouse_x, mouse_y) {
  tooltipContainer.style('display', 'block').style('clear', 'both');

  tooltipContainer
  .select('.tea-chart-tooltip')
  .style('left', `${mouse_x}px`)
  .style('top', `${mouse_y}px`);
}

export function hexToRGB(hex: string, alpha = 1) {
  const colorString = hex.startsWith('#') ? hex.slice(1) : hex;
  const red = colorString.slice(0, 2);
  const green = colorString.slice(2, 4);
  const blue = colorString.slice(4, 6);
  return `rgba(${parseInt(`0x${red}`, 16)}, ${parseInt(`0x${green}`, 16)}, ${parseInt(`0x${blue}`, 16)}, ${alpha})`;
}

export function startWorker(data) {
  const worker = new Worker(workerContent);
  return new Promise(res => {
    worker.postMessage(JSON.stringify(data));
    worker.onmessage = function (e) {
      worker.terminate();
      res(e.data);
    };
  });
}
