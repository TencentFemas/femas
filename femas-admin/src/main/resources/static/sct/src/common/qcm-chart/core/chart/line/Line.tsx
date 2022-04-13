/* eslint-disable prettier/prettier */
// @ts-nocheck
import AxisBase, {BizData} from '../AxisBase';
import * as d3 from 'd3';
import {generateCustomTooltip, generateLineTooltip} from '../../tooltip/Tooltip';
import {
  axisTheme,
  DefaultPaddingXForHead,
  DefaultPaddingXForTail,
  GraphConfig,
  legendTheme,
  lineTheme,
} from '../../../theme/theme';
import {addAxisStyle, addSplitLine} from '../../axis/axis';
import {
  AdditionalLine,
  axisBrushDefault,
  AxisProps,
  AxisSplitLineDefault,
  axisZoomDefault,
  compareLineDefaultOptions,
  Data,
  DataProps,
} from '../../../type/chart';
import {
  calTooltipPos,
  convertDate,
  convertTranslate,
  getCompareAxisLabel,
  hexToRGB,
  nodeHeight,
  nodeWidth,
  startWorker,
} from '../../lib/helper';

export interface AreaLineProps {
  /**
   * 是否需要渲染为area样式
   * @default false
   */
  isArea?: boolean;
}

export interface CompareLineProps {
  /**
   * 是否为对比图（支持时间段长度一致的数据对比）
   */
  compare?: Compare;
}

export interface BiaxialLineProps {
  /**
   * 是否需要展示双Y轴
   * @default false
   */
  needDoubleYAxis?: boolean;
}

export interface LineProps {
  /**
   * 折线图点大小
   * @default 2
   */
  pointSize?: number;
  /**
   * 折线图线宽度
   * @default 3
   */
  lineWidth?: number;
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
   * 是否为曲线
   * @default true
   */
  isCurve?: boolean;

  /**
   * @ignore
   *
   * 是否为简易图
   *
   * 需要隐藏legend、坐标轴等
   */
  isSimple?: boolean;

  /**
   * 是否连接空值
   *
   * @default false
   */
  connectNulls?: boolean;

  /**
   * canvas模式，大数据模式下推荐使用
   * 注：缩放模式下不建议使用，且legend切换&hover等事件会有一定的延时
   * @default true
   */
  canvasMode?: boolean;

  /**
   * 点与线大小不需要进行区分时，建议使用
   * @default false
   */
  hideDots?: boolean;

  /**
   * 曲线模式，d3原生支持的曲线模式
   * @default curveMonotoneX
   */
  curveMode?: string;
}

const DEFAULT_CURVE_MODE = 'curveMonotoneX';

export interface Compare {
  /**
   * 是否为对比图（支持时间段长度一致的数据对比）
   * @default false
   */
  isCompare: boolean;
  /**
   * 返回period，单位为秒
   *
   * 默认为1天以内粒度1分钟；1天-3天粒度1小时；3天-7天粒度1天 —— 标识横坐标轴的最小刻度
   */
  calculatePeriod?: (startTime: Date, endTime: Date) => number; // 单位 s
}

export interface LineConnectProps extends LineProps, DataProps, AxisProps, AreaLineProps {
}

export class Line extends AxisBase<LineConnectProps & CompareLineProps & BiaxialLineProps> {
  constructor(props) {
    super(props);
  }

  get line() {
    const {transformX} = this.state;
    const {isCurve = true, connectNulls, curveMode = DEFAULT_CURVE_MODE} = this.props;
    const compare = {...compareLineDefaultOptions, ...this.props.compare};

    const line = d3
    .line()
    // 排除null数据，绘制分段path
    .defined(d => connectNulls || d.value !== null)

    .x(d => transformX(compare.isCompare ? d.axisNumber : d.label))
    .y(d => this.getDataOfYAxis(d));
    return isCurve ? line.curve(d3[curveMode]) : line;
  }

  get area() {
    const {transformX, transformY, transformY2} = this.state;
    const {isCurve = true, isStack, connectNulls, curveMode = DEFAULT_CURVE_MODE} = this.props;
    let {compare} = this.props;
    compare = {...compareLineDefaultOptions, ...compare};
    const area = d3
    .area()
    // 排除null数据，绘制分段path
    .defined(d => connectNulls || d.value !== null)
    .x(d => transformX(compare.isCompare ? d.axisNumber : d.label))
    .y0(d => {
      // 堆叠数据处理，非堆叠时为0，上半轴需要使用过yScale(0)
      const beforeDatas = isStack ? this.getBeforeValue(d) : [];
      return this.dataInY2Axis(d.yIndex)
          ? isStack
              ? beforeDatas.reduce((pre, cur) => pre + cur, 0)
              : transformY2(0)
          : beforeDatas.length && isStack
              ? beforeDatas.reduce((pre, cur) => pre + cur, 0)
              : transformY(0);
    })
    .y1(d => this.getDataOfYAxis(d));
    return isCurve ? area.curve(d3[curveMode]) : area;
  }

  get xAxis() {
    const {isTimeAxis = true} = this.props;
    let {compare} = this.props;
    const {diagramWidth} = this.state;
    compare = {...compareLineDefaultOptions, ...compare};
    const xAxis = d3.axisBottom().scale(this.xScale);
    // 处理ticks，避免axis显示过多tick
    return isTimeAxis && !compare.isCompare
        ? xAxis
        .ticks(diagramWidth < GraphConfig.THRESHOLD_WIDTH ? axisTheme.miniTickSize : axisTheme.normalTickSize)
        .tickFormat(d3.timeFormat('%H:%M'))
        : xAxis;
  }

  reload() {
    // reload时重新渲染
    const {lineGroup, svgGroup} = this.state;
    if (lineGroup !== null) {
      svgGroup.selectAll('g').remove();
      this.setState({
        lineGroup: null,
      });
    }
    super.reload();
  }

  // 绘图
  drawChart() {
    const {svgGroup, lineGroup, convertedData} = this.state;
    const {isTimeAxis = true, xAxisNeedHoverLine = true, yAxisNeedHoverLine, isSimple, hideDots} = this.props;
    const {show, xAxisNeedZoom, yAxisNeedZoom} = {
      ...axisZoomDefault,
      ...this.props.axisZoom,
    };
    const {show: needTooltip} = this.tooltip;

    if (!convertedData || !convertedData.length) return;

    if (lineGroup && lineGroup !== null) {
      return;
    }
    this.drawAxis();
    const curLineGroup = svgGroup
    .append('g')
    .attr('class', 'd3-line')
    .attr('transform', `translate(${this.padding}, ${this.topPadding})`);
    this.setState(
        {
          lineGroup: curLineGroup,
          transformX: this.xScale,
          transformY: this.yScale,
          transformY2: this.y2Scale,
          brushSelection: [],
          showReset: false,
        },
        async () => {
          !isSimple && this.generateClip(curLineGroup);
          this._drawLine();
          !hideDots && this._drawDot();
          // 是否需要生成hoverLine
          if (needTooltip && (xAxisNeedHoverLine || yAxisNeedHoverLine)) {
            await this.generateHoverLine();
          }
          this._generateHoverCircleGroup();
          this.drawBrush();
          needTooltip && this.hoverEvent();
          needTooltip && this.mouseoutEvent();
          if (!show || !(xAxisNeedZoom || yAxisNeedZoom) || isSimple) return;
          isTimeAxis && this.drawZoom();
        },
    );
  }

  // 转换数据
  async convertData() {
    // 处理横轴labels
    const {data, isTimeAxis = true, needDoubleYAxis} = this.props;
    let {compare} = this.props;
    const labels = [];
    compare = {...compareLineDefaultOptions, ...compare};
    if (!data) return;
    let convertedData;

    if (window.Worker) {
      convertedData = await startWorker({
        data,
        isTimeAxis,
        isCompare: compare.isCompare,
        needDoubleYAxis,
      });
    } else {
      if (data.length > 1) {
        data.forEach(d => {
          d.value.forEach((v: BizData) => {
            isTimeAxis && labels.push(v.label);
            v.yIndex = d.yIndex || 0;
          });
        });
        const axisLabel = isTimeAxis
            ? Array.from(
                new Set(
                    labels.sort(function (a, b) {
                      return convertDate(a) > convertDate(b) ? 1 : -1;
                    }),
                ),
            )
            : labels;

        !compare.isCompare &&
        !needDoubleYAxis &&
        data.forEach(d => {
          axisLabel.forEach(label => {
            let exist = false;
            d.value.forEach(v => {
              if (exist) return;
              if (v.label === label) {
                exist = true;
              }
            });
            if (!exist) {
              d.value.push({
                label,
                value: null,
                yIndex: d.yIndex || 0,
              } as BizData);
            }
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

    // 添加axisNumber用于compare型折线图绘图
    if (compare.isCompare && data.length > 1) {
      const {startTime, endTime} = this.getStartAndEndTime(convertedData, true);
      const period = compare.calculatePeriod(startTime, endTime);
      convertedData.forEach(d => {
        d.value.forEach(v => {
          v.axisNumber = getCompareAxisLabel(d.value[0].label, v.label, period);
        });
      });
    }

    this.setState({
      convertedData: convertedData || data,
    });
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

  // 绘制点
  _drawDot() {
    const {svgGroup, lineGroup, diagramHeight, parentId} = this.state;
    const {xScale, useableData} = this;
    const {pointSize = GraphConfig.defaultPointSize, canvasMode} = this.props;
    let {compare} = this.props;
    const {topPadding} = this;
    compare = {...compareLineDefaultOptions, ...compare};

    lineGroup.raise();
    // 处理legend hover后brush位置变化，无法再次hover问题
    svgGroup.select('g.brush').raise();
    svgGroup.select('g.reset').raise();
    lineGroup.select('g.dots').remove();

    const dot = lineGroup
    .append('g')
    .attr('class', 'dots')
    .selectAll('g.dot')
    .data(useableData)
    .enter()
    .append('g')
    .attr('class', 'dot')
    .attr('transform', d => `translate(0, ${this.dataInY2Axis(d.yIndex) ? diagramHeight / 2 - topPadding : 0})`)
    .attr('fill', d => this.colors[d.name]);

    if (canvasMode) {
      this.convertCirclesToImage(dot);
      return;
    }

    const dotItem = dot
    .selectAll('g.dot-item')
    .data(d => d.value)
    .enter()
    .append('g')
    .attr('class', 'dot-item');

    dotItem
    .append('circle')
    .attr('clip-path', d => (this.dataInY2Axis(d.yIndex) ? `url(#clip-${parentId})` : `url(#clip_y-${parentId})`)) // 防止图形越界
    .attr('r', d => (d.value !== null ? pointSize : 0))
    .attr('cx', d => xScale(compare.isCompare ? d.axisNumber : d.label))
    .attr('cy', d => this.getDataOfYAxis(d));
    // 这部分可以不用
    // .on('mouseover', function () {
    //   d3.select(this).attr('r', pointSize + GraphConfig.pointSizeDiff)
    // })
    // .on('mouseleave', function () {
    //   d3.select(this).attr('r', pointSize)
    // })
    // .transition(this.transition)
  }

  // 绘制折线
  _drawLine() {
    const {lineWidth = 3, isArea, isStack, canvasMode, hideDots} = this.props;
    const {lineGroup, convertedData, diagramHeight, parentId} = this.state;
    const {topPadding, useableData} = this;
    if (!convertedData || !convertedData.length) return;
    const {isCompare} = {
      ...compareLineDefaultOptions,
      ...this.props.compare,
    };

    lineGroup.select('g.chart').remove();

    const chart = lineGroup.append('g').attr('class', 'chart');

    const line = chart
    .selectAll('g.line')
    .data(useableData)
    .enter()
    .append('g')
    .attr('class', 'line')
    .attr('transform', d => `translate(0, ${this.dataInY2Axis(d.yIndex) ? diagramHeight / 2 - topPadding : 0})`)
    //防止图形越界
    .attr('clip-path', d => (this.dataInY2Axis(d.yIndex) ? `url(#clip-${parentId})` : `url(#clip_y-${parentId})`));

    if (canvasMode) {
      // image模式
      this.convertPathToImage(line);
    } else {
      line
      .append('path')
      .attr('fill', 'none')
      .attr('stroke', d => this.colors[d.name])
      .attr('stroke-width', lineWidth)
      .attr('stroke-dasharray', d => d.lineDash || 'none')
      .attr('stroke-linecap', hideDots ? 'round' : 'none')
      .attr('d', d => this.line(d.value));
    }

    const self = this;
    // 添加additionalLine
    line.each(function (d) {
      // 不支持对比折线图&堆叠折线图
      if (isStack || isCompare || !d.additionalLines?.length) return;

      d.additionalLines.forEach(lineValue => {
        const value = self.getAxisLabels().map(v => ({
          label: v,
          value: lineValue.value,
          yIndex: d.yIndex,
        }));
        self._drawAdditionalContent(value, this, lineValue);
      });
    });

    if (!isArea) {
      return;
    }

    const area = chart
    .selectAll('g.area')
    .data(useableData)
    .enter()
    .append('g')
    .attr('class', 'area')
    .attr('transform', d => `translate(0, ${this.dataInY2Axis(d.yIndex) ? diagramHeight / 2 - topPadding : 0})`)
    //防止图形越界
    .attr('clip-path', d => (this.dataInY2Axis(d.yIndex) ? `url(#clip-${parentId})` : `url(#clip_y-${parentId})`));
    if (canvasMode) {
      // image模式
      this.convertPathToImage(area);
    } else {
      area
      .append('path')
      .attr('fill', d => this.colors[d.name])
      .attr('fill-opacity', 0.1)
      .attr('d', d => this.area(d.value));
    }
  }

  convertPathToImage(parent, disableNames = []) {
    const {diagramWidth, diagramHeight, lineGroup} = this.state;
    const {lineWidth = 3, isArea} = this.props;
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
      canvasCtx.beginPath();
      const lineGenerator = this.line.context(canvasCtx);
      lineGenerator(data.value);
      canvasCtx.lineWidth = lineWidth;
      canvasCtx.setLineDash(data.lineDash?.split(' ') || []);
      canvasCtx.strokeStyle =
          disableNames.indexOf(data.name) > -1 ? legendTheme.disabledColor : this.colors[data.name];
      canvasCtx.lineCap = 'round';
      canvasCtx.stroke();

      if (isArea) {
        lineGroup
        .select('g.chart')
        .selectAll('g.line')
        .remove();
        const canvasAreaCtx = canvas.getContext('2d');
        canvasAreaCtx.beginPath();
        canvasAreaCtx.scale(window.devicePixelRatio, window.devicePixelRatio);
        canvasAreaCtx.translate(0, 0);
        const areaGenerator = this.area.context(canvasAreaCtx);
        areaGenerator(data.value);
        canvasAreaCtx.fillStyle = hexToRGB(
            disableNames.indexOf(data.name) > -1 ? legendTheme.disabledColor : this.colors[data.name],
            0.1,
        );

        canvasAreaCtx.fill();
      }
      return canvas.toDataURL();
    })
    .attr('style', `pointer-events:none;mixed-blend-mode:normal;opacity:${1};`);
  }

  convertCirclesToImage(parent, disableNames = []) {
    const {diagramWidth, diagramHeight, transformX} = this.state;
    const {pointSize = GraphConfig.defaultPointSize} = this.props;
    let {compare} = this.props;
    compare = {...compareLineDefaultOptions, ...compare};
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
      canvasCtx.fillStyle = disableNames.indexOf(data.name) > -1 ? legendTheme.disabledColor : this.colors[data.name];
      data.value.forEach(d => {
        if (d.value === null) {
          return;
        }
        canvasCtx.beginPath();
        canvasCtx.arc(
            transformX(compare.isCompare ? d.axisNumber : d.label),
            this.getDataOfYAxis(d),
            pointSize,
            0,
            Math.PI * 2,
            true,
        );
        canvasCtx.fill();
      });
      return canvas.toDataURL();
    });
  }

  _drawAdditionalContent(value, group, additionLine: AdditionalLine) {
    // 单点情况或数据为NaN则不展示
    if (value.length < 2 || isNaN(additionLine.value)) return;

    const {parentId, diagramWidth, transformY, transformY2, diagramHeight} = this.state;
    const {paddingXForHead = DefaultPaddingXForHead} = this.props;
    const line = d3
    .select(group)
    .append('g')
    .attr('class', 'additional-line');
    line
    .append('path')
    .attr('clip-path', d => (this.dataInY2Axis(d.yIndex) ? `url(#clip-${parentId})` : `url(#clip_y-${parentId})`)) //防止图形越界
    .attr('fill', 'none')
    .attr('stroke', d => additionLine.color || this.colors[d.name])
    .attr('stroke-width', additionLine.lineWidth || 1)
    .attr('stroke-dasharray', additionLine.lineDash || '3 5')
    .attr('d', this.line(value));

    if (!additionLine.desc) return;
    // 描述
    const text = line
    .append('text')
    .text(additionLine.desc)
    .attr('fill', d => additionLine.color || this.colors[d.name])
    .attr('style', additionLine.textStyle || 'font-size: 12px');

    text.attr('transform', d => {
      const valueY = this.dataInY2Axis(d.yIndex)
          ? transformY2(additionLine.value)
          : transformY(additionLine.value) || 0;

      const offsetX = additionLine.formatterOffsetX
          ? additionLine.formatterOffsetX(nodeWidth(text.node()), diagramWidth, this.padding)
          : paddingXForHead;
      const offsetY = additionLine.formatterOffsetY
          ? additionLine.formatterOffsetY(nodeHeight(text.node()), valueY, diagramHeight, this.padding)
          : valueY - 5;
      return `translate(${offsetX}, ${offsetY})`;
    });
  }

  // 生成坐标轴Hover line
  generateHoverLine() {
    const {lineGroup, diagramWidth, convertedData, parentId, yGroup} = this.state;
    const {xAxisNeedHoverLine = true, yAxisNeedHoverLine} = this.props;

    const hoverLineContainer = lineGroup.append('g').attr('class', 'hover-container');

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
    const {mouseoutCallback} = this.tooltip;
    const self = this;

    this.overlayRect.on('mouseout', function () {
      self.clearTooltip();
      mouseoutCallback && mouseoutCallback();
    });
  }

  clearTooltip() {
    const {xAxisNeedHoverLine = true, yAxisNeedHoverLine} = this.props;
    const {xHoverContainer, yHoverContainer, svgGroup, parentId} = this.state;
    const {show: needTooltip} = this.tooltip;
    const tooltip = document.querySelector(`#${parentId} .tooltip`);
    needTooltip && tooltip && tooltip.setAttribute('style', 'display: none');
    // 隐藏hover line
    xAxisNeedHoverLine && xHoverContainer.attr('opacity', 0);
    yAxisNeedHoverLine && yHoverContainer && yHoverContainer.attr('opacity', 0);
    svgGroup.select('g.hover-circles').attr('opacity', 0);
  }

  // 处理Tooltip

  // 取hover时最靠近的点
  bisect(mx, data, key) {
    const {isTimeAxis = true} = this.props;
    const {transformX} = this.state;
    if (isTimeAxis) {
      const date = transformX.invert(mx);
      const index = d3.bisector(d => d[key]).left(data, date, 1);
      const a = data[index - 1];
      const b = data[index];
      // 容错处理
      if (!a || !b) return a || b;

      return date - a[key] > b[key] - date ? b : a;
    } else {
      const arr = data.map(d => transformX(d.label));
      const index = this.closest(mx, arr);

      return data[index];
    }
  }

  // 悬停回调函数
  hoverEvent() {
    const {diagramWidth} = this.state;
    const {pointSize = GraphConfig.defaultPointSize} = this.props;
    const {padding, topPadding, height} = this;
    const {mouseHoverCallback} = this.tooltip;
    const self = this;
    const overlay = this.overlayRect
    .attr('width', diagramWidth - padding * 2 + pointSize * 2)
    .attr('height', height + pointSize * 2)
    .attr('transform', `translate(${padding - pointSize},${topPadding - pointSize})`);

    overlay.on('mousemove', function () {
      // eslint-disable-next-line @typescript-eslint/camelcase
      const [mouse_x, mouse_y] = d3.mouse(this);
      mouseHoverCallback && mouseHoverCallback(mouse_x, mouse_y);
      self.mouseHoverHandler(mouse_x, mouse_y);
    });
  }

  // eslint-disable-next-line @typescript-eslint/camelcase
  public mouseHoverHandler(mouse_x, mouse_y) {
    const {
      convertedData,
      xHoverContainer,
      yHoverContainer,
      svgGroup,
      diagramWidth,
      transformX,
      transformY,
      transformY2,
    } = this.state;
    if (!svgGroup) return;
    const {
      isTimeAxis = true,
      xAxisNeedHoverLine = true,
      yAxisNeedHoverLine,
      pointSize = GraphConfig.defaultPointSize,
      needDoubleYAxis,
      paddingXForHead = DefaultPaddingXForHead,
      paddingXForTail = DefaultPaddingXForTail,
    } = this.props;
    let {compare} = this.props;
    const {height, formateTime} = this;
    const {show: needTooltip} = this.tooltip;
    const self = this;

    compare = {...compareLineDefaultOptions, ...compare};
    const key = compare.isCompare ? 'axisNumber' : 'label';

    this.clearTooltip();

    const hoverData = [];

    // 考虑到多条数据情况，对convertedData进行循环处理
    convertedData.forEach(d => {
      // 处理legend disable时的情况
      if (self.state.legendDisableKeys.indexOf(d.name) > -1) {
        return;
      }
      const data = self.bisect(
          mouse_x,
          d.value.filter(v => self.dataInBrushSelection(v)),
          key,
      );
      if (!data) return;
      const {value, yIndex, label, tipLabel, tipValue} = data;

      // 保存当前label数据
      hoverData.push({
        ...d,
        yIndex,
        value,
        x: transformX(data[key]),
        label,
        tipLabel,
        tipValue,
      });

      if (value === null) return;

      if (yAxisNeedHoverLine && yHoverContainer) {
        yHoverContainer
        .select('line.hover-line-y')
        .attr('y1', transformY(value))
        .attr('y2', transformY(value));
        yHoverContainer.attr('opacity', 1);
      }
    });

    // 获取最近的点
    const tmpData = [...hoverData];
    // eslint-disable-next-line @typescript-eslint/camelcase
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
    .attr('r', ({value}) => (value !== null ? lineTheme.hoverPointSize : 0))
    .attr('fill', '#fff')
    .attr('stroke', d => self.colors[d.name])
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
          transformY(value) < -pointSize || transformY(value) > (needDoubleYAxis ? height / 2 : height) + pointSize;

      const y2IsOut = transformY2(value) < -pointSize || transformY2(value) > height / 2 + pointSize;

      return xIsOut || (self.dataInY2Axis(yIndex) ? y2IsOut : yIsOut) ? 0 : 1;
    })
    .attr('transform', data => {
      const {yIndex, x} = data;

      const y = self.getDataOfYAxis(data, transformY, transformY2);
      return `translate(${x}, ${self.dataInY2Axis(yIndex) ? y + height / 2 : y})`;
    });

    // 处理legend 事件后，无法再次hover问题
    svgGroup.select('g.brush').raise();
    svgGroup.select('g.reset').raise();

    if (!needTooltip) return;
    // 处理Tooltip数据
    self.tooltipCallback(
        Array.from(
            new Set(
                hoverData.filter(d => d.value !== null).map(d => d.tipLabel || (isTimeAxis ? formateTime(d.label) : d.label)),
            ),
        ),
        hoverData.filter(d => d.value !== null),
        mouse_x,
        mouse_y,
    );
  }

  // eslint-disable-next-line @typescript-eslint/camelcase
  tooltipCallback(labels, hoverData, mouse_x, mouse_y) {
    const {diagramHeight, diagramWidth, parentId} = this.state;
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
      this.clearTooltip();
      return;
    }

    tooltip.innerHTML = formatter
        ? generateCustomTooltip(formatter, hoverData, this.colors)
        : generateLineTooltip(labels, hoverData, this.colors, additionalTip, showSplitLine);

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
      lineGroup,
      svgGroup,
      transformX,
      transformY,
      transformY2,
      brushSelection,
    } = this.state;
    const {
      isArea,
      xAxisNeedHoverLine = true,
      yAxisNeedHoverLine,
      needDoubleYAxis,
      isStack,
      canvasMode,
      hideDots,
    } = this.props;
    const {line, xAxis, area, yAxis, y2Axis} = this;
    const {show: needTooltip} = this.tooltip;
    const compare = {...compareLineDefaultOptions, ...this.props.compare};
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

    const key = compare.isCompare ? 'axisNumber' : 'label';
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
      this.clearTooltip();
      this.hoverEvent();
    }

    // 处理hover circle
    svgGroup.select('g.hover-circles').attr('opacity', 0);

    if (!hideDots) {
      const circleG = lineGroup.select('g.dots');
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
        const circles = circleG.selectAll('g.dot-item').selectAll('circle');
        circles.attr('cx', function (d) {
          return transformX(d[key]);
        });
        circles.attr('cy', d => (this.dataInBrushSelection(d) ? this.getDataOfYAxis(d) : 0));
        circles.attr('opacity', d => (this.dataInBrushSelection(d) ? 1 : 0));
      }
    }
    //缩放&移动折线
    const lineG = lineGroup.select('g.chart').selectAll('g.line');

    if (canvasMode) {
      lineG.selectAll('image').remove();
      this.convertPathToImage(lineG);
    } else {
      lineG.select('path').attr('d', d => {
        line.x(function (v) {
          return transformX(v[key]);
        });

        line.y(v => this.getDataOfYAxis(v));

        // 处理brush 选区
        return line(d.value.filter(v => this.dataInBrushSelection(v)));
      });
    }

    lineG.selectAll('g.additional-line').remove();
    // 添加additionalLine
    lineG.each(function (d) {
      // 不支持对比折线图&堆叠折线图
      if (isStack || compare.isCompare || !d.additionalLines?.length) return;

      d.additionalLines.forEach(lineValue => {
        // 缩放&重置情况
        const value = (brushSelection.length ? brushSelection : self.getAxisLabels()).map(v => ({
          label: v,
          value: lineValue.value,
          yIndex: d.yIndex,
        }));
        self._drawAdditionalContent(value, this, lineValue);
      });
    });

    // 重新添加坐标轴样式
    addAxisStyle(this);

    if (xAxisNeedSplitLine || yAxisNeedSplitLine) {
      // 重新处理坐标轴分割线
      svgGroup.selectAll('line.split-line').remove();
      addSplitLine(this);
    }

    if (!isArea) return;
    const areaG = lineGroup.select('g.chart').selectAll('g.area');
    if (canvasMode) {
      areaG.selectAll('image').remove();
      this.convertPathToImage(areaG);
    } else {
      areaG.select('path').attr('d', d => {
        area.x(function (v) {
          return transformX(v[key]);
        });

        area.y1(v => {
          return this.getDataOfYAxis(v);
        });

        // 处理堆叠数据
        isStack &&
        area.y0(d => {
          const beforeDatas = isStack ? this.getBeforeValue(d) : [];
          return this.dataInY2Axis(d.yIndex)
              ? beforeDatas.reduce((pre, cur) => pre + cur, 0)
              : beforeDatas.length
                  ? beforeDatas.reduce((pre, cur) => pre + cur, 0)
                  : transformY(0);
        });
        return area(d.value.filter(v => this.dataInBrushSelection(v)));
      });
    }
  }

  // hover legend
  onLegendItemHover({name}) {
    const {lineGroup} = this.state;
    const {colors, useableData} = this;
    const {isArea, canvasMode, hideDots} = this.props;
    const self = this;

    // 处理line
    lineGroup
    .select('g.chart')
    .selectAll('g.line')
    .call(g => {
      g.each(function highlight({name: gName}) {
        const ele = d3.select(this);
        if (gName === name) {
          ele.raise();
        }
        if (canvasMode) {
          self.convertPathToImage(
              g,
              useableData?.filter(d => d.name !== name)?.map(d => d.name),
          );
          return;
        }
        if (gName === name) {
          ele
          .select('path')
          .transition()
          .attr('stroke', colors[name])
          .ease();
        } else {
          ele
          .select('path')
          .transition()
          .attr('stroke', legendTheme.disabledColor)
          .ease();
        }
      });
    });

    // 处理area
    isArea &&
    lineGroup
    .select('g.chart')
    .selectAll('g.area')
    .call(g => {
      g.each(function highlight({name: gName}) {
        const ele = d3.select(this);
        if (gName === name) {
          ele.raise();
        }
        if (canvasMode) {
          self.convertPathToImage(
              g,
              useableData?.filter(d => d.name !== name)?.map(d => d.name),
          );
          return;
        }
        if (gName === name) {
          ele.select('path').attr('fill', colors[name]);
        } else {
          ele.select('path').attr('fill', legendTheme.disabledColor);
        }
      });
    });

    if (hideDots) {
      return;
    }

    // 处理dots
    lineGroup
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
          ele.attr('fill', colors[name]);
        } else {
          ele.attr('fill', legendTheme.disabledColor);
        }
      });
    });
  }

  // 移出
  onLegendItemLeave() {
    // 还原line & area & dot
    this._drawLine();
    !this.props.hideDots && this._drawDot();
    this.handlerZoom();
  }

  // legend click
  onLegendItemClick(data) {
    const {isTimeAxis = true, isSimple} = this.props;
    super.onLegendItemClick(data);
    if (isSimple) return;

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
    const {convertedData, period} = this.state;
    let {compare} = this.props;
    compare = {...compareLineDefaultOptions, ...compare};

    const {startTime, endTime} = this.getStartAndEndTime(convertedData, compare.isCompare);

    const diffTime = (endTime - startTime) / 1000 / 10;

    // 缩放粒度
    return diffTime / (compare.isCompare ? period : 60);
  }

  convertStackData(data) {
    const {isTimeAxis = true} = this.props;
    const {formateTime} = this;
    // 处理data，按 label进行分类
    const stackData = [] as Array<Data>;
    data.forEach(data => {
      data.value.forEach(v => {
        const label = isTimeAxis ? formateTime(v.label) : v.label;
        const item = stackData.find(d => d.name === label);
        if (item) {
          item.value.push({...v, label: data.name});
          return;
        }
        stackData.push({
          name: label,
          value: [{...v, label: data.name}],
        });
      });
    });
    return stackData;
  }

  // 堆叠图：获取同一坐标前一数据
  getBeforeValue(curData, yScale = this.state.transformY, y2Scale = this.state.transformY2) {
    const {useableData} = this;
    const {needDoubleYAxis, isTimeAxis = true} = this.props;
    const isNegative = curData.value < 0;

    const stackData = this.convertStackData(useableData);
    const values = (stackData.find(d => d.name === (isTimeAxis ? this.formateTime(curData.label) : curData.label))
        .value as Array<BizData>).filter(
        v => (isNegative ? v.value < 0 : v.value >= 0) && (!needDoubleYAxis || v.yIndex === curData.yIndex),
    );

    const index = values.findIndex(v => v.label === curData.name);

    const beforeDatas = values.slice(0, index);

    return beforeDatas.map(v => (this.dataInY2Axis(v.yIndex) ? y2Scale(v.value) : yScale(v.value)));
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
    let {compare} = this.props;
    const {convertedData} = this.state;
    compare = {...compareLineDefaultOptions, ...compare};

    const {startTime: start, endTime: end} = this.getStartAndEndTime(convertedData, compare.isCompare);

    let scale = isTimeAxis ? d3.scaleTime().domain([start, end]) : d3.scalePoint().domain(this.getAxisLabels());

    if (compare.isCompare) {
      const duration = (end - start) / 1000;
      const period = compare.calculatePeriod(start, end);
      this.setState({
        duration,
        period,
      });
      // 对比图
      scale = d3.scaleLinear().domain([0, duration / period]);
    }
    return scale;
  }
}
