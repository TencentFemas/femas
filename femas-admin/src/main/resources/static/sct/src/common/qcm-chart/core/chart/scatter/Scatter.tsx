/* eslint-disable prettier/prettier */
/* eslint-disable @typescript-eslint/camelcase */
// @ts-nocheck
import AxisBase, {BizData} from '../AxisBase';
import * as d3 from 'd3';
import {generateCustomTooltip, generateScatterTooltip} from '../../tooltip/Tooltip';
import {
  DefaultPaddingXForHead,
  DefaultPaddingXForTail,
  GraphConfig,
  legendTheme,
  lineTheme,
} from '../../../theme/theme';
import {addAxisStyle, addSplitLine} from '../../axis/axis';
import {axisBrushDefault, AxisSplitLineDefault, axisZoomDefault} from '../../../type/chart';
import {calTooltipPos, convertDate, convertTranslate, startWorker} from '../../lib/helper';

export interface ScatterProps {
  /**
   * 散点图点大小
   * @default 2
   */
  pointSize?: number;
  /**
   * X轴是否需要hover line
   * @default true
   */
  xAxisNeedHoverLine?: boolean;
  /**
   * Y轴是否需要hover line
   * @default false
   */
  yAxisNeedHoverLine?: boolean;

  /**
   * 渐变颜色， 须与valueRange配合使用
   *
   * 如`['#B8D4FA', '#18669A']`
   */
  colorRange?: Array<string>;

  /**
   * 渐变值域， 须与colorRange配合使用
   *
   * 如`[0, 10]`
   */
  valueRange?: Array<number>;

  /**
   * 需要使用渐变属性的key，默认采用value值
   *
   * 例：{label: xxx, value: xxx, rangeKey: xxx} 需保证rangeKey中有数据
   */
  rangeKey?: string;

  /**
   * 返回点颜色
   * 注：当需要自定义颜色时可以使用
   */
  formateScatterColor?: (data) => string;

  /**
   * canvas模式，大数据模式下推荐使用
   * 注：缩放模式下不建议使用，且legend切换&hover等事件会有一定的延时
   * @default false
   */
  canvasMode?: boolean;
}

export class Scatter extends AxisBase<ScatterProps> {
  constructor(props) {
    super(props);
  }

  componentDidMount() {
    super.componentDidMount();
  }

  reload() {
    // reload时重新渲染
    const {scatterGroup, svgGroup} = this.state;
    if (scatterGroup !== null) {
      svgGroup.selectAll('g').remove();
      this.setState({
        scatterGroup: null,
      });
    }
    super.reload();
  }

  // 绘图
  drawChart() {
    const {svgGroup, scatterGroup, convertedData} = this.state;
    const {isTimeAxis = true, xAxisNeedHoverLine = true, yAxisNeedHoverLine} = this.props;
    const {show, xAxisNeedZoom, yAxisNeedZoom} = {
      ...axisZoomDefault,
      ...this.props.axisZoom,
    };
    const {show: needTooltip} = this.tooltip;

    if (!convertedData || !convertedData.length) return;

    if (scatterGroup && scatterGroup !== null) {
      return;
    }
    this.drawAxis();
    const curScatterGroup = svgGroup
    .append('g')
    .attr('class', 'd3-scatter')
    .attr('transform', `translate(${this.padding}, ${this.topPadding})`);
    this.setState(
        {
          scatterGroup: curScatterGroup,
          transformX: this.xScale,
          transformY: this.yScale,
          transformY2: this.y2Scale,
          brushSelection: [],
          showReset: false,
          colorMap: null,
        },
        async () => {
          this.generateClip(curScatterGroup);
          await this._drawColorLinear();
          this._drawDot();
          // 是否需要生成hoverLine
          if (needTooltip && (xAxisNeedHoverLine || yAxisNeedHoverLine)) {
            await this.generateHoverLine();
          }
          this._generateHoverCircleGroup();
          this.drawBrush();
          needTooltip && this.hoverEvent();
          needTooltip && this.mouseoutEvent();
          if (!show || !(xAxisNeedZoom || yAxisNeedZoom)) return;
          isTimeAxis && this.drawZoom();
        },
    );
  }

  // 转换数据
  async convertData() {
    // 处理横轴labels
    const {data, isTimeAxis = true} = this.props;
    const labels = [];
    if (!data) return;
    let convertedData;

    if (window.Worker) {
      convertedData = await startWorker({data, isTimeAxis, type: 'scatter'});
    } else {
      if (data.length > 1) {
        data.forEach(d => {
          d.value.forEach((v: BizData) => {
            isTimeAxis && labels.push(v.label);
            v.yIndex = d.yIndex || 0;
          });
        });
      }

      // 数据排序
      // 为了保证数据hover效果的准确性，序数比例尺也需要进行排序
      data.forEach(d => {
        d.value = d.value.sort(function (a, b) {
          return (isTimeAxis ? convertDate(a.label) > convertDate(b.label) : a.label > b.label) ? 1 : -1;
        });
      });

      if (isTimeAxis) {
        convertedData = data.map(d => ({
          ...d,
          value: d.value.map(v => ({...v, label: convertDate(v.label)})),
        }));
      }
    }

    this.setState({
      convertedData: convertedData || data,
    });
  }

  getStartAndEndTime(data) {
    let startTime = new Date().getTime();
    let endTime = 0;
    // 需要取start最小值以及end最大值
    data.forEach(item => {
      const [start, end] = d3.extent(item.value, d => d.label);
      startTime = Math.min(startTime, start);
      endTime = Math.max(endTime, end);
    });
    return {startTime, endTime};
  }

  // 绘制color map
  _drawColorLinear() {
    const {colorRange, valueRange} = this.props;
    if (colorRange?.length !== 2 || valueRange?.length !== 2) return;

    const colorMap = d3
    .scaleLinear()
    .domain(
        valueRange.sort(function (a, b) {
          return a - b;
        }),
    )
    .range(colorRange);

    this.setState({colorMap});
  }

  // 绘制点
  _drawDot() {
    const {svgGroup, scatterGroup, diagramHeight, parentId} = this.state;
    const {xScale, useableData, topPadding} = this;
    const {pointSize = GraphConfig.defaultPointSize, canvasMode} = this.props;

    scatterGroup.raise();
    // 处理legend hover后brush位置变化，无法再次hover问题
    svgGroup.select('g.brush').raise();
    svgGroup.select('g.reset').raise();

    scatterGroup.select('g.dots').remove();

    const dot = scatterGroup
    .append('g')
    .attr('class', 'dots')
    .selectAll('g.dot')
    .data(useableData)
    .enter()
    .append('g')
    .attr('class', 'dot')
    .attr('transform', d => `translate(0, ${this.dataInY2Axis(d.yIndex) ? diagramHeight / 2 - topPadding : 0})`);

    if (canvasMode) {
      this.convertCirclesToImage(dot);
      return;
    }

    const dotItem = dot
    .selectAll('g.dot-item')
    .data(d => d.value)
    .enter()
    .append('g')
    .attr('class', 'dot-item')
    .attr('fill', d => this.getCircleColor(d));

    dotItem
    .append('circle')
    .attr('clip-path', d => (this.dataInY2Axis(d.yIndex) ? `url(#clip-${parentId})` : `url(#clip_y-${parentId})`)) // 防止图形越界
    .attr('r', d => (d.value !== null && !d.hideValue ? pointSize : 0))
    .attr('cx', d => xScale(d.label))
    .attr('cy', d => this.getDataOfYAxis(d));
    // .on('mouseover', function () {
    //   d3.select(this).attr('r', pointSize + GraphConfig.pointSizeDiff)
    // })
    // .on('mouseleave', function () {
    //   d3.select(this).attr('r', pointSize)
    // })
    // .transition(this.transition)
  }

  convertCirclesToImage(parent, disableNames = []) {
    const {diagramWidth, diagramHeight, transformX} = this.state;
    const {pointSize = GraphConfig.defaultPointSize} = this.props;
    const canvas = document.createElement('canvas');
    const width = diagramWidth * window.devicePixelRatio;
    const height = diagramHeight * window.devicePixelRatio;
    canvas.width = width;
    canvas.setAttribute('style', `width:${diagramWidth}px;height:${diagramHeight}px`);
    const canvasCtx = canvas.getContext('2d');
    canvasCtx.scale(window.devicePixelRatio, window.devicePixelRatio);
    canvasCtx.translate(0, 0);
    let imageGroup = parent.select('image');
    if (imageGroup.empty()) {
      imageGroup = parent.append('image');
    }
    imageGroup
    .attr('x', 0)
    .attr('y', 0)
    .attr('width', width)
    .attr('height', height)
    .attr('xlink:href', data => {
      // canvas高度变更，则会清空
      canvas.height = height;

      data.value.forEach(d => {
        if (d.value === null || d.hideValue) {
          return;
        }
        canvasCtx.beginPath();
        canvasCtx.fillStyle =
            disableNames.indexOf(data.name) > -1 ? legendTheme.disabledColor : this.getCircleColor(d);
        canvasCtx.arc(transformX(d.label), this.getDataOfYAxis(d), pointSize, 0, Math.PI * 2, true);
        canvasCtx.fill();
      });
      return canvas.toDataURL();
    });
  }

  // 生成坐标轴Hover line
  generateHoverLine() {
    const {scatterGroup, diagramWidth, convertedData, parentId, yGroup} = this.state;
    const {xAxisNeedHoverLine = true, yAxisNeedHoverLine} = this.props;

    const hoverLineContainer = scatterGroup.append('g').attr('class', 'hover-container');

    const xHoverContainer =
        xAxisNeedHoverLine &&
        hoverLineContainer
        .append('g')
        .attr('class', 'x-container')
        .attr('opacity', 0);

    const tick = yGroup && yGroup.select('.tick:last-child');

    const translate = tick && !tick.empty() ? convertTranslate(tick.attr('transform')) : [];

    xAxisNeedHoverLine &&
    xHoverContainer
    .append('line')
    .attr('class', 'hover-line-x')
    .attr('clip-path', `url(#clip-${parentId})`)
    .attr('stroke', '#ddd')
    .attr('stroke-width', 1)
    .attr('stroke-dasharray', '5 5')
    .attr('y2', this.height)
    .attr('y1', translate.length ? translate[1] : 0);

    let yHoverContainer = null;

    // data长度大于1，不展示y hover line
    if (convertedData.length < 2) {
      yHoverContainer =
          yAxisNeedHoverLine &&
          hoverLineContainer
          .append('g')
          .attr('class', 'y-container')
          .attr('opacity', 0);

      yAxisNeedHoverLine &&
      yHoverContainer
      .append('line')
      .attr('class', 'hover-line-y')
      .attr('clip-path', `url(#clip_y-${parentId})`)
      .attr('stroke', '#ddd')
      .attr('stroke-width', 1)
      .attr('stroke-dasharray', '5 5')
      .attr('x2', diagramWidth);
    }

    this.setState(
        {
          xHoverContainer,
          yHoverContainer,
        },
        () => {
          this.hoverEvent();
        },
    );
  }

  // 生成hover circle group
  _generateHoverCircleGroup() {
    const {svgGroup} = this.state;
    if (svgGroup.select('g.hover-circles').empty()) {
      svgGroup
      .append('g')
      .attr('class', 'hover-circles')
      .attr('transform', `translate(${this.padding}, ${this.topPadding})`)
      .attr('opacity', 0);
    }
  }

  // 监听mouseout事件，统一处理
  mouseoutEvent() {
    const {xAxisNeedHoverLine = true, yAxisNeedHoverLine} = this.props;
    const {xHoverContainer, yHoverContainer, svgGroup, parentId} = this.state;
    const {show: needTooltip} = this.tooltip;

    this.overlayRect.on('mouseout', function () {
      const tooltip = document.querySelector(`#${parentId} .tooltip`);
      needTooltip && tooltip && tooltip.setAttribute('style', 'display: none');
      xAxisNeedHoverLine && xHoverContainer.attr('opacity', 0);
      yAxisNeedHoverLine && yHoverContainer && yHoverContainer.attr('opacity', 0);
      svgGroup.select('g.hover-circles').attr('opacity', 0);
    });
  }

  // 悬停回调函数
  hoverEvent(
      curXScale = this.state.transformX,
      curYScale = this.state.transformY,
      curY2Scale = this.state.transformY2,
  ) {
    const {convertedData, xHoverContainer, yHoverContainer, svgGroup, diagramWidth} = this.state;
    const {
      isTimeAxis = true,
      xAxisNeedHoverLine = true,
      yAxisNeedHoverLine,
      pointSize = GraphConfig.defaultPointSize,
      needDoubleYAxis,
      paddingXForHead = DefaultPaddingXForHead,
      paddingXForTail = DefaultPaddingXForTail,
    } = this.props;
    const {padding, topPadding, height, formateTime} = this;
    const {show: needTooltip} = this.tooltip;
    const self = this;

    const key = 'label';

    // 取hover时最靠近的点
    const bisect = (mx, my, data) => {
      if (isTimeAxis) {
        const date = curXScale.invert(mx);
        const index = d3.bisector(d => d[key]).left(data, date, 1);
        const a = data[index - 1];
        const b = data[index];
        // 容错处理
        const res = !a || !b ? a || b : date - a[key] > b[key] - date ? b : a;

        const result = [];

        // 处理多数据
        data.forEach(d => {
          if (d.label?.getTime() === res.label?.getTime()) {
            // 相同时间数据
            result.push(d);
          }
        });

        let indexY = 0;
        let min = 1000;
        result.forEach((v, i) => {
          const value = self.dataInY2Axis(v.yIndex) ? curY2Scale.invert(my - height / 2) : curYScale.invert(my);
          const diff = Math.abs(v.value - value);
          if (diff > min) {
            return;
          }
          indexY = i;
          min = diff;
        });
        return result[indexY];
      } else {
        const arr = data.map(d => curXScale(d.label));
        const index = this.closest(mx, arr);

        return data[index];
      }
    };

    const overlay = this.overlayRect
    .attr('width', diagramWidth - padding * 2 + pointSize * 2)
    .attr('height', height + pointSize * 2)
    .attr('transform', `translate(${padding - pointSize},${topPadding - pointSize})`);

    overlay.on('mousemove', function () {
      const [mouse_x, mouse_y] = d3.mouse(this);
      let hoverData = [];

      // 考虑到多条数据情况，对convertedData进行循环处理
      convertedData.forEach(d => {
        // 处理legend disable时的情况
        if (self.state.legendDisableKeys.indexOf(d.name) > -1) {
          return;
        }
        const data = bisect(
            mouse_x,
            mouse_y,
            d.value.filter(v => self.dataInBrushSelection(v)),
        );
        if (!data) return;
        const {value, hideValue} = data;

        // 保存当前label数据
        hoverData.push({
          ...d,
          ...data,
          x: curXScale(data[key]),
        });
        if (value === null || hideValue) return;

        if (yAxisNeedHoverLine && yHoverContainer) {
          yHoverContainer
          .select('line.hover-line-y')
          .attr('y1', curYScale(value))
          .attr('y2', curYScale(value));
          yHoverContainer.attr('opacity', 1);
        }

        // 数据 length为1时展示横向hover标示线
        if (yAxisNeedHoverLine && yHoverContainer) {
          yHoverContainer
          .select('line.hover-line-y')
          .attr('y1', curYScale(value))
          .attr('y2', curYScale(value));
          yHoverContainer.attr('opacity', 1);
        }
      });

      // 处理hoverData，当存在双轴时
      if (hoverData.length > 1) {
        // 判断在y1还是y2轴
        hoverData = hoverData.filter(d => (mouse_y > height / 2 ? d.yIndex === 1 : d.yIndex !== 1));
      }

      // 获取最近的点
      const tmpData = [...hoverData];
      tmpData.sort((a, b) => (Math.abs(a.x - mouse_x) > Math.abs(b.x - mouse_x) ? 1 : -1));
      const minData = tmpData[0];

      // 不在选区内
      if (!self.dataInBrushSelection(minData)) return;

      hoverData.forEach(d => {
        if (!(d.label - minData.label) || d.x === minData.x) {
          return;
        }
        if (!isTimeAxis && d.label === minData.label) {
          return;
        }
        d.value = null;
      });

      if (minData && xAxisNeedHoverLine) {
        // 根据xScale计算当前数据点坐标
        xHoverContainer
        .select('line.hover-line-x')
        .attr('x1', minData.x)
        .attr('x2', minData.x);

        xHoverContainer.attr('opacity', 1);
      }

      // 添加hover circle
      svgGroup
      .select('g.hover-circles')
      .raise() // 提升，避免出现hover circle与line层级交替问题
      .attr('opacity', 1)
      .selectAll('circle')
      .data(hoverData)
      .join('circle')
      .attr('r', ({value, hideValue}) => (value !== null && !hideValue ? lineTheme.hoverPointSize : 0))
      .attr('fill', '#fff')
      .attr('stroke', d => self.getCircleColor(d))
      .attr('stroke-width', lineTheme.hoverCircleStrokeWidth)
      .attr('opacity', ({value, yIndex, x, label}) => {
        if (label - minData.label && x !== minData.x) {
          return 0;
        }

        if (!isTimeAxis && label !== minData.label) {
          return 0;
        }

        const xIsOut = x < paddingXForHead - pointSize || x > diagramWidth - paddingXForTail;

        const yIsOut =
            curYScale(value) < -pointSize || curYScale(value) > (needDoubleYAxis ? height / 2 : height) + pointSize;

        const y2IsOut = curY2Scale(value) < -pointSize || curY2Scale(value) > height / 2 + pointSize;

        return xIsOut || (self.dataInY2Axis(yIndex) ? y2IsOut : yIsOut) ? 0 : 1;
      })
      .attr('transform', data => {
        const {yIndex, x} = data;

        const y = self.getDataOfYAxis(data, curYScale, curY2Scale);
        return `translate(${x}, ${self.dataInY2Axis(yIndex) ? y + height / 2 : y})`;
      });

      // 处理legend 事件后，无法再次hover问题
      svgGroup.select('g.brush').raise();
      svgGroup.select('g.reset').raise();
      if (!needTooltip) return;
      // 处理Tooltip数据
      self.tooltipCallback(
          hoverData
          .filter(d => d.value !== null && !d.hideValue)
          .map(d => ({
            ...d,
            label: d.tipLabel || (isTimeAxis ? formateTime(d.label) : d.label),
          })),
          mouse_x,
          mouse_y,
      );
    });
  }

  // 处理Tooltip
  tooltipCallback(hoverData, mouse_x, mouse_y) {
    const {diagramHeight, diagramWidth, xHoverContainer, yHoverContainer, parentId, colorMap} = this.state;
    const {xAxisNeedHoverLine = true, yAxisNeedHoverLine} = this.props;
    const {formatter, additionalTip, showSplitLine} = this.tooltip;

    const tooltipContainer = d3
    .select(`#${parentId}`)
    .select('section.chart')
    .selectAll('div.tooltip')
    .data([null])
    .join('div')
    .attr('class', 'tooltip');

    const tooltip = document.querySelector(`#${parentId} .tooltip`);

    // 避免出现空白Tooltip
    if (!hoverData.length) {
      // 隐藏hover line
      xAxisNeedHoverLine && xHoverContainer.attr('opacity', 0);
      yAxisNeedHoverLine && yHoverContainer && yHoverContainer.attr('opacity', 0);
      tooltip && tooltip.setAttribute('style', 'display: none');
      return;
    }

    const data = hoverData[0];

    tooltip.innerHTML = formatter
        ? generateCustomTooltip(formatter, hoverData, this.colors, colorMap)
        : generateScatterTooltip(data, this.getCircleColor(data), additionalTip, showSplitLine);

    calTooltipPos(tooltipContainer, diagramWidth, diagramHeight, parentId, mouse_x, mouse_y);
  }

  // 缩放&平移效果
  drawZoom() {
    const {svgSelection} = this.state;
    const {xScale, zoom, yScale, y2Scale} = this;
    const groupZoom = zoom.on('zoom', () => {
      //获取新scale
      const transformX = d3.event.transform.rescaleX(xScale);
      const transformY = d3.event.transform.rescaleY(yScale);
      const transformY2 = d3.event.transform.rescaleY(y2Scale);

      const {yAxisNeedZoom, show: zoomShow, xAxisNeedZoom} = {
        ...axisZoomDefault,
        ...this.props.axisZoom,
      };
      if (!zoomShow || (zoomShow && !xAxisNeedZoom && !yAxisNeedZoom)) {
        return;
      }
      this.setState(
          {
            transformX: zoomShow && xAxisNeedZoom ? transformX : xScale,
            transformY: zoomShow && yAxisNeedZoom ? transformY : yScale,
            transformY2: zoomShow && yAxisNeedZoom ? transformY2 : y2Scale,
          },
          () => {
            this.handlerZoom();
          },
      );
    });

    this.setState({
      groupZoom,
    });

    svgSelection
    .call(groupZoom)
    // 禁止双击放大
    .on('dblclick.zoom', null)
    .transition(this.transition);

    groupZoom.scaleTo(svgSelection, 1);
  }

  // 判断数据是否在选区内
  dataInBrushSelection(d, brushSelection = this.state.brushSelection) {
    const {isTimeAxis = true} = this.props;
    const {showReset} = this.state;

    if (!d) return false;

    if (!brushSelection.length) {
      return !showReset;
    }

    let inSelection = true;
    const [start, end] = brushSelection;

    if (isTimeAxis) {
      if (d.label.getTime() < start.getTime() || d.label.getTime() > end.getTime()) {
        inSelection = false;
      }
    } else {
      // 非时间轴
      // 对比节点的坐标位置
      if (brushSelection.indexOf(d.label) < 0) {
        inSelection = false;
      }
    }
    return inSelection;
  }

  handlerZoom() {
    const {
      xGroup,
      yGroup,
      y2Group,
      scatterGroup,
      svgGroup,
      xHoverContainer,
      yHoverContainer,
      parentId,
      transformX,
      transformY,
      transformY2,
    } = this.state;
    const {xAxisNeedHoverLine = true, yAxisNeedHoverLine, needDoubleYAxis, canvasMode} = this.props;
    const {xAxis, yAxis, y2Axis} = this;
    const {show: needTooltip} = this.tooltip;
    const self = this;

    const {yAxisNeedZoom, show: zoomShow, xAxisNeedZoom} = {
      ...axisZoomDefault,
      ...this.props.axisZoom,
    };

    const {show, yAxisNeedBrushZoom} = {
      ...axisBrushDefault,
      ...this.props.axisBrushZoom,
    };

    const {xAxisNeedSplitLine, yAxisNeedSplitLine} = {
      ...AxisSplitLineDefault,
      ...this.props.axisSplitLine,
    };

    const key = 'label';
    //缩放&移动坐标轴
    if ((zoomShow && xAxisNeedZoom) || show) {
      xGroup.call(xAxis.scale(transformX));
    }
    ((zoomShow && yAxisNeedZoom) || (show && yAxisNeedBrushZoom)) && yGroup.call(yAxis.scale(transformY));
    needDoubleYAxis &&
    ((zoomShow && yAxisNeedZoom) || (show && yAxisNeedBrushZoom)) &&
    y2Group.call(y2Axis.scale(transformY2));

    if (xAxisNeedHoverLine || yAxisNeedHoverLine || needTooltip) {
      // 先隐藏，再重新计算hover line的scale值
      xAxisNeedHoverLine && xHoverContainer.attr('opacity', 0);
      yAxisNeedHoverLine && yHoverContainer && yHoverContainer.attr('opacity', 0);
      const tooltip = document.querySelector(`#${parentId} .tooltip`);
      needTooltip && tooltip && tooltip.setAttribute('style', 'display: none');
      this.hoverEvent();
    }

    // 处理hover circle
    svgGroup.select('g.hover-circles').attr('opacity', 0);

    const circleG = scatterGroup.select('g.dots');
    if (canvasMode) {
      circleG
      .selectAll('g.dot')
      .selectAll('image')
      .remove();
      circleG.selectAll('g.dot').each(function () {
        self.convertCirclesToImage(d3.select(this));
      });
    } else {
      // 处理circle位置
      const circles = scatterGroup
      .select('g.dots')
      .selectAll('g.dot-item')
      .selectAll('circle');

      circles.attr('cx', function (d) {
        return transformX(d[key]);
      });
      circles.attr('cy', d => (this.dataInBrushSelection(d) ? this.getDataOfYAxis(d) : 0));

      circles.attr('opacity', d => (this.dataInBrushSelection(d) ? 1 : 0));
    }
    // 重新添加坐标轴样式
    addAxisStyle(this);

    if (xAxisNeedSplitLine || yAxisNeedSplitLine) {
      // 重新处理坐标轴分割线
      svgGroup.selectAll('line.split-line').remove();
      addSplitLine(this);
    }
  }

  // hover legend
  onLegendItemHover({name}) {
    const {scatterGroup} = this.state;
    const {canvasMode} = this.props;
    const {colors, useableData} = this;
    const self = this;

    // 处理dots
    scatterGroup
    .select('g.dots')
    .selectAll('g.dot')
    .call(g => {
      g.each(function highlight({name: gName}) {
        const ele = d3.select(this);
        if (gName === name) {
          ele.raise();
        }
        if (canvasMode) {
          self.convertCirclesToImage(
              g,
              useableData?.filter(d => d.name !== name)?.map(d => d.name),
          );
          return;
        }
        if (gName === name) {
          ele.select('g.dot-item').attr('fill', colors[name]);
        } else {
          ele.select('g.dot-item').attr('fill', legendTheme.disabledColor);
        }
      });
    });
  }

  // 移出
  onLegendItemLeave() {
    this._drawDot();
    this.handlerZoom();
  }

  // legend click
  onLegendItemClick(data) {
    const {isTimeAxis = true} = this.props;
    super.onLegendItemClick(data);

    const {show: zoomShow} = {
      ...axisZoomDefault,
      ...this.props.axisZoom,
    };

    const {show} = {
      ...axisBrushDefault,
      ...this.props.axisBrushZoom,
    };

    if ((isTimeAxis && zoomShow) || show) {
      this.handlerZoom();
    }
  }

  getScaleExtent() {
    const {convertedData} = this.state;

    const {startTime, endTime} = this.getStartAndEndTime(convertedData);

    const diffTime = (endTime - startTime) / 1000 / 10;

    // 缩放粒度
    return diffTime / 60;
  }

  // 堆叠图：获取同一坐标前一数据
  getBeforeValue() {
    return [];
  }

  // 处理brush选区数据
  brushFinished(selection, yValues, ySelections) {
    const {transformX, convertedData, transformY, transformY2} = this.state;
    const {isTimeAxis = true} = this.props;
    const {yAxisNeedBrushZoom, brushCallback, onlyBrush} = {
      ...axisBrushDefault,
      ...this.props.axisBrushZoom,
    };

    let data = selection;
    if (isTimeAxis) {
      const interval = d3.timeSecond.every(1);
      data = selection.map(d => interval.round(transformX.invert(d)));
    } else {
      const values = [];
      convertedData.forEach(data => {
        data.value.forEach(v => {
          const x = transformX(v.label);
          if (selection.length && x >= selection[0] && x <= selection[1]) {
            values.push(v.label);
          }
        });
      });
      data = Array.from(new Set(values));
    }
    if (!data.length) return this.state.showReset || false;

    this.brushTooltipCallback(data, yValues, selection, ySelections);
    brushCallback && brushCallback(data, yValues);

    if (onlyBrush) return;

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
      transformX: isTimeAxis ? transformX.domain(data) : transformX.domain(this.getAxisLabels(d)),
      brushSelection: data,
      showReset: data.length !== this.getAxisLabels().length,
    });

    this.handlerZoom();
    return data.length !== this.getAxisLabels().length;
  }

  getXScale() {
    const {isTimeAxis = true} = this.props;
    const {convertedData} = this.state;

    const {startTime: start, endTime: end} = this.getStartAndEndTime(convertedData);

    return isTimeAxis ? d3.scaleTime().domain([start, end]) : d3.scalePoint().domain(this.getAxisLabels());
  }

  getCircleColor(d) {
    const {rangeKey, formateScatterColor} = this.props;
    const {colorMap} = this.state;
    return formateScatterColor
        ? formateScatterColor(d)
        : colorMap
            ? rangeKey
                ? colorMap(d[rangeKey])
                : colorMap(d.value)
            : this.colors[d.name];
  }
}
