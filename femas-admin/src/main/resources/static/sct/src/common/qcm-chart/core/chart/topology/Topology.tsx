/* eslint-disable prettier/prettier */
// @ts-nocheck
import * as React from 'react';
import {circleColors, labelColors, lineColors, TopoTheme} from '../../../theme/theme';
import {
  calculateRange,
  calNodeSize,
  convertImgToBase64,
  convertNodeId,
  getLinkPoints
} from '../../lib/topology';
import {generateParentId, nodeHeight, nodeWidth} from '../../lib/helper';
import {Icon, Text} from 'tea-component';
import moment from 'moment';
import dagre from 'dagre';
import {DataType, LayoutType, TopologyProps} from '../../../type/topology';
import throttle from '../../lib/throttle';
import * as d3 from 'd3';

export const TIME_LIMIT = 200;
let scaleChangeHandler;

export default class TraceChart extends React.Component<TopologyProps, any> {
  state = {
    // 图形高度
    diagramWidth: 0,
    // 图形宽度
    diagramHeight: 450,
    svgSelection: null,
    // 转换后的数据
    nodes: [],
    links: [],
    svgGroup: null,
    // 保存点击后的数据
    curData: {},
    curScale: TopoTheme.defaultScale, // 保存当前缩放值
    parentId: '',
  };

  get nodeColors() {
    const {nodeColors = []} = this.props;
    return nodeColors?.length ? nodeColors : circleColors;
  }

  get linkColors() {
    const {linkColors = []} = this.props;
    return linkColors?.length ? linkColors : lineColors;
  }

  get labelColors() {
    const {labelColors: propsColors} = this.props;
    return propsColors?.length ? propsColors : labelColors;
  }

  get svgClickEvent() {
    const {svgClickEvent} = this.props;
    return {
      clickCallback: svgClickEvent?.clickCallback,
      initHightlight: svgClickEvent?.initHightlight ?? true,
      eventBreak: svgClickEvent?.eventBreak,
    };
  }

  componentDidMount() {
    // 生成parentId
    const parentId = generateParentId('topology');
    if (scaleChangeHandler && this.props.onScaleChange) {
      scaleChangeHandler = throttle(this.props.onScaleChange, 3000);
    }
    this.setState(
        {
          parentId,
        },
        () => {
          this._initSvgSelection();
          this.reload();
          this._initEvent();
        },
    );
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    if (!nextProps.data || nextProps.loading) {
      this._destoryTooltip();
      this._destory();
      this._toggleSvgDisplay(false);
      return;
    }

    if (nextProps.nodeCalMode !== this.props.nodeCalMode) {
      this._convertData(nextProps, () => {
        this.drawChart();
      });
      return;
    }

    // 支持外部缩放
    if (this.props.topoScale?.toFixed(2) !== nextProps.topoScale?.toFixed(2)) {
      // 缩放更新
      const scale = +nextProps.topoScale?.toFixed(2);
      if (scale <= TopoTheme.maxZoom && scale >= TopoTheme.minZoom) {
        // 获取当前画布的translate值
        const transform = this.state.svgGroup?.attr('transform') || '';
        const translate = transform.substring(transform.indexOf('(') + 1, transform.indexOf(')')).split(',');

        this._resetTransform(scale, +translate?.[0] || 0, +translate?.[1] || 0);
        return;
      }
    }

    if (
        JSON.stringify(nextProps.data) === JSON.stringify(this.props.data) &&
        JSON.stringify(nextProps.curData) === JSON.stringify(this.props.curData) &&
        nextProps.maxHeight === this.props.maxHeight &&
        nextProps.layoutType === this.props.layoutType
    ) {
      return;
    }

    // 切换maxHeight时将curData置空
    // 改变curData销毁Tooltip，避免数据不一致问题
    this._convertPropsCurData(nextProps.maxHeight !== this.props.maxHeight ? {} : nextProps.curData);
    this.reload(nextProps, nextProps.maxHeight !== this.props.maxHeight);
  }

  // 处理props curData
  _convertPropsCurData(curData) {
    const {nodes, links} = this.state;
    const {curSource, curTarget} = curData || {};
    const {hightlightSingle} = this.props;
    let curNode = {} as { x: number; y: number };
    if (!curTarget && !curSource) {
      this._saveData({});
      return;
    }
    if (curTarget && curSource) {
      // 边
      let data = {} as any;
      links.forEach(linkData => {
        if (curTarget !== linkData?.targetId || curSource !== linkData?.sourceId) {
          return;
        }
        data = {...linkData, dataType: DataType.link};
        const {source, target} = linkData;

        // 将连线上x最小值定位在画布中间
        curNode = target.x > source.x ? source : target;
      });
      this._locateSvgGroup(curNode);
      // 如果没有这条边，则将curTarget&curSource两个节点高亮
      // 保存类型为'single'
      this._saveData(data.dataType ? data : {nodes: [curTarget, curSource], dataType: 'single'});
      return;
    }
    if (curTarget) {
      // 被调用者，高亮其parents
      nodes.forEach(node => {
        if (curTarget !== node.id) {
          return;
        }
        curNode = node;
        this._saveData({
          ...node,
          highlightType: hightlightSingle && 'target',
          dataType: DataType.node,
        });
      });
    }
    if (curSource) {
      // 调用者，高亮其children
      links.forEach(linkData => {
        if (linkData.sourceId !== curSource) return;
        // 将child节点高亮
        curNode = linkData.source;
        this._saveData({
          ...curNode,
          highlightType: hightlightSingle && 'source',
          dataType: DataType.node,
        });
      });
    }
    this._locateSvgGroup(curNode);
  }

  // 定位画布，将当前高亮数据居中
  _locateSvgGroup(node) {
    const {diffTransformCalc} = this.props;
    const {curScale, diagramHeight, diagramWidth} = this.state;

    const transformX = (diagramWidth - (diffTransformCalc?.diffWidth || 0)) / 2 - (node.x || 0) * curScale;
    const transformY = (diagramHeight - (diffTransformCalc?.diffHeight || 0)) / 2 - (node.y || 0) * curScale;

    this._resetTransform(curScale, transformX, transformY);
  }

  // 初始化事件
  _initEvent() {
    const {resizeNeedReload} = this.props;
    //  暂不需要监听resize
    resizeNeedReload && window.addEventListener('resize', () => this.reload());
  }

  reload(props = this.props, needReload = false) {
    const {data, curData}: any = props;
    // 正在加载中，不重新渲染
    if (!data || props.loading) {
      return;
    }

    // curData 无数据&& needReload为false，重设scale
    // needReload 全屏切换时使用
    // curData panel时不重设，需要居中node
    if ((!curData?.curSource && !curData?.curTarget) || needReload) {
      // reload时重设scale
      this._resetTransform(TopoTheme.defaultScale);
    }

    this._destoryTooltip();
    this._destory();
    this._toggleSvgDisplay();
    this._initHighlight();

    // delay一会，等react更新完毕
    setTimeout(() => {
      this._resize();
    });
  }

  // 隐藏svgGroup
  _toggleSvgDisplay(show = true) {
    const {parentId} = this.state;
    if (!parentId) return;
    d3.select(`#${parentId}`).attr('display', show ? 'block' : 'none');
  }

  // 设置transfrom
  _resetTransform(scale = 1, x = 0, y = 0) {
    const {svgGroup, svgSelection} = this.state;

    svgGroup && svgGroup.attr('transform', `translate(${x}, ${y}) scale(${scale})`);
    this.setState({curScale: scale});
    scaleChangeHandler && scaleChangeHandler(scale);
    // 更新event的x&y&scale的值
    const transform = d3
    .zoomTransform(0)
    .scale(scale)
    // 由于这里的translate在zoom里还会再进行scale处理，我们这里处理下数据
    .translate(x / scale, y / scale);
    svgSelection && d3.zoom().transform(svgSelection, transform);
  }

  // 销毁
  _destory() {
    const {svgGroup} = this.state;
    if (svgGroup !== null) {
      svgGroup.selectAll('g').remove();
    }
  }

  // 清除tooltip数据
  _destoryTooltip() {
    const tooltip = document.querySelector('.tooltip');
    if (tooltip !== null) {
      tooltip.parentNode && tooltip.parentNode.removeChild(tooltip);
    }
  }

  // 销毁links数据
  _destoryLinks() {
    const {svgGroup} = this.state;
    if (svgGroup !== null) {
      svgGroup.selectAll('g.links').remove();
    }
  }

  // 重新计算画布size
  _resize() {
    const container = document.querySelector(`#${this.state.parentId}`);
    const {data, maxHeight} = this.props;

    // 由于考虑到屏幕适配的问题，所以根据父级的container宽度和高度动态的改变图层的大小
    const width = Math.max((container?.parentNode as Element)?.clientWidth || 0, TopoTheme.defaultWidth);
    const height =
        this.props.height ||
        Math.min(Math.max(TopoTheme.minHeight, container ? container.clientHeight : 0), maxHeight || TopoTheme.maxHeight);

    if (!data) {
      return;
    }

    this.setState(
        {
          diagramHeight: height,
          diagramWidth: width,
        },
        () => {
          this._convertData();
          this._updateSvgSelection();
        },
    );
  }

  // 更新svg 宽高
  _updateSvgSelection() {
    const {svgSelection, diagramHeight, diagramWidth} = this.state;

    svgSelection.attr('width', diagramWidth).attr('height', diagramHeight);

    this.setState(
        {
          svgSelection,
        },
        () => {
          this.drawChart();
        },
    );
  }

  // 初始化画布
  _initSvgSelection() {
    const {diagramHeight, diagramWidth} = this.state;
    const {hideZoom} = this.props;
    const self = this;

    const zoomed = () => {
      const currentTransform = d3.event.transform;
      svgGroup.attr('transform', currentTransform);
      scaleChangeHandler && scaleChangeHandler(currentTransform.k);
    };

    // 平移&缩放
    const zoom = d3
    .zoom()
    .scaleExtent(hideZoom ? [TopoTheme.defaultScale, TopoTheme.defaultScale] : [TopoTheme.minZoom, TopoTheme.maxZoom])
    .on('zoom', zoomed);

    const svgSelection = d3
    .select(`#${this.state.parentId}`)
    .append('svg')
    .attr('class', 'topo-svg')
    .attr('width', diagramWidth)
    .attr('height', diagramHeight)
    .attr('cursor', 'pointer')
    .call(zoom)
    .on('click', function () {
      const {svgClickEvent} = self;
      svgClickEvent?.clickCallback && svgClickEvent.clickCallback(d3.event);
      if (!svgClickEvent.initHightlight) return;
      // 点击非节点或连线时，移除tooltip
      self._destoryTooltip();
      // 清除curData数据
      self._saveData({});
    });

    const svgGroup = svgSelection.append('g').attr('class', 'svg-container');

    this.setState({
      svgSelection,
      svgGroup,
    });
  }

  // 转换数据
  _convertData(props = this.props, callback = null) {
    const {diagramHeight} = this.state;
    const {nodeCalMode, layoutType, getMissIds} = props;
    const data: any = props.data;
    if (!data?.nodes || !data?.traces) return;
    const rangs = calculateRange(data.nodes, nodeCalMode);
    const nodes = data.nodes.map(n => {
      const nodeInfo = calNodeSize(rangs, n, nodeCalMode);
      return {
        ...n,
        ...nodeInfo,
        convertId: convertNodeId(n.id),
        width: nodeInfo.nodeRadius * 4,
        height: nodeInfo.nodeRadius * 2 + TopoTheme.nodeLabelDiff * 2,
      };
    });
    const nodesMap = {};
    nodes.forEach(n => {
      if (nodesMap[n.id]) {
        return;
      }
      nodesMap[n.id] = n;
    });
    const missIds = [];
    const links = data.traces.map(d => {
      const {sourceId, targetId} = d;
      if (!nodesMap[sourceId]) {
        missIds.push(sourceId);
      }
      if (!nodesMap[targetId]) {
        missIds.push(targetId);
      }
      return {
        ...d,
        sourceId,
        targetId,
        source: nodesMap[sourceId] || {
          id: sourceId,
          name: d.sourceName,
        },
        target: nodesMap[targetId] || {
          id: targetId,
          name: d.targetName,
        },
        linkNum: 1,
      };
    });

    // missId场景，上报
    const ids = Array.from(new Set(missIds));
    getMissIds && getMissIds(ids);

    if (layoutType === LayoutType.force) {
      // 力导图 布局
      // 创建一个弹簧力，根据 link 的 strength 值决定强度
      const linkForce = d3
      .forceLink(links)
      .id(node => node.id)
      // 资源节点与信息节点间的 strength 小一点，信息节点间的 strength 大一点
      .strength(0.001);

      const simulation = d3
      .forceSimulation(nodes)
      .force('link', linkForce)
      // 在 y轴 方向上施加一个力把整个图形压扁一点
      .force(
          'yt',
          d3.forceY().strength(() => 0.005),
      )
      .force(
          'yb',
          d3.forceY(diagramHeight).strength(() => 0.015),
      )
      // 节点间相互排斥的电磁力
      .force('charge', d3.forceManyBody().strength(-400))
      // 避免节点相互覆盖
      .force(
          'collision',
          d3.forceCollide().radius(d => d.nodeRadius),
      )
      // 通过svgGroup transform去居中
      .force('center', d3.forceCenter(0, 0))
      .stop();

      // 手动调用 tick 使布局达到稳定状态
      for (
          let i = 0, n = Math.ceil(Math.log(simulation.alphaMin()) / Math.log(1 - simulation.alphaDecay()));
          i < n;
          ++i
      ) {
        simulation.tick();
      }
    } else {
      // dagre 布局
      // Create a new directed graph
      const graph = new dagre.graphlib.Graph().setGraph({rankdir: 'LR'});
      // Default to assigning a new object as a label for each new edge.
      graph.setDefaultEdgeLabel(function () {
        return {};
      });

      nodes.forEach(n => {
        graph.setNode(n.id, n);
      });

      links.forEach(l => {
        graph.setEdge(l.sourceId, l.targetId);
      });

      dagre.layout(graph);
      // 入度大于2，向上偏移
      nodes.forEach(n => {
        if (n?.edgeList?.length >= 2) {
          n.y += n.height / 2;
        }
      });
    }

    const linkMap = {};
    // 统计两点之间连线的数量
    links.forEach(link => {
      if (link.targetId === link.sourceId) {
        link.isOwn = true;
      }
      if (linkMap[`${link.sourceId}_${link.targetId}`] || linkMap[`${link.targetId}_${link.sourceId}`]) {
        link.linkNum++;
        return;
      }
      linkMap[`${link.sourceId}_${link.targetId}`] = true;
    });

    this._generateDataRef({nodes, links});

    this.setState(
        {
          nodes,
          links,
        },
        callback,
    );
  }

  _generateDataRef({nodes, links}) {
    nodes.forEach(data => {
      this['tooltip-' + data.id] = React.createRef();
    });
    links.forEach(data => {
      this[`${data.sourceId}-${data.targetId}`] = React.createRef();
    });
  }

  // 生成tooltip
  _generateTooltip(data, type = DataType.node) {
    const {diagramWidth, curData} = this.state;
    const {renderNodeTooltip, renderLinkTooltip} = this.props;
    if ((type === DataType.node && !renderNodeTooltip) || (type === DataType.link && !renderLinkTooltip)) {
      return;
    }
    const width = type === DataType.link ? renderLinkTooltip?.width : renderNodeTooltip?.width;
    const height = type === DataType.link ? renderLinkTooltip?.height : renderNodeTooltip?.height;
    const translateX = type === DataType.link ? renderLinkTooltip?.translateX : renderNodeTooltip?.translateX;
    const translateY = type === DataType.link ? renderLinkTooltip?.translateY : renderNodeTooltip?.translateY;
    const clickCallback = type === DataType.link ? renderLinkTooltip?.clickCallback : renderNodeTooltip?.clickCallback;

    const tool = d3
    .select(`#${this.state.parentId}`)
    .append('div')
    .attr('class', 'tooltip')
    .style('width', `${width || 350}px`)
    .style('height', height ? `${height}px` : '')
    .style('position', 'absolute')
    .on('click', () => {
      clickCallback && clickCallback(curData, d3.event);
      // 阻止click事件冒泡
      d3.event.stopPropagation();
    });

    const tooltip = document.querySelector('.tooltip');

    tooltip.innerHTML =
        type === DataType.link
            ? this[`${data.sourceId}-${data.targetId}`].current.innerHTML
            : this['tooltip-' + data.id].current.innerHTML;

    tool.style(
        'transform',
        `translate(${translateX ?? diagramWidth - tooltip.clientWidth}px ,${translateY ??
        -(tooltip.clientHeight + 10)}px)`,
    );
  }

  // 生成节点数据
  _initCircleData() {
    const {svgGroup, nodes: nodesData} = this.state;
    const {curNodeId, curDataClickCallback, renderCurNodeTooltip, curData, hightlightSingle} = this.props;
    const self = this;
    let dragStartTime = null;
    let timer = null;
    let hoverStartTime = 0;

    svgGroup.select('g.nodes').remove();

    // 节点拖拽函数
    const drag = d3
    .drag()
    .subject(function (d) {
      return d;
    })
    .on('start', function () {
      dragStartTime = +moment();
      d3.event.sourceEvent.stopPropagation();
      d3.select(this).classed('dragging', true);
    })
    .on('drag', function (d) {
      // 制定时间差，避免点击时执行
      if (+moment() - dragStartTime < 100) {
        return;
      }
      // 更新节点坐标
      d.x += d3.event.dx;
      d.y += d3.event.dy;
      d3.select(this).attr('transform', `translate(${d.x}, ${d.y})`);
      // 更新连线数据
      self._destoryLinks();
      // updateLinksData(graphData, d)
      self._initLinkData();
      // 拖拽时，回到初始状态
      self._initHighlight();
    })
    .on('end', function () {
      d3.select(this).classed('dragging', false);
    });

    // Select nodes
    svgGroup
    .append('g')
    .attr('class', 'nodes')
    .selectAll('g.nodes')
    .data(nodesData)
    .enter()
    .append('g')
    .attr('class', 'node-item')
    .attr('cursor', 'move')
    .attr('id', data => data.convertId)
    .attr('transform', ({x, y}) => `translate(${x}, ${y})`)
    .call(drag) // 实现节点拖拽
    .on('click', function (data) {
      const {svgClickEvent} = self;
      // 阻止click事件冒泡
      d3.event.stopPropagation();
      if (svgClickEvent.eventBreak && svgClickEvent.eventBreak(data)) return;
      svgClickEvent.clickCallback && svgClickEvent.clickCallback(d3.event);
      // 保持客户端节点选择高亮效果
      const curData = {
        ...data,
        highlightType: hightlightSingle && 'target',
        dataType: DataType.node,
      };
      self._saveData(curData);
      self._destoryTooltip();
      curDataClickCallback && curDataClickCallback(curData);
      self._generateTooltip(data);
    })
    .on('mouseover', function (node) {
      // 如果存在className为dragging，则处于拖拽期间=》取消hover效果
      const dragging = document.querySelector('.dragging');
      if (dragging) {
        return;
      }
      // 设置timer，避免页面闪烁
      hoverStartTime = +moment();
      timer = setTimeout(function () {
        self._highlightNode(node);
      }, TIME_LIMIT);
    })
    .on('mouseout', function () {
      if (+moment() - hoverStartTime < TIME_LIMIT) {
        clearTimeout(timer);
        return;
      }
      self._initHighlight();
    });

    this._handleNode();

    if ((curNodeId && renderCurNodeTooltip) || curData?.curTarget || curData?.curSource) {
      return;
    }

    // 调整svgGroup位置
    this._centeredPosition();
  }

  _centeredPosition() {
    const {svgGroup, curScale, diagramHeight, diagramWidth} = this.state;
    const {layoutType} = this.props;
    // 调整svgGroup位置
    const width = svgGroup.node().getBoundingClientRect().width;
    const height = svgGroup.node().getBoundingClientRect().height;
    const offsetX = (diagramWidth - width) / 2;
    const offsetY = (diagramHeight - height) / 2;
    this._resetTransform(
        curScale,
        layoutType === LayoutType.force ? diagramWidth / 2 : offsetX > 0 ? offsetX : 0,
        layoutType === LayoutType.force ? diagramHeight / 2 : offsetY > 0 ? offsetY : 0,
    );
  }

  // 生成内圆，填充白色，覆盖连线多余部分
  _genOvercapCircle(nodes) {
    nodes
    .append('circle')
    .attr('r', d => d.nodeRadius - d.innerNodeRadiusDiff)
    .attr('fill', '#fff');
  }

  // 当前服务标识
  _drawCurNode(nodes) {
    const {curNodeId, renderCurNodeTooltip} = this.props;
    const self = this;
    let curFlag = false;
    nodes.each(function (d) {
      if (d.id !== curNodeId || !renderCurNodeTooltip) return;
      curFlag = true;
      // 将当前node提升，避免tip被遮盖
      d3.select(this).raise();
      const c = d3
      .select(this)
      .append('g')
      .attr('class', 'cur-node');

      const {content, width, height, clickCallback} = renderCurNodeTooltip(d);

      const con = c
      .append('foreignObject')
      .attr('width', width)
      .attr('height', height)
      .style('cursor', 'pointer')
      .html(content);

      c.attr('transform', `translate(-${nodeWidth(con.node()) / 2}, -${d.nodeRadius + nodeHeight(con.node())})`);

      con.on('click', d => {
        clickCallback && clickCallback(d, d3.event);
        // 阻止click事件冒泡
        d3.event.stopPropagation();
      });

      self._locateSvgGroup(d);
    });
    if (!curFlag) {
      this._centeredPosition();
    }
  }

  // 生成连线
  _initLinkData() {
    const {svgGroup, links: linkDatas} = this.state;
    const {layoutType, curDataClickCallback} = this.props;
    const self = this;
    let timer = null;
    let hoverStartTime = 0;

    svgGroup.select('g.links').remove();

    // How to draw edges
    const line = d3
    .line()
    .curve(d3.curveBasis)
    .x(d => d.x)
    .y(d => d.y);

    // Plot edges
    // 使用insert将links组作为第一个子元素插入svgGroup
    const links = svgGroup
    .insert('g', ':first-child')
    .attr('class', 'links')
    .selectAll('path')
    .data(
        linkDatas.map(link => {
          const {source, target} = link;
          const points = getLinkPoints(link, layoutType);
          return {
            ...link,
            points,
            id: `${source.convertId}-${target.convertId}`,
          };
        }),
    )
    .enter()
    .append('g')
    .attr('class', 'link-item')
    .attr('id', d => d.id)
    .on('click', function (linkData) {
      const {svgClickEvent} = self;
      // 阻止click事件冒泡
      d3.event.stopPropagation();
      if (svgClickEvent.eventBreak && svgClickEvent.eventBreak(linkData)) {
        return;
      }
      svgClickEvent.clickCallback && svgClickEvent.clickCallback(d3.event);
      self._destoryTooltip();
      const curData = {...linkData, dataType: DataType.link};
      self._saveData(curData);
      curDataClickCallback && curDataClickCallback(curData);
      self._generateTooltip(linkData, DataType.link);
    })
    .on('mouseover', function (linkData) {
      hoverStartTime = +moment();
      timer = setTimeout(() => {
        self._highlightLink(linkData);
      }, TIME_LIMIT);
    })
    .on('mouseout', function () {
      if (+moment() - hoverStartTime < TIME_LIMIT) {
        clearTimeout(timer);
        return;
      }
      // 移除高亮样式
      self._initHighlight();
    });

    //添加defs标签
    const defs = links.append('defs');
    //添加marker标签及其属性
    defs
    .append('marker')
    .attr('id', (d, i) => `arrow${i}`)
    .attr('markerUnits', 'strokeWidth')
    .attr('markerWidth', TopoTheme.markerSize)
    .attr('markerHeight', TopoTheme.markerSize)
    .attr('viewBox', '0 0 12 12')
    .attr('refX', 6)
    .attr('refY', 6)
    .attr('orient', 'auto')
    .append('path')
    .attr('d', ({source, target}) =>
        target.x >= source.x ? 'M2,2 L10,6 L2,10 L7,6 L2,2' : 'M10,2 L2,6 L10,10 L5,6 L10,2',
    )
    .attr('fill', d => self.getColors(d, 'line'));

    // 添加连线层，处理hover连线高亮细节
    links
    .append('path')
    .attr('d', d => line(d.points))
    .attr('fill', 'none')
    .attr('stroke-width', 10)
    .attr('stroke', 'transparent')
    .attr('id', d => `traceTsw-${d.sourceId}traceTsw-${d.targetId}`);

    links
    .append('path')
    .attr('d', d => line(d.points))
    .attr('id', d => `textPath${d.id}`)
    .attr('fill', 'none')
    .attr('class', 'line')
    .attr('stroke-width', TopoTheme.lineWidth)
    // 由于设置了外层line，需要将当前的path pointer-events设置为none，
    // 禁止当前line再次触发鼠标事件，避免出现hover连线时抖动问题
    .attr('pointer-events', 'none')
    .attr('stroke', d => self.getColors(d, 'line'))
    // 为连线添加箭头
    .attr('marker-end', ({source, target}, i) => (target.x >= source.x ? `url(#arrow${i})` : ''))
    // 根据x调整箭头
    .attr('marker-start', ({source, target}, i) => (target.x < source.x ? `url(#arrow${i})` : ''));

    // 添加连线上的label
    links
    .append('text')
    .append('textPath')
    // 给textPath设置path的引用
    .attr('xlink:href', d => {
      return '#textPath' + d.id;
    })
    // 字体居中
    .style('text-anchor', 'middle')
    .attr('startOffset', '50%')
    .text(linkData => (linkData?.linkShowText ? linkData?.linkShowText(linkData) : ''))
    .attr('font-size', '12px')
    .attr('fill', d => self.getColors(d))
    .attr('dominant-baseline', 'text-before-edge');
  }

  // 生成节点中间的信息title
  _generateCenterTitle(nodes) {
    // Add text to nodes
    const textGroup = nodes
    .append('g')
    .attr('class', 'text')
    .style('font-weight', 'bold')
    .style('font-size', '12px')
    .style('font-family', 'sans-serif')
    .style('text-anchor', 'middle')
    .style('alignment-baseline', 'middle')
    .style('fill', 'black');

    textGroup
    .selectAll('text.center-text')
    .data(d =>
        d.nodeCenterContent && !d.disabled && !d.imgHref
            ? d.nodeCenterContent(d).map((text, i) => ({
              ...d,
              text,
              i,
              length: d.nodeCenterContent(d)?.length,
            }))
            : [],
    )
    .enter()
    .append('text')
    .attr('class', 'center-text')
    .attr('dy', d =>
        d.length > 1 ? (d.i === 0 ? `-${d.textDiffEm - d.textDiff}em` : `${d.textDiffEm + d.textDiff}em`) : 0,
    )
    .style('font-size', d => (d.i === 0 ? `${d.reqFontSize}px` : '12px'))
    .text(d => d.text);
  }

  //将所有节点及连线的透明度设置为0.1
  _setOpacity() {
    const {parentId} = this.state;
    d3.selectAll(`section#${parentId} g.node-item`).attr('opacity', 0.1);
    d3.selectAll(`section#${parentId} g.link-item`).attr('opacity', 0.1);
  }

  // 高亮多个单节点
  _highlightMulNode(nodes) {
    const {nodes: nodesData, parentId} = this.state;
    this._setOpacity();
    const nodesMap = {};
    nodesData.forEach(item => {
      nodesMap[item.id] = item.convertId;
    });
    nodes.forEach(nodeId => {
      // 处理id，避免出现id中存在.的问题
      d3.select(`section#${parentId} g#${nodesMap[nodeId]}`).attr('opacity', 1);
    });
  }

  // 移入节点，高亮与节点相关的连线及其他节点
  _highlightNode(node) {
    const {links, parentId} = this.state;
    this._setOpacity();
    // 处理id，避免出现id中存在.的问题
    d3.select(`section#${parentId} g#${node.convertId}`).attr('opacity', 1);
    // 设置node.highlightType，若为target即被调用者，仅高亮其parents，若为source即调用者，仅高亮其children
    // source
    node.highlightType !== 'target' &&
    links.forEach(({source, target}) => {
      if (source.convertId !== node.convertId) return;
      // 将child节点高亮
      d3.select(`section#${parentId} g#${target.convertId}`).attr('opacity', 1);
      // 将node->child连线高亮
      d3.select(`section#${parentId} g#${node.convertId}-${target.convertId}`)
      .attr('opacity', 1)
      .select('.line')
      .attr('stroke-width', TopoTheme.focusLineWidth);
    });
    // 使用edgeList来获取入度信息
    // target
    node.highlightType !== 'source' &&
    links.forEach(({source, target}) => {
      if (target.convertId !== node.convertId) return;
      // 将parent节点高亮
      d3.select(`section#${parentId} g#${source.convertId}`).attr('opacity', 1);
      // 将parent->node连线高亮
      d3.select(`section#${parentId} g#${source.convertId}-${node.convertId}`)
      .attr('opacity', 1)
      .select('.line')
      .attr('stroke-width', TopoTheme.focusLineWidth);
    });
  }

  // 移入连线，高亮与连线相关的节点
  _highlightLink({source, target}) {
    const {parentId} = this.state;
    this._setOpacity();
    // 将连线两头节点高亮
    d3.select(`section#${parentId} #${source.convertId}`).attr('opacity', 1);
    d3.select(`section#${parentId} #${target.convertId}`).attr('opacity', 1);
    // 将连线高亮
    d3.select(`section#${parentId} g#${source.convertId}-${target.convertId}`)
    .attr('opacity', 1)
    .select('.line')
    .attr('stroke-width', TopoTheme.focusLineWidth);
  }

  // 保存当前data
  _saveData(curData) {
    this.setState(
        {
          curData,
        },
        () => {
          this._initHighlight();
        },
    );
  }

  // 移除高亮样式
  _initHighlight() {
    // 获取curData，保持curData高亮效果
    const {curData, parentId}: any = this.state;
    if (!curData?.dataType) {
      d3.selectAll(`section#${parentId} g.node-item`).attr('opacity', 1);
      d3.selectAll(`section#${parentId} g.link-item`)
      .attr('opacity', 1)
      .select('.line')
      .attr('stroke-width', TopoTheme.lineWidth);
      return;
    }
    // 区分single节点
    if (curData?.dataType === 'single') {
      this._highlightMulNode(curData?.nodes);
      return;
    }
    if (curData?.dataType === DataType.node) {
      this._highlightNode(curData);
      return;
    }
    this._highlightLink(curData);
  }

  // 处理节点大小&中心title等
  _handleNode() {
    const {svgGroup} = this.state;
    const {curNodeId, convertRingData, imgHrefConvertToBase64} = this.props;
    const {nodeColors} = this;
    const nodes = svgGroup.select('g.nodes').selectAll('g.node-item');

    //用svg的path绘制弧形的内置方法
    const arc = d3
    .arc() //设置弧度的内外径，等待传入的数据生成弧度
    .outerRadius(d => d.nodeRadius)
    .innerRadius(d => d.nodeRadius - d.innerNodeRadiusDiff);

    this._genOvercapCircle(nodes);
    curNodeId && this._drawCurNode(nodes);
    this._generateCenterTitle(nodes);

    // 创建环形node
    nodes
    .selectAll('g.arc')
    .data(d =>
        d3
        .pie()(convertRingData ? convertRingData(d) : [])
        .map(p => ({
          ...p,
          nodeRadius: d.nodeRadius,
          innerNodeRadiusDiff: d.innerNodeRadiusDiff,
          disabled: d.disabled,
          imgHref: d.imgHref,
        })),
    )
    .enter()
    .append('g')
    .attr('class', 'arc')
    .append('path') //每个g元素都追加一个path元素用绑定到这个g的数据d生成路径信息
    .attr('fill', ({data, disabled, imgHref}, i) => {
      return imgHref ? 'transparent' : disabled ? nodeColors[0] : data <= 1 ? nodeColors[i + 1] : 'transparent';
    })
    .attr('d', arc);

    // 展示客户端图片
    const imageContainer = nodes
    .append('image')
    .attr('x', d => `-${d.nodeRadius}`)
    .attr('y', d => `-${d.nodeRadius}`)
    .attr('id', data => `image-${data.id}`)
    .attr('width', d => d.nodeRadius * 2 - d.innerNodeRadiusDiff)
    .attr('height', d => d.nodeRadius * 2 - d.innerNodeRadiusDiff);

    // 下载需求需要把外链图片内容引入
    imageContainer.each(function (d) {
      const width = d.nodeRadius * 2 - d.innerNodeRadiusDiff;
      const height = d.nodeRadius * 2 - d.innerNodeRadiusDiff;
      if (imgHrefConvertToBase64) {
        convertImgToBase64(d.imgHref, width, height, url => {
          // 浏览器适配，需指定宽高，并使用xlink:href
          d3.select(this).attr('xlink:href', url);
        });
        return;
      }
      d3.select(this)
      .attr('width', width)
      .attr('height', height)
      .attr('xlink:href', d.imgHref);
    });

    // 生成节点label，客户端不展示label
    nodes
    .selectAll('g.label')
    .data(d => (d.labels || [d.name]).filter(n => n).map(name => ({
          ...d,
          name,
          nodeRadius: d.nodeRadius,
          type: d.type
        })),
    )
    .enter()
    .append('g')
    .attr('class', 'label')
    // 根据节点大小确定label translate
    .attr('transform', (d, i) => `translate(0, ${d.nodeRadius + TopoTheme.nodeLabelDiff * (i + 1)})`)
    .append('text')
    .style('font-size', '14px')
    .style('font-weight', 'bold')
    .style('text-anchor', 'middle')
    .attr('cursor', 'pointer')
    .text(data => data.nodeShowText ? data.nodeShowText(data) : data.name);
  }

  drawChart() {
    const {data} = this.props;
    if (!data) return;
    this._initLinkData();
    this._initCircleData();
    this._initHighlight();
  }

  getColors(linkData, type = '') {
    const {linkColors, labelColors} = this;
    const color = type === 'line' ? linkData?.linkColor || linkColors[0] : linkData?.labelColor || labelColors[0];
    return color;
  }

  render() {
    const {loading, data, renderNodeTooltip, renderLinkTooltip, emptyContent} = this.props;
    const {nodes, links} = this.state;
    const noData = !data || (!data?.nodes?.length && !data?.traces?.length);
    return (
        <>
          {renderNodeTooltip ? (
              <section style={{display: 'none'}}>
                {nodes?.map(data => (
                    <section key={data.id} ref={this['tooltip-' + data.id]}>
                      {renderNodeTooltip.render && renderNodeTooltip.render(data)}
                    </section>
                ))}
              </section>
          ) : null}
          {renderLinkTooltip ? (
              <section style={{display: 'none'}}>
                {links?.map((data, i) => (
                    <section key={`${data.sourceId}-${data.targetId}-${i}`}
                             ref={this[`${data.sourceId}-${data.targetId}`]}>
                      {renderLinkTooltip.render && renderLinkTooltip.render(data)}
                    </section>
                ))}
              </section>
          ) : null}
          <section style={{height: '100%', minHeight: 200}}>
            {loading && (
                <Text parent='p' style={{textAlign: 'center', padding: '100px 0'}} reset>
                  <Icon type='loading'/>
                  loading...
                </Text>
            )}
            {!loading &&
            noData &&
            (emptyContent || (
                <Text parent='p' style={{textAlign: 'center', padding: '100px 0'}} reset>
                  暂无数据
                </Text>
            ))}
            <section
                id={this.state.parentId}
                style={{
                  height: ' 100%',
                  display: !loading && !noData ? 'block' : 'none',
                  width: '100%',
                }}
            />
          </section>
        </>
    );
  }
}
