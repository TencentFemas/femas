/* eslint-disable prettier/prettier */
import * as d3 from 'd3';
import {axisTheme} from '../../theme/theme';
import {
  AxisArrow,
  AxisArrowDefault,
  AxisDesc,
  AxisDescDefault,
  AxisLine,
  AxisLineDefault,
  AxisSplitLine,
  AxisSplitLineDefault,
} from './../../type/chart';
import {nodeHeight, nodeWidth} from '../lib/helper';

// 添加坐标轴样式
export const addAxisStyle = context => {
  const {diagramWidth} = context.state;
  const {hideX, hideY} = context.props;

  // 宽度太小时需要展示不同的axis style
  const fontSize = diagramWidth < axisTheme.diagramMinWidth ? axisTheme.miniFontSize : axisTheme.fontSize;

  if (!hideX) {
    addXAxisStyle(context, fontSize);
  }

  if (!hideY) {
    addYAxisStyle(context, fontSize);
  }
};

// 为坐标轴添加箭头
export const addArrow = context => {
  const {xGroup, yGroup, y2Group} = context.state;
  const {needDoubleYAxis, hideX, hideY} = context.props;

  const {xAxisNeedArrow, yAxisNeedArrow} = {
    ...AxisArrowDefault,
    ...context.props.axisArrow,
  } as AxisArrow;

  if (!(xAxisNeedArrow || yAxisNeedArrow)) return;
  // 绘制箭头
  !hideX && xAxisNeedArrow && generateArrow(context, 'x-arrow');
  if (!hideY) {
    yAxisNeedArrow && generateArrow(context, 'y-arrow', false);
    needDoubleYAxis && yAxisNeedArrow && generateArrow(context, 'y2-arrow', false, true);
  }

  // 添加箭头
  !hideX && xAxisNeedArrow && xGroup.select('path.domain').attr('marker-end', 'url(#x-arrow)');

  if (!yAxisNeedArrow || hideY) return;
  yGroup.select('path.domain').attr('marker-end', 'url(#y-arrow)');
  needDoubleYAxis && y2Group && y2Group.select('path.domain').attr('marker-end', 'url(#y2-arrow)');
};

// 为坐标轴添加splitLine
export const addSplitLine = context => {
  const {xGroup, yGroup, y2Group, diagramHeight, diagramWidth} = context.state;
  const {needDoubleYAxis, hideX, hideY} = context.props;
  const {xAxisNeedSplitLine, yAxisNeedSplitLine} = {
    ...AxisSplitLineDefault,
    ...context.props.axisSplitLine,
  } as AxisSplitLine;

  if (!(xAxisNeedSplitLine || yAxisNeedSplitLine)) return;

  !hideX &&
  xAxisNeedSplitLine &&
  xGroup
  .selectAll('g.tick')
  .append('line')
  .attr('class', 'split-line')
  .attr(
      'transform',
      `translate(0, -${needDoubleYAxis ? diagramHeight / 2 - context.padding : diagramHeight - context.padding * 2})`,
  )
  .attr('stroke', axisTheme.splitLineColor)
  .attr('stroke-width', axisTheme.splitLineWidth)
  .attr('y2', diagramHeight - context.padding * 2);

  if (!yAxisNeedSplitLine || hideY) return;
  yGroup
  .selectAll('g.tick')
  .append('line')
  .attr('class', 'split-line')
  .attr('stroke', axisTheme.splitLineColor)
  .attr('stroke-width', axisTheme.splitLineWidth)
  .attr('x2', diagramWidth - context.padding * 2);

  needDoubleYAxis &&
  y2Group &&
  y2Group
  .selectAll('g.tick')
  .append('line')
  .attr('class', 'split-line')
  .attr('stroke', axisTheme.splitLineColor)
  .attr('stroke-width', axisTheme.splitLineWidth)
  .attr('x2', diagramWidth - context.padding * 2);
};

// 为坐标轴添加描述
export const addDesc = context => {
  const {xGroup, yGroup, y2Group, diagramWidth, diagramHeight} = context.state;
  const {needDoubleYAxis} = context.props;
  const {titleOpt} = context;

  const {xAxisDesc, yAxisDesc, y2AxisDesc} = {
    ...AxisDescDefault,
    ...context.props.axisDesc,
  } as AxisDesc;
  if (!(xAxisDesc || yAxisDesc || y2AxisDesc)) return;

  // 添加文字描述
  xAxisDesc &&
  xGroup
  .append('text')
  .text(xAxisDesc)
  .attr('fill', axisTheme.descColor)
  .attr('font-size', `${axisTheme.descFontSize}px`)
  .attr('font-weight', axisTheme.descFontWeight)
  .attr('transform', function () {
    return `translate(${diagramWidth - context.padding * 2 - nodeWidth(d3.select(this).node())}, -10)`;
  });
  if (yAxisDesc) {
    const desc = yGroup
    .append('text')
    .text(yAxisDesc)
    .attr('fill', axisTheme.descColor)
    .attr('font-size', `${axisTheme.descFontSize}px`)
    .attr('font-weight', axisTheme.descFontWeight);

    desc.attr('transform', function () {
      return `translate(${nodeWidth(desc.node()) -
      context.padding}, -${nodeHeight(desc.node()) + (titleOpt.text ? -2 : 10)})`;
    });
  }

  needDoubleYAxis &&
  y2AxisDesc &&
  y2Group
  .append('text')
  .text(y2AxisDesc)
  .attr('fill', axisTheme.descColor)
  .attr('font-size', `${axisTheme.descFontSize}px`)
  .attr('font-weight', axisTheme.descFontWeight)
  .attr('transform', function () {
    const node = d3.select(this).node();
    return `translate(${nodeWidth(node) -
    context.padding}, ${diagramHeight / 2 - context.bottomPadding + nodeHeight(node) + 10})`;
  });
};

// 绘制箭头
const generateArrow = (context, id, isXAxis = true, isY2 = false) => {
  const {svgGroup} = context.state;
  const marker = svgGroup
  .append('defs')
  .append('marker')
  .attr('id', id)
  .attr('markerUnits', 'strokeWidth')
  .attr('markerWidth', '18')
  .attr('markerHeight', '18')
  .attr('viewBox', '0 0 12 12');
  if (isXAxis) {
    marker
    .attr('refX', '6')
    .attr('refY', '10')
    .append('path')
    .attr('d', 'M2,2 L10,6 L2,10 L6,6 L2,2')
    .attr('fill', axisTheme.arrowColor);
    return;
  }

  if (!isY2) {
    // 竖直向上
    marker
    .attr('refX', '2')
    .attr('refY', '6')
    .append('path')
    .attr('d', 'M2,10 L6,2 L10,10 L6,6 L2,10')
    .attr('fill', axisTheme.arrowColor);
    return;
  }

  // 竖直向下
  marker
  .attr('refX', '2')
  .attr('refY', '6')
  .append('path')
  .attr('d', 'M2,2 L6,10 L10,2 L6,6 L2,2')
  .attr('fill', axisTheme.arrowColor);
};

// 坐标轴是否展示line
export const toggleAxisLine = context => {
  const {xGroup, yGroup, y2Group} = context.state;
  const {needDoubleYAxis = true, hideX, hideY} = context.props;

  const {xAxisNeedLine, yAxisNeedLine} = {
    ...AxisLineDefault,
    ...context.props.axisLine,
  } as AxisLine;

  !hideX &&
  xGroup
  .raise()
  .select('path.domain')
  .attr('opacity', xAxisNeedLine ? 1 : 0);
  if (hideY) return;
  yGroup.select('path.domain').attr('opacity', yAxisNeedLine ? 1 : 0);
  needDoubleYAxis && y2Group && y2Group.select('path.domain').attr('opacity', yAxisNeedLine ? 1 : 0);
};

const addXAxisStyle = (context, fontSize) => {
  const {xGroup, diagramWidth} = context.state;
  const {needDoubleYAxis, axisTickFormatter} = context.props;

  const tickLength = diagramWidth < axisTheme.diagramMinWidth ? axisTheme.miniTickSize : axisTheme.normalTickSize;

  // 设置坐标轴文字样式
  const xTicks = xGroup.selectAll('g.tick');

  xTicks
  .select('text')
  .attr('fill', axisTheme.tickTextColor)
  .attr('font-size', `${fontSize}px`);

  if (context.handlerX) {
    const tickLen = xTicks.nodes().length;
    // 如果x轴tick文字太多
    if (tickLen > tickLength && tickLen <= 2 * tickLength) {
      xTicks.each(function (d, i) {
        d3.select(this)
        .select('text')
        .attr('opacity', i % 2 === 0 ? 0 : 1);
      });
    } else if (tickLen >= 2 * tickLength) {
      xTicks.each(function (d, i) {
        d3.select(this)
        .select('text')
        .attr('opacity', i % 4 === 0 ? 1 : 0);
      });
    } else {
      xTicks.select('text').attr('opacity', 1);
    }
  }

  // 坐标line样式
  xGroup.selectAll('path').attr('stroke', axisTheme.lineColor);

  // 设置tick line样式
  xGroup
  .selectAll('g.tick')
  .selectAll('line')
  .attr('stroke', axisTheme.lineColor);

  axisTickFormatter?.xAxisTickFormatter &&
  xGroup
  .selectAll('g.tick')
  .select('text')
  .text(d => axisTickFormatter?.xAxisTickFormatter(d));

  // 双y轴样式
  if (!needDoubleYAxis) return;

  // 设定x轴 tick样式细节，处理x轴文字与y2轴tick文字重叠问题
  xGroup
  .selectAll('g.tick')
  .select('text')
  .attr('dy', '0.4em')
  .attr('font-size', axisTheme.miniFontSize);
};

const addYAxisStyle = (context, fontSize) => {
  const {yGroup, y2Group} = context.state;
  const {needDoubleYAxis, axisTickFormatter} = context.props;

  axisTickFormatter?.yAxisTickFormatter &&
  yGroup
  .selectAll('g.tick')
  .select('text')
  .text(d => axisTickFormatter?.yAxisTickFormatter(d));

  yGroup
  .selectAll('g.tick')
  .select('text')
  .attr('x', 0)
  .attr('fill', axisTheme.tickTextColor)
  .attr('font-size', `${fontSize}px`)
  .attr('transform', function () {
    return `translate(${nodeWidth(d3.select(this).node()) + 2}, ${fontSize})`;
  })
  .attr('opacity', (d, i) => (i === 0 ? 0 : 1));

  yGroup.selectAll('path').attr('stroke', axisTheme.lineColor);

  yGroup
  .selectAll('g.tick')
  .selectAll('line')
  .attr('stroke', axisTheme.splitLineColor);
  // .attr('opacity', 0)

  // 双y轴样式
  if (!needDoubleYAxis || !y2Group) return;

  axisTickFormatter?.y2AxisTickFormatter &&
  y2Group
  .selectAll('g.tick')
  .select('text')
  .text(d => axisTickFormatter?.y2AxisTickFormatter(d));

  y2Group
  .selectAll('g.tick')
  .select('text')
  .attr('x', 0)
  .attr('fill', axisTheme.tickTextColor)
  .attr('font-size', `${fontSize}px`)
  .attr('transform', function () {
    return `translate(${nodeWidth(d3.select(this).node()) + 2}, ${fontSize})`;
  })
  .attr('opacity', (d, i) => (i === 0 ? 0 : 1));

  // 坐标line样式
  y2Group.selectAll('path').attr('stroke', axisTheme.lineColor);

  // 设置tick line样式
  y2Group
  .selectAll('g.tick')
  .selectAll('line')
  .attr('stroke', axisTheme.splitLineColor);
  // .attr('opacity', 0)
};
