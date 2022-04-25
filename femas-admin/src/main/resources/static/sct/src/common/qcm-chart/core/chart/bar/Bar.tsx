/* eslint-disable prettier/prettier */
// @ts-nocheck
import * as React from 'react';
import * as d3 from 'd3';
import {BarTheme, legendTheme} from '../../../theme/theme';
import {generateBarTooltip, generateCustomTooltip} from '../../tooltip/Tooltip';
import {calTooltipPos} from '../../lib/helper';
import {
  axisBrushDefault,
  AxisProps,
  AxisSplitLineDefault,
  BaseProps,
  Data
} from '../../../type/chart';
import AxisBase from '../AxisBase';
import {addAxisStyle, addSplitLine} from '../../axis/axis';

export interface BarProps {
  // 多个柱状图时text会有显示问题
  /**
   * 是否需要展示每个柱状图相关值
   *
   *
   * @default false
   */
  needBarText?: boolean;

  /**
   * 间距
   *
   * @default 5
   */
  barPadding?: number;

  /**
   * baritem 内部间距
   *
   * @default 5
   */
  innerPadding?: number;

  /**
   * 返回baritem颜色
   */
  formateBarColor?: (data) => string;
}

export abstract class BarContent<T> extends AxisBase<BarProps & T> {
  get innerPadding() {
    return this.props.innerPadding ?? BarTheme.innerPadding;
  }

  convertData() {
    const {data} = this.props;
    this.setState({
      convertedData: data,
    });
  }

  convertStackData(data) {
    // 处理data，按 label进行分类
    const barData = [] as Array<Data>;
    data.forEach(d => {
      d.value.forEach(v => {
        const item = barData.find(d => d.name === v.label);
        if (item) {
          item.value.push({
            ...d,
            ...v,
            yIndex: d.yIndex,
            label: d.name,
            value: v.value,
            name: v.label,
          });
          return;
        }
        barData.push({
          name: v.label,
          value: [
            {
              ...d,
              ...v,
              label: d.name,
              value: v.value,
              yIndex: d.yIndex,
              name: v.label,
            },
          ],
        });
      });
    });
    return barData;
  }

  reload() {
    // reload时重新渲染
    const {barGroup, svgGroup} = this.state;
    if (barGroup !== null) {
      svgGroup.selectAll('g').remove();
      this.setState({
        barGroup: null,
      });
    }
    super.reload();
  }

  drawChart() {
    // 可支持展示多个pie
    const {svgGroup, barGroup, convertedData} = this.state;
    if (!convertedData || !convertedData.length) return;

    if (barGroup && barGroup !== null) {
      return;
    }
    this.drawAxis(true);
    const curBarGroup = svgGroup
    .append('g')
    .attr('class', 'd3-bar')
    .attr('transform', `translate(${this.padding}, ${this.topPadding})`);

    this.setState(
        {
          barGroup: curBarGroup,
          transformX: this.xScale,
          transformY: this.yScale,
          transformY2: this.y2Scale,
          brushSelection: [],
          showReset: false,
        },
        async () => {
          this.generateClip(curBarGroup);
          this.drawBrush();
          this.drawBars();
          this.drawHoverBar();
        },
    );
  }

  drawBars() {
    const {barGroup, diagramHeight, transformX, transformY, transformY2, parentId} = this.state;
    const {useableData: data, topPadding, innerPadding} = this;
    const {needBarText, barPadding = BarTheme.barPadding, needDoubleYAxis, formateBarColor} = this.props;

    const height = needDoubleYAxis ? diagramHeight / 2 - topPadding : this.height;

    barGroup.select('g.rects').remove();

    // 绘制柱形
    const rects = barGroup.append('g').attr('class', 'rects');

    const bandWidth = transformX.bandwidth() - innerPadding * 2 - barPadding * (data.length - 1);

    const width = bandWidth / data.length > BarTheme.maxWidth ? BarTheme.maxWidth : bandWidth / data.length;

    const rectItem = rects
    .selectAll('g.rects-item')
    .data(
        data.map(d => ({
          ...d,
          value: d.value.filter(v => this.dataInBrushSelection(v)),
        })),
    )
    .enter()
    .append('g')
    .attr('class', 'rects-item')
    .attr(
        'transform',
        d =>
            `translate(${innerPadding + (bandWidth - width * data.length) / 2}, ${
                this.dataInY2Axis(d.yIndex) ? height : 0
            })`,
    )
    .selectAll('g.rect-item')
    .data((d, i) =>
        d.value.map(v => ({
          yIndex: d.yIndex,
          ...v,
          name: d.name,
          index: i,
          width,
        })),
    )
    .enter()
    .append('g')
    .attr('class', 'rect-item');

    rectItem
    .append('rect')
    .attr('class', 'bar')
    .attr('x', d => transformX(d.label) + d.index * (d.width + barPadding))
    .attr('y', d => {
      const value = this.getDataOfYAxis(d);
      // 处理d.value < 0 的情况
      return this.dataInY2Axis(d.yIndex)
          ? d.value < 0
              ? value
              : transformY2(0)
          : d.value < 0
              ? transformY(0)
              : value;
    })
    .attr('height', d => {
      const value = this.getDataOfYAxis(d);
      // 处理d.value < 0 的情况
      return Math.max(
          this.dataInY2Axis(d.yIndex)
              ? d.value < 0
              ? transformY2(0) - value
              : value - transformY2(0)
              : d.value < 0
              ? value - transformY(0)
              : transformY(0) - value,
          1,
      );
    })
    .attr('width', d => d.width)
    .attr('fill', d => (formateBarColor ? formateBarColor(d) : this.colors[d.name]))
    .attr('opacity', 0.8)
    .attr('clip-path', d => (this.dataInY2Axis(d.yIndex) ? `url(#clip-${parentId})` : `url(#clip_y-${parentId})`)); //防止图形越界

    needBarText &&
    rectItem
    .append('text')
    .text(d => d.value)
    .attr(
        'transform',
        d =>
            `translate(${transformX(d.label) + d.index * d.width + d.width / 2}, ${this.getDataOfYAxis(d) +
            (this.dataInY2Axis(d.yIndex) ? 2 : -1) * 10})`,
    )
    .attr('font-size', '12px')
    .attr('text-anchor', 'middle')
    .attr('fill', d => (formateBarColor ? formateBarColor(d) : this.colors[d.name]));
  }

  drawHoverBar() {
    const {barGroup, parentId, transformX, brushSelection, diagramWidth} = this.state;
    const {height, useableData, padding, topPadding, formateTime} = this;
    const {isTimeAxis = true} = this.props;
    const {show: needTooltip} = this.tooltip;

    const data = this.convertStackData(useableData)
    .filter(v => this.dataInBrushSelection(v, brushSelection, 'name'))
    .map(d => ({
      ...d,
      convertedName: isTimeAxis ? formateTime(d.name) : d.name,
    }));

    this.overlayRect
    .attr('transform', `translate(${padding},${topPadding})`)
    .attr('height', height)
    .attr('width', diagramWidth - padding * 2)
    .on('mousemove', function () {
      // eslint-disable-next-line @typescript-eslint/camelcase
      const [mouse_x, mouse_y] = d3.mouse(this);

      const index = _this.closest(
          mouse_x,
          data.map(d => transformX(d.name) + transformX.bandwidth() / 2),
      );

      if (index < 0) return;

      barGroup.select('g.rect-hover').remove();

      barGroup
      .append('g')
      .attr('class', 'rect-hover')
      .lower()

      .selectAll('rect.hover')
      .data([{...data[index]}])
      .enter()
      .append('rect')
      .attr('class', 'hover')
      .attr('width', transformX.bandwidth())
      .attr('height', height)
      .attr('fill', BarTheme.hoverRectColor)
      .attr('x', d => transformX(d.name))
      .attr('opacity', 0.1);

      if (!needTooltip) return;
      // 处理Tooltip数据
      _this.tooltipCallback(data[index], mouse_x, mouse_y);
    })
    .on('mouseout', function () {
      barGroup.select('g.rect-hover').remove();
      if (!needTooltip) return;
      const tooltip = document.querySelector(`#${parentId} .tooltip`);
      needTooltip && tooltip && tooltip.setAttribute('style', 'display: none');
    });
  }

  // eslint-disable-next-line @typescript-eslint/camelcase
  tooltipCallback(hoverData, mouse_x, mouse_y) {
    const {diagramHeight, diagramWidth, parentId} = this.state;
    const {formatter, additionalTip, showSplitLine} = this.tooltip;
    const {formateBarColor} = this.props;

    const tooltipContainer = d3
    .select(`#${parentId}`)
    .select('section.chart')
    .selectAll('div.tooltip')
    .data([null])
    .join('div')
    .attr('class', 'tooltip');

    const tooltip = document.querySelector(`#${parentId} .tooltip`);

    // 处理 Tooltip value 为空时的情况
    const filterValue = hoverData.value.filter(v => v.value !== null);
    if (!filterValue.length) {
      tooltip && tooltip.setAttribute('style', 'display: none');
      return;
    }
    hoverData.value = [...filterValue];

    tooltip.innerHTML = formatter
        ? generateCustomTooltip(formatter, hoverData, this.colors)
        : generateBarTooltip(hoverData, this.colors, additionalTip, formateBarColor, showSplitLine);

    calTooltipPos(tooltipContainer, diagramWidth, diagramHeight, parentId, mouse_x, mouse_y);
  }

  // hover legend
  onLegendItemHover({name}) {
    const {barGroup} = this.state;
    const {colors} = this;
    const {needBarText} = this.props;

    barGroup
    .select('g.rects')
    .selectAll('g.rects-item')
    .call(g => {
      g.each(function highlight({name: gName}) {
        const ele = d3.select(this).selectAll('g.rect-item');
        if (gName === name) {
          ele.select('rect').attr('fill', colors[name]);

          needBarText && ele.select('text').attr('fill', colors[name]);
        } else {
          ele.select('rect').attr('fill', legendTheme.disabledColor);

          needBarText && ele.select('text').attr('fill', legendTheme.disabledColor);
        }
      });
    });
  }

  // 移出
  onLegendItemLeave() {
    const {barGroup} = this.state;
    const {needBarText, formateBarColor} = this.props;

    // 还原
    const item = barGroup
    .select('g.rects')
    .selectAll('g.rects-item')
    .selectAll('g.rect-item');

    item.select('rect').attr('fill', d => (formateBarColor ? formateBarColor(d) : this.colors[d.name]));

    needBarText && item.select('text').attr('fill', d => (formateBarColor ? formateBarColor(d) : this.colors[d.name]));
  }

  // legend click
  onLegendItemClick(data) {
    super.onLegendItemClick(data);
    // 重新绘制
    this.handlerZoom();
  }

  getXScale() {
    return d3.scaleBand().domain(this.getAxisLabels());
  }

  brushFinished(selection, yValues, ySelections) {
    const {transformX, convertedData, transformY, transformY2} = this.state;
    const {yAxisNeedBrushZoom, brushCallback, onlyBrush} = {
      ...axisBrushDefault,
      ...this.props.axisBrushZoom,
    };
    const values = [];
    convertedData.forEach(data => {
      data.value.forEach(v => {
        const start = transformX(v.label);
        const end = start + transformX.bandwidth();
        // 判断与选区的关系
        if (
            selection.length &&
            ((start >= selection[0] && start <= selection[1]) ||
                (end >= selection[0] && end <= selection[1]) ||
                (start <= selection[0] && end >= selection[1]))
        ) {
          values.push(v.label);
        }
      });
    });
    if (!values.length) return false;
    const data = Array.from(new Set(values));

    this.brushTooltipCallback(data, yValues, selection, ySelections);
    brushCallback && brushCallback(data, yValues);

    if (onlyBrush) return false;

    const d = convertedData.map(d => ({
      ...d,
      value: d.value.filter(v => this.dataInBrushSelection(v, data)),
    }));

    if (yAxisNeedBrushZoom) {
      const maxY = this.getYMax(d);
      const maxY2 = this.getYMax(d, true);
      const minY = this.getYMin(d);
      const minY2 = this.getYMin(d, true);
      this.setState({
        transformY: transformY.domain([minY, maxY]),
        transformY2: transformY2.domain([minY2, maxY2]),
      });
    }

    this.setState({
      transformX: transformX.domain(this.getAxisLabels(d)),
      brushSelection: data,
      showReset: data.length !== this.getAxisLabels().length,
    });

    this.handlerZoom();
    return data.length !== this.getAxisLabels().length;
  }

  // 判断数据是否在选区内
  dataInBrushSelection(d, brushSelection = this.state.brushSelection, key = 'label') {
    const {showReset} = this.state;

    if (!d) return false;

    if (!brushSelection.length) {
      return !showReset;
    }

    // 对比节点的坐标位置
    if (brushSelection.indexOf(d[key]) < 0) {
      return false;
    }
    return true;
  }

  handlerZoom() {
    const {xGroup, yGroup, y2Group, svgGroup, transformX, transformY, transformY2} = this.state;
    const {needDoubleYAxis} = this.props;
    const {xAxis, yAxis, y2Axis} = this;

    const {show, yAxisNeedBrushZoom} = {
      ...axisBrushDefault,
      ...this.props.axisBrushZoom,
    };

    const {xAxisNeedSplitLine, yAxisNeedSplitLine} = {
      ...AxisSplitLineDefault,
      ...this.props.axisSplitLine,
    };

    //缩放&移动坐标轴
    show && xGroup.call(xAxis.scale(transformX));
    yAxisNeedBrushZoom && yGroup.call(yAxis.scale(transformY));
    needDoubleYAxis && yAxisNeedBrushZoom && y2Group.call(y2Axis.scale(transformY2));

    // 重新添加坐标轴样式
    addAxisStyle(this);

    if (xAxisNeedSplitLine || yAxisNeedSplitLine) {
      // 重新处理坐标轴分割线
      svgGroup.selectAll('line.split-line').remove();
      addSplitLine(this);
    }

    this.drawBars();
    this.drawHoverBar();
  }
}

export class Bar extends React.Component<BaseProps & AxisProps & BarProps> {
  render() {
    const {data, ...rest} = this.props;
    return <BarContent data={data} {...rest} isTimeAxis={false}/>;
  }
}
