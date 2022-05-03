/* eslint-disable prettier/prettier */
/* eslint-disable react/no-deprecated */
import * as d3 from 'd3';
import * as React from 'react';
import {BaseProps, LegendDefault, Title, TitleDefault, TooltipDefault} from '../../type/chart';
import {GraphConfig, legendTheme, originColors, titleTheme} from '../../theme/theme';
import {
  calIconTooltipPos,
  generateParentId,
  judgeTopCondition,
  nodeHeight,
  nodeWidth
} from '../lib/helper';
import {drawToolBar} from '../toolbar/toolbar';
import {Bubble, Icon} from 'tea-component';
import throttle from '../lib/throttle';

let resizeHandler;

export default abstract class Base<T> extends React.Component<T & BaseProps, any> {
  constructor(props) {
    super(props);
    this.state = {
      // 图形高度
      diagramWidth: 0,
      // 图形宽度
      diagramHeight: 450,
      svgGroup: null,
      svgSelection: null,
      legendDisableKeys: [],
      noData: true,
      parentId: '',
      isAmplify: true,
    };
    this.reload = this.reload.bind(this);
  }

  // 获取可展示的数据
  get useableData() {
    const {convertedData} = this.state;
    const {legendDisableKeys} = this;

    const filterData = convertedData.filter(d => legendDisableKeys.indexOf(d.name) < 0);
    filterData.forEach(d => {
      d.value.forEach(v => {
        v.name = d.name;
      });
    });
    return filterData;
  }

  // 颜色配置
  get colors() {
    const {colors: propColors} = this.props;
    const {convertedData} = this.state;
    const dataColors = {};
    // 根据name来设定颜色
    convertedData.forEach((d, i) => {
      dataColors[d.name] = (propColors || originColors)[i];
    });
    return dataColors;
  }

  get legendDisableKeys() {
    return this.state.legendDisableKeys;
  }

  set legendDisableKeys(keys) {
    this.setState({
      legendDisableKeys: keys,
    });
  }

  // 缩放
  get zoom() {
    const {diagramWidth, diagramHeight} = this.state;
    return d3
    .zoom()
    .scaleExtent([1, this.getScaleExtent()]) //缩放比例范围
    .translateExtent([
      [0, 0],
      [diagramWidth, diagramHeight],
    ]);
  }

  // hover rect
  get overlayRect() {
    const {diagramHeight, diagramWidth, svgGroup} = this.state;
    const {padding} = this;
    const overlay = svgGroup.select('rect.overlay');
    if (!overlay.empty()) return overlay.raise();
    return svgGroup
    .append('rect')
    .attr('class', 'overlay')
    .attr('width', diagramWidth - padding * 2)
    .attr('height', diagramHeight - padding * 2)
    .attr('opacity', 0);
  }

  // legend
  get legendGroup() {
    const {svgGroup, diagramHeight} = this.state;
    const legend = svgGroup.select('g.legend');
    if (!legend.empty()) return legend;
    return svgGroup
    .append('g')
    .attr('class', 'legend')
    .attr('transform', `translate(0, ${diagramHeight - 10})`);
  }

  // 动画
  get transition() {
    return d3
    .transition()
    .duration(1000)
    .ease(d3.easeLinear);
  }

  get padding() {
    return GraphConfig.PADDING;
  }

  get topPadding() {
    return judgeTopCondition(this) ? GraphConfig.PADDING : GraphConfig.PADDING / 2;
  }

  get bottomPadding() {
    const {show} = this.legendOptions;
    const {legendGroup} = this;

    const padding = GraphConfig.PADDING / 2;

    const legendHeight = nodeHeight(legendGroup.node());

    return show ? padding + (legendHeight > padding ? legendHeight : padding) : padding;
  }

  get height() {
    const {diagramHeight} = this.state;
    return diagramHeight - (this.topPadding + this.bottomPadding);
  }

  get titleOpt() {
    const {title} = this.props;
    let titleOpt = {...TitleDefault} as Title;

    if (typeof title === 'string') {
      titleOpt.text = title;
    } else {
      titleOpt = {...titleOpt, ...(title as Title)};
    }

    return titleOpt;
  }

  get tooltip() {
    const {tooltip, needTooltip} = this.props;

    const additionalTip = tooltip?.additionalTip
        ? typeof tooltip.additionalTip === 'string'
            ? [tooltip.additionalTip]
            : tooltip.additionalTip
        : [];

    const tool = {...TooltipDefault, ...tooltip, additionalTip};
    return {
      ...tool,
      show: typeof needTooltip === 'boolean' ? needTooltip : tool.show,
    };
  }

  get legendOptions() {
    return {...LegendDefault, ...this.props.legend};
  }

  componentDidMount() {
    const parentId = generateParentId('base');
    const {threshold} = this.props;
    if (!resizeHandler) {
      resizeHandler = throttle(this.reload, threshold || 300);
    }
    this.setState({parentId}, () => {
      this.initEvents();
      this.init();
      this.handleEmptyData(this.props.data);
    });

    // 设置默认的legend disable key
    const {disabledKeys = []} = {...this.props.legend};
    this.legendDisableKeys = disabledKeys;
  }

  handleEmptyData(data) {
    const {parentId} = this.state;
    let noData = true;
    if (!(data instanceof Array)) {
      noData = !data;
    } else {
      noData = !(data && data.length);
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

  componentWillReceiveProps(nextProps) {
    // 页面渲染容错处理
    const {parentId} = this.state;
    if (
        (nextProps.data !== this.props.data ||
            nextProps.loading !== this.props.loading ||
            nextProps.silentReload !== this.props.silentReload) &&
        parentId
    ) {
      d3.select(`#${parentId}`).style('display', nextProps.loading && !nextProps.silentReload ? 'none' : 'block');
      if (nextProps.loading && !nextProps.silentReload) return;
      this.handleEmptyData(nextProps.data);
      // 重置disable
      this.legendDisableKeys = [];
    }
  }

  componentWillUnmount() {
    const {resizeNeedReload} = this.props;
    this.dispose();
    this.legendDisableKeys = [];
    // 销毁事件
    resizeNeedReload && window.removeEventListener('resize', resizeHandler);
  }

  // 销毁
  dispose() {
    const {svgGroup} = this.state;
    if (svgGroup !== null) {
      svgGroup.selectAll('g').remove();
    }
  }

  // 初始化
  init() {
    const {toolBar = {show: true}} = this.props;
    const {diagramWidth, diagramHeight, parentId} = this.state;

    let chart = d3.select(`#${parentId}`).select('section.chart');

    if (chart.empty()) {
      chart = d3
      .select(`#${parentId}`)
      .append('section')
      .attr('class', 'chart')
      .attr('id', parentId);
    }

    // 生成画布
    const svgSelection = chart
    .append('svg')
    .attr('width', diagramWidth)
    .attr('height', diagramHeight)
    .on('click', function () {
      const toolbar = document.querySelector('.toolbar');
      toolBar.show && toolbar && toolbar.setAttribute('style', 'display: none');
    });

    const svgGroup = svgSelection
    .append('g')
    .attr('class', 'svg-container')
    .attr('cursor', 'default');

    this.setState({
      svgSelection,
      svgGroup,
    });
  }

  // 初始化事件
  initEvents() {
    const {resizeNeedReload} = this.props;
    // 可以重写
    resizeNeedReload && window.addEventListener('resize', resizeHandler);
  }

  _resize() {
    const {parentId, noData} = this.state;

    if (noData) return;

    const container = document.querySelector(`#${parentId}`);
    // 由于考虑到屏幕适配的问题，所以根据父级的container宽度和高度动态的改变图层的大小
    const width = container ? Math.max(GraphConfig.MIN_WIDTH, container.clientWidth) : GraphConfig.MIN_WIDTH;
    const height = this.props.height ? this.props.height : GraphConfig.HEIGHT;

    this.setState(
        {
          diagramHeight: height,
          diagramWidth: width,
        },
        async () => {
          await this.convertData();
          this.updateSvgSelection();
        },
    );
  }

  // 处理原生数据
  abstract convertData();

  // 更新svg 宽高
  updateSvgSelection(
      {width, height} = {
        width: this.state.diagramWidth,
        height: this.state.diagramHeight,
      },
  ) {
    const {svgSelection, noData} = this.state;
    const {show} = this.legendOptions;

    if (noData) return;

    svgSelection
    .attr('width', width)
    .attr('height', height)
    .attr('preserveAspectRatio', 'xMinYMax meet');

    this.setState(
        {
          svgSelection,
        },
        () => {
          this._drawTitle();
          drawToolBar(this);
          show && this.drawLegend();
          this.drawChart();
        },
    );
  }

  // 渲染title
  _drawTitle() {
    const {svgGroup, parentId} = this.state;
    const {title} = this.props;
    if (!title) return;

    const {titleOpt} = this;

    // 绘制title
    const tileG = svgGroup
    .selectAll('g.title-g')
    .data([null])
    .join('g')
    .attr('class', 'title-g')
    // 3为toolBar icon padding大小
    .attr('transform', `translate(0, ${GraphConfig.MINI_PADDING + 3})`);

    const titleText = tileG
    .selectAll('text.title')
    .data([null])
    .join('text')
    .text(titleOpt.text)
    .attr('class', 'title')
    .attr('font-size', titleOpt.titleFontSize)
    .attr('font-weight', titleTheme.fontWeight)
    .attr('fill', titleOpt.titleColor);

    // 描述
    titleOpt.desc &&
    tileG
    .selectAll('text.desc')
    .data([null])
    .join('text')
    .text(titleOpt.desc)
    .attr('class', 'desc')
    .attr('transform', `translate(${nodeWidth(titleText.node())}, 0)`)
    .attr('font-size', titleOpt.descFontSize)
    .attr('fill', titleOpt.descColor);

    // bubble
    if (!titleOpt.showBubble) return;

    // 添加icon
    svgGroup
    .selectAll('g.title-icon')
    .data([null])
    .join('g')
    .attr('class', 'title-icon')
    .attr('transform', `translate(${nodeWidth(tileG.node()) + 5}, 4)`)
    .append('foreignObject')
    .attr('width', 18)
    .attr('height', 18)
    .html(`<i class="tea-icon tea-icon-${titleOpt.iconType}"></i>`)
    .on('mouseover', function () {
      const tooltipContainer = d3
      .select(`#${parentId}`)
      .select('section.chart')
      .selectAll('div.tooltip-icon')
      .data([null])
      .join('div')
      .attr('class', 'tooltip-icon');

      const tooltip = document.querySelector(`#${parentId} .tooltip-icon`);

      tooltip.innerHTML = titleOpt.tipContent
          ? `<div class="tea-chart-tooltip" style="position: absolute; padding: 15px; z-index: 98; background: rgb(255, 255, 255); white-space: nowrap; box-shadow: rgba(0, 0, 0, 0.15) 0px 4px 8px 0px; right: unset;">${titleOpt.tipContent}</div>`
          : '';

      calIconTooltipPos(tooltipContainer, nodeWidth(tileG.node()) + 25, 0);
    })
    .on('mouseleave', function () {
      const tooltip = document.querySelector(`#${parentId} .tooltip-icon`);
      tooltip && tooltip.remove();
    });
  }

  // 渲染legend
  drawLegend(data = this.state.convertedData) {
    const {diagramWidth, diagramHeight} = this.state;
    this.legendGroup.selectAll('.legend-item').remove();

    const items = this.legendGroup.selectAll('.legend-item').data(data);

    const enter = items
    .join('g')
    .attr('class', 'legend-item')
    .style('cursor', 'pointer')
    .call(g => {
      g.append('rect')
      .attr('width', legendTheme.rectWidth)
      .attr('height', legendTheme.rectHeight)
      .attr('y', -legendTheme.fontSize / 2);

      g.append('text')
      .attr('x', legendTheme.rectWidth + 5)
      .attr('font-size', `${legendTheme.fontSize}px`)
      .text(d => d.name);
    });

    let xOffset = 0; // 每个图例的 X 偏移量
    let yOffset = 0;
    items
    .merge(enter)
    .attr('transform', function setItemOffset() {
      const x = xOffset;
      const itemWidth = nodeWidth(this as SVGGraphicsElement) + 30;
      xOffset += itemWidth;
      if (xOffset > diagramWidth) {
        xOffset = itemWidth;
        yOffset += legendTheme.fontSize * 2;
        return `translate(0, ${yOffset})`;
      }
      return `translate(${x}, ${yOffset})`;
    })
    .on('mouseenter', this.onLegendItemHover.bind(this))
    .on('mouseleave', this.onLegendItemLeave.bind(this))
    .on('click', this.onLegendItemClick.bind(this))
    .call(g => {
      g.selectAll('rect').attr('fill', d =>
          this.legendDisableKeys.find(name => name === d.name) ? legendTheme.disabledColor : this.colors[d.name],
      );
      g.selectAll('text').attr('fill', d =>
          this.legendDisableKeys.find(k => k === d.key) ? legendTheme.disabledColor : legendTheme.color,
      );
    });

    // 设置legend居中
    const legendWidth = nodeWidth(this.legendGroup.node());
    const legendHeight = nodeHeight(this.legendGroup.node());
    const x = Math.floor(diagramWidth / 2) - Math.floor(legendWidth / 2);
    this.legendGroup.attr('transform', () => {
      return `translate(${x}, ${diagramHeight - legendHeight + 10})`;
    });
  }

  onLegendItemHover() {
  }

  onLegendItemLeave() {
  }

  onLegendItemClick(legendData) {
    (d3 as any).event.stopPropagation();
    const {name} = legendData;
    const disabled = this.legendDisableKeys.findIndex(k => k === name) !== -1;
    if (disabled) {
      this.legendDisableKeys = this.legendDisableKeys.filter(k => k !== name);
    } else {
      this.legendDisableKeys.push(name);
    }
    this.drawLegend();
    this.onLegendItemLeave();
  }

  // 绘图
  abstract drawChart();

  // 添加缩放效果
  drawZoom() {
  }

  // 配置缩放大小-可重写
  getScaleExtent() {
    return 1;
  }

  formateTime(data) {
    return d3.timeFormat('%Y-%m-%d %H:%M:%S')(data);
  }

  // 刷新
  reload() {
    this._resize();
  }

  render() {
    const {noData, parentId} = this.state;
    const {emptyContent, height, loading, position = 'relative', silentReload} = this.props;
    const {titleOpt} = this;
    /* Tooltip根据section定位 */
    const tipHeight = height || GraphConfig.HEIGHT;
    return (
        parentId && (
            <>
              <section id={parentId} style={{height}}>
                <section style={{position: position as any}} className='chart'/>
              </section>
              {!loading && noData && (
                  <section style={{height: tipHeight}}>
                    {titleOpt.text && (
                        <p>
                  <span
                      style={{
                        fontSize: titleOpt.titleFontSize,
                        color: titleOpt.titleColor,
                        fontWeight: titleTheme.fontWeight,
                      }}
                  >
                    {titleOpt.text}
                  </span>
                          {titleOpt.desc && (
                              <span
                                  style={{
                                    fontSize: titleOpt.descFontSize,
                                    color: titleOpt.descColor,
                                  }}
                              >
                      {titleOpt.desc}
                    </span>
                          )}
                          {titleOpt.showBubble && (
                              <Bubble content={titleOpt.tipContent} placement='right'>
                                <Icon type={titleOpt.iconType} style={{marginLeft: 5}}/>
                              </Bubble>
                          )}
                        </p>
                    )}
                    {emptyContent ? (
                        emptyContent
                    ) : (
                        <p
                            style={{
                              textAlign: 'center',
                              padding: `${(tipHeight - 14) / 2}px 0`,
                            }}
                        >
                          暂无数据
                        </p>
                    )}
                  </section>
              )}
              {loading && !silentReload && (
                  <section style={{height: tipHeight}}>
                    {titleOpt.text && (
                        <p>
                  <span
                      style={{
                        fontSize: titleOpt.titleFontSize,
                        color: titleOpt.titleColor,
                        fontWeight: titleTheme.fontWeight,
                      }}
                  >
                    {titleOpt.text}
                  </span>
                          {titleOpt.desc && (
                              <span
                                  style={{
                                    fontSize: titleOpt.descFontSize,
                                    color: titleOpt.descColor,
                                  }}
                              >
                      {titleOpt.desc}
                    </span>
                          )}
                          {titleOpt.showBubble && (
                              <Bubble content={titleOpt.tipContent} placement='right'>
                                <Icon type={titleOpt.iconType} style={{marginLeft: 5}}/>
                              </Bubble>
                          )}
                        </p>
                    )}
                    <p
                        style={{
                          textAlign: 'center',
                          padding: `${(tipHeight - 14) / 2}px 0`,
                        }}
                    >
                      <i className='tea-icon tea-icon-loading' style={{marginRight: 5}}/>
                      加载中
                    </p>
                  </section>
              )}
            </>
        )
    );
  }
}
