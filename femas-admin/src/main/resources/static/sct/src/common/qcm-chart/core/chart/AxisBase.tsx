/* eslint-disable prettier/prettier */
// @ts-nocheck
import Base from './Base';
import * as d3 from 'd3';
import {axisBrushDefault, AxisProps, Value} from '../../type/chart';
import {addArrow, addAxisStyle, addDesc, addSplitLine, toggleAxisLine} from '../axis/axis';
import {calTooltipPos, judgeTopCondition, nodeHeight, nodeWidth} from '../lib/helper';
import {
  axisTheme,
  DefaultPaddingXForHead,
  DefaultPaddingXForTail,
  GraphConfig
} from '../../theme/theme';
import {generateBrushTooltip} from '../tooltip/Tooltip';

export interface BizData extends Value {
  yIndex?: number;
}

export interface StackProps {
  /**
   * @ignore
   *
   * 是否为堆叠图
   */
  isStack?: boolean;
}

// 实现坐标轴相关逻辑
export default abstract class AxisBase<T> extends Base<T & AxisProps & StackProps> {
  get xAxis() {
    const {isTimeAxis = true} = this.props;
    const {diagramWidth} = this.state;
    const xAxis = d3.axisBottom().scale(this.xScale);
    // 处理ticks，避免axis显示过多tick
    return isTimeAxis
        ? xAxis
        .ticks(diagramWidth < GraphConfig.THRESHOLD_WIDTH ? axisTheme.miniTickSize : axisTheme.normalTickSize)
        .tickFormat(d3.timeFormat('%H:%M'))
        : xAxis;
  }

  get yAxis() {
    return d3
    .axisLeft()
    .scale(this.yScale)
    .ticks(this.tickSizeY);
  }

  get tickSizeY() {
    const {diagramHeight} = this.state;
    const {needDoubleYAxis} = this.props;
    const h = diagramHeight / (needDoubleYAxis ? 2 : 1);
    return Math.ceil(h / this.yTickHeight);
  }

  get yTickHeight() {
    return this.props.yTickHeight || axisTheme.yTickHeight;
  }

  get y2Axis() {
    return d3
    .axisLeft()
    .scale(this.y2Scale)
    .ticks(this.tickSizeY);
  }

  get xScale() {
    const {diagramWidth} = this.state;
    const {paddingXForHead = DefaultPaddingXForHead, paddingXForTail = DefaultPaddingXForTail} = this.props;

    const scale = this.getXScale();

    return scale.range([paddingXForHead, diagramWidth - this.padding * 2 - paddingXForTail]);
  }

  get padding() {
    return this.props.padding || 0;
  }

  get diffSize() {
    return this.props.axisDiffSize ?? GraphConfig.AXIS_DIFF;
  }

  get yScale() {
    const {convertedData} = this.state;
    const {needDoubleYAxis, axisDomain, yAxisType} = this.props;
    const {height} = this;
    const max = this.getYMax(convertedData);
    const min = this.getYMin(convertedData);
    const tickStep = d3.tickStep(min, max, this.tickSizeY);
    return (yAxisType === 'sqrt' ? d3.scaleSqrt() : d3.scaleLinear())
    .domain(axisDomain?.yDomain ? axisDomain.yDomain : [min, Math.ceil(max / tickStep) * tickStep])
    .nice()
    .range([needDoubleYAxis ? height / 2 : height, 0]);
  }

  get y2Scale() {
    const {convertedData} = this.state;
    const {needDoubleYAxis, axisDomain, yAxisType} = this.props;
    const {height} = this;

    const max = this.getYMax(convertedData, true);
    const min = this.getYMin(convertedData, true);
    const tickStep = d3.tickStep(min, max, this.tickSizeY);

    return (yAxisType === 'sqrt' ? d3.scaleSqrt() : d3.scaleLinear())
    .domain(axisDomain?.y2Domain ? axisDomain.y2Domain : [min, Math.ceil(max / tickStep) * tickStep])
    .nice()
    .range([0, needDoubleYAxis ? height / 2 : height]);
  }

  get diffPadding() {
    return judgeTopCondition(this) ? 0 : this.topPadding;
  }

  get brush() {
    const {diagramWidth, diagramHeight} = this.state;
    const {padding, topPadding, bottomPadding} = this;
    const {onlyBrush, showCustomBrushY} = {
      ...axisBrushDefault,
      ...this.props.axisBrushZoom,
    };
    const brush = onlyBrush && showCustomBrushY ? d3.brush() : d3.brushX();

    return brush
    .extent([
      [padding, topPadding],
      [diagramWidth - padding, diagramHeight - bottomPadding],
    ])
    .on('end', () => this.brushended());
  }

  get overlayRect() {
    const {diagramHeight, diagramWidth, svgGroup} = this.state;
    const {padding} = this;
    const {show} = {...axisBrushDefault, ...this.props.axisBrushZoom};

    if (show) {
      return svgGroup
      .select('g.brush')
      .select('rect.overlay')
      .attr('x', 0)
      .attr('y', 0);
    }

    const overlay = svgGroup.select('rect.overlay');
    if (!overlay.empty()) return overlay.raise();
    return svgGroup
    .append('rect')
    .attr('class', 'overlay')
    .attr('width', diagramWidth - padding * 2)
    .attr('height', diagramHeight - padding * 2)
    .attr('opacity', 0);
  }

  abstract handlerZoom();

  handleEmptyData(data) {
    const {parentId} = this.state;
    let noData = true;
    if (!(data instanceof Array)) {
      noData = !data;
    } else {
      noData = !(data && data.length && data.some(d => d.value && d.value.length));
    }
    this.setState(
        {
          noData,
        },
        () => {
          if (!noData) this.reload();
        },
    );
    d3.select(`#${parentId}`).style('display', noData ? 'none' : 'block');
    return noData;
  }

  _initBrushTooltip() {
    const {parentId} = this.state;
    const tooltip = document.querySelector(`#${parentId} .tooltip-brush`);
    tooltip && tooltip.remove();
  }

  drawBrush() {
    const {svgGroup, diagramWidth} = this.state;
    const {show, resetText, onlyBrush} = {
      ...axisBrushDefault,
      ...this.props.axisBrushZoom,
    };
    this._initBrushTooltip();

    if (!show) {
      return;
    }
    const self = this;
    const brush = svgGroup
    .append('g')
    .attr('class', 'brush')
    .raise()
    .call(this.brush);
    brush.select('rect.selection').attr('fill', '#bbb');

    brush.select('rect.overlay').attr('transform', 'translate(0, 0)');

    if (onlyBrush) return;

    // 添加重置按钮
    const reset = svgGroup
    .append('g')
    .attr('class', 'reset')
    .style('cursor', 'pointer')
    .attr('opacity', 0)
    .on('click', function () {
      // 重置
      (d3 as any).event.stopPropagation();
      d3.select(this).attr('opacity', 0);
      self.setState({
        transformX: self.xScale,
        transformY: self.yScale,
        transformY2: self.y2Scale,
        brushSelection: [],
        showReset: false,
      });
      self.handlerZoom();
    });

    reset
    .append('rect')
    .attr('fill', '#f2f2f2')
    .attr('stroke-width', 1)
    .attr('stroke', '#dddddd')
    .attr('height', 30)
    .attr('width', 40);

    reset
    .append('text')
    .text(resetText)
    .style('font-size', '12px')
    .attr('fill', '#333')
    .attr('x', 8)
    .attr('y', 20);

    reset.attr('transform', function () {
      return `translate(${diagramWidth - nodeWidth(d3.select(this).node())}, ${self.topPadding})`;
    });
  }

  // 绘制坐标轴
  drawAxis(handlerX = false) {
    const {diagramHeight, svgGroup} = this.state;
    const {needDoubleYAxis, hideX, hideY} = this.props;
    const {padding, topPadding, bottomPadding, diffPadding} = this;
    const xGroup = svgGroup
    .append('g')
    .attr('class', 'x-axis')
    .attr(
        'transform',
        `translate(${padding}, ${needDoubleYAxis ? diagramHeight / 2 - diffPadding : diagramHeight - bottomPadding})`,
    )
    .attr('opacity', hideX ? 0 : 1)
    .call(this.xAxis);

    const yGroup = svgGroup
    .append('g')
    .attr('class', 'y-axis')
    .attr('transform', `translate(${padding}, ${topPadding})`)
    .attr('opacity', hideY ? 0 : 1)
    .call(this.yAxis);

    let y2Group = null;

    if (needDoubleYAxis) {
      y2Group = svgGroup
      .append('g')
      .attr('class', 'y2-axis')
      .attr('transform', `translate(${padding}, ${diagramHeight / 2 - diffPadding})`)

      .attr('opacity', hideY ? 0 : 1)
      .call(this.y2Axis);
    }

    this.setState(
        {
          xGroup,
          yGroup,
          y2Group,
        },
        () => {
          // 添加坐标轴样式
          addAxisStyle({...this, handlerX});
          // 根据props绘制坐标轴附加
          addArrow(this);
          addSplitLine(this);
          addDesc(this);
          toggleAxisLine(this);
        },
    );
  }

  // 生成clip path，防止缩放图形溢出
  generateClip(group) {
    const {svgGroup, diagramWidth, diagramHeight, parentId} = this.state;
    const {
      needDoubleYAxis,
      paddingXForHead = DefaultPaddingXForHead,
      paddingXForTail = DefaultPaddingXForTail,
    } = this.props;
    const {padding, height, diffSize, legendGroup} = this;

    // 添加首尾留白间距
    const width = diagramWidth - padding * 2 - paddingXForHead - paddingXForTail + diffSize * 2;

    const clipGroup = svgGroup.append('g').attr('class', 'clip-paths');

    clipGroup
    .append('clipPath')
    .attr('id', `clip-${parentId}`)
    .append('rect')
    .attr('width', width)
    .attr('height', height + diffSize * 2)
    .attr('transform', `translate(${paddingXForHead - diffSize},-${diffSize})`);

    clipGroup
    .append('clipPath')
    .attr('id', `clip_y-${parentId}`)
    .append('rect')
    .attr('width', width)
    .attr(
        'height',
        (needDoubleYAxis
            ? (diagramHeight - padding - nodeHeight(legendGroup.node())) / 2 - GraphConfig.MINI_PADDING - 16 // 16为toolbar icon高度
            : height) +
        diffSize * 2,
    )
    .attr('transform', `translate(${paddingXForHead - diffSize}, -${diffSize})`);

    // 防止存在双y轴时图形越界
    group.attr('clip-path', `url(#clip-${parentId})`);
  }

  // 判断是否处于y2轴
  dataInY2Axis(yIndex) {
    const {needDoubleYAxis} = this.props;
    return yIndex === 1 && needDoubleYAxis;
  }

  // 获取y值
  getDataOfYAxis(curData, yScale = this.state.transformY, y2Scale = this.state.transformY2) {
    if (!curData) return 0;

    // 处理堆叠数据
    const {isStack} = this.props;

    // 获取当前数据之前已绘制的数据
    const beforeDatas = isStack ? this.getBeforeValue(curData, yScale, y2Scale) : [];

    const y = this.dataInY2Axis(curData.yIndex) ? y2Scale(curData.value) : yScale(curData.value);

    return (
        y -
        (isStack
            ? beforeDatas
            .map(v => (this.dataInY2Axis(curData.yIndex) ? y2Scale(0) - v : yScale(0) - v))
            .reduce((pre, cur) => pre + cur, 0)
            : 0)
    );
  }

  // 堆叠图：获取同一坐标前一数据
  abstract getBeforeValue(curData, yScale, y2Scale);

  convertStackData(data) {
    return data;
  }

  getScaleExtent() {
    const {convertedData} = this.state;

    const {startTime, endTime} = this.getStartAndEndTime(convertedData);

    const diffTime = (endTime - startTime) / 1000 / 10;

    // 缩放粒度
    return diffTime / 60;
  }

  getStartAndEndTime(data, isCompare = false) {
    let startTime = new Date().getTime();
    let endTime = 0;
    // 如果isCompare为true时，需要比较startTime与endTime的差
    if (isCompare) {
      const times = [];
      data.forEach(item => {
        const [start, end] = d3.extent(item.value, d => d.label);
        times.push({startTime: start, endTime: end});
      });
      let diff = 0;
      let index = 0;
      times.forEach(({startTime, endTime}, i) => {
        if (endTime - startTime > diff) {
          diff = endTime - startTime;
          index = i;
        }
      });
      return {...times[index]};
    }
    // 需要取start最小值以及end最大值
    data.forEach(item => {
      const [start, end] = d3.extent(item.value, d => d.label);
      startTime = Math.min(startTime, start);
      endTime = Math.max(endTime, end);
    });
    return {startTime, endTime};
  }

  getXScale() {
    const {isTimeAxis = true} = this.props;
    const {convertedData} = this.state;
    const {startTime: start, endTime: end} = this.getStartAndEndTime(convertedData);
    return isTimeAxis
        ? d3.scaleTime().domain([start, end])
        : d3.scalePoint().domain(convertedData[0].value.map(d => d.label));
  }

  getYMax(data, isY2 = false) {
    const {needDoubleYAxis, isStack} = this.props;
    let max = d3.max(
        data.filter(v => (isY2 ? v.yIndex === 1 : !v.yIndex)),
        d => d3.max(d.value, v => +v.value),
    );
    // 堆叠图最大值为各数值相加
    if (isStack) {
      const stackData = this.convertStackData(
          data.filter(d => !needDoubleYAxis || (isY2 ? d.yIndex === 1 : !d.yIndex)),
      );
      max = d3.max(stackData, d =>
          d.value.reduce((pre, cur) => {
            return cur.value > 0 ? pre + cur.value : pre;
          }, 0),
      );
    }
    return !max ? 1 : max;
  }

  getYMin(data, isY2 = false) {
    const {needDoubleYAxis, isStack} = this.props;
    let min = d3.min(
        data.filter(v => (isY2 ? v.yIndex === 1 : !v.yIndex)),
        d => d3.min(d.value, v => +v.value),
    );
    // 堆叠图最小值为各数值相加
    if (isStack) {
      const stackData = this.convertStackData(
          data.filter(d => !needDoubleYAxis || (isY2 ? d.yIndex === 1 : !d.yIndex)),
      );
      min = d3.min(stackData, d =>
          d.value.reduce((pre, cur) => {
            return cur.value < 0 ? pre + cur.value : pre;
          }, 0),
      );
    }
    return min < 0 ? min : 0;
  }

  brushended() {
    const selection = d3.event.selection;
    const {brush} = this;
    const {svgGroup} = this.state;
    const {onlyBrush} = {
      ...axisBrushDefault,
      ...this.props.axisBrushZoom,
    };
    this._initBrushTooltip();
    if (!d3.event.sourceEvent || !selection) return;
    let xSelections = selection;
    let yValues = [];
    let ySelections = [];
    if (selection.every(d => typeof d === 'object')) {
      // 自定义y brush开启
      xSelections = selection.map(d => d[0]);
      ySelections = selection.map(d => d[1]);
      yValues = this._handleYValues(selection.map(d => d[1]));
    }
    const showReset = this.brushFinished(
        xSelections.map(d => d - this.padding),
        yValues,
        ySelections,
    );

    !onlyBrush &&
    svgGroup
    .select('g.brush')
    .transition()
    .call(brush.move, null);
    !onlyBrush && svgGroup.select('g.reset').attr('opacity', showReset ? 1 : 0);
  }

  _handleYValues(selections) {
    const {transformY, transformY2, diagramHeight} = this.state;
    const {needDoubleYAxis} = this.props;
    const diff = diagramHeight / 2 - this.diffPadding;
    const yValues = selections.map(v => {
      const isY2 = !!(needDoubleYAxis && v >= diff);
      return isY2 ? transformY2.invert(v - diff) : transformY.invert(v - this.topPadding);
    });

    // 判断是否跨轴
    if (needDoubleYAxis && selections.some(v => v < diff) && selections.some(v => v > diff)) {
      const res = [];
      // 判断值大小
      yValues.forEach(v => {
        res.push(...(v > 0 ? [0, v] : [v, 0]));
      });
      return res;
    }

    return yValues.sort(function (a, b) {
      return a - b;
    });
  }

  brushTooltipCallback(xValues, yValues, xSelections, ySelections) {
    const {diagramHeight, diagramWidth, parentId} = this.state;
    const {onlyBrush, genBrushTooltip, brushTipClickCallback} = {
      ...axisBrushDefault,
      ...this.props.axisBrushZoom,
    };

    if (!onlyBrush || !genBrushTooltip) return;

    const tooltipContainer = d3
    .select(`#${parentId}`)
    .select('section.chart')
    .selectAll('div.tooltip-brush')
    .data([null])
    .join('div')
    .attr('class', 'tooltip-brush');

    const tooltip = document.querySelector(`#${parentId} .tooltip-brush`);

    // 避免出现空白Tooltip
    if (!xValues?.length && !yValues?.length) {
      this._initBrushTooltip();
      return;
    }

    tooltip.addEventListener('click', function (e) {
      e.stopPropagation();
      e.preventDefault();
      brushTipClickCallback && brushTipClickCallback(e, xValues, yValues);
    });

    const my = ySelections?.length ? ySelections.reduce((pre, cur) => pre + cur, 0) / 2 : this.height / 2;

    let mx = xSelections[1];

    tooltip.innerHTML = generateBrushTooltip(genBrushTooltip, xValues, yValues, mx, my);

    const tooltipContent = document.querySelector(`#${parentId} .tooltip-brush .tea-chart-tooltip`);

    if (tooltipContent && mx > diagramWidth - tooltipContent.clientWidth) {
      mx = xSelections[0] - tooltipContent.clientWidth;
    }
    if (tooltipContent && mx < tooltipContent.clientWidth) {
      mx = Math.min(xSelections[0] + (xSelections[1] - xSelections[0]) / 2, tooltipContent.clientWidth);
    }

    calTooltipPos(tooltipContainer, diagramWidth, diagramHeight, parentId, mx, my, 'tooltip-brush', false);
  }

  abstract brushFinished(xSelections, yValues, ySelections);

  getAxisLabels(data = this.state.convertedData) {
    const axisData = [];

    data.forEach(d => {
      d.value.forEach(v => {
        axisData.push(v.label);
      });
    });

    return Array.from(new Set(axisData));
  }

  // 非时间轴（序数比例尺）需要实现获取最接近的index
  closest(num, arr) {
    let mid;
    let lo = 0;
    let index = arr.length - 1;
    while (index - lo > 1) {
      mid = Math.floor((lo + index) / 2);
      if (arr[mid] < num) {
        lo = mid;
      } else {
        index = mid;
      }
    }
    if (num - arr[lo] <= arr[index] - num) {
      return lo;
    }
    return index;
  }
}
