export enum LayoutType {
  dagre = 'relationship',
  force = 'force',
}

export enum DataType {
  node = 'node',
  link = 'link',
}

export interface TopologyProps {
  /**
   * 当前选择的内容，用于外部改变
   */
  curData?: CurData;
  /**
   * 依赖图数据
   */
  data: TopoData;
  /**
   * loading
   */
  loading?: boolean;
  /**
   * 高度
   * @default 700
   */
  height?: number;
  /**
   * 是否需要响应式处理，即监听resize
   *
   * @default false
   */
  resizeNeedReload?: boolean;
  /**
   * 定义最大高度，切换全屏功能时可以使用
   * @default 700
   */
  maxHeight?: number;
  /**
   * svg点击回调
   */
  svgClickEvent?: SvgClick;
  /**
   * 以某个字段名数据为准，将节点大小范围分为5段
   * 不填默认等大
   */
  nodeCalMode?: string; // 节点计算方式
  /**
   * 禁用缩放
   */
  hideZoom?: boolean;
  /**
   * 布局类型，支持力导图以及顺排图
   */
  layoutType?: LayoutType;
  /**
   * 节点/连线点击回调，可用于自定义点击提示，如不使用自带的tooltip，需要用抽屉等
   */
  curDataClickCallback?: (curData) => void;
  /**
   * 获取missId，做额外工作，比如yehe上报等
   */
  getMissIds?: (missIds: Array<string>) => void;
  /**
   * 环形数据，比率数据
   */
  convertRingData: (data) => Array<number>;
  /**
   * 节点颜色
   * 注：disabled默认取第一个颜色
   *
   * @default ['#999999','#F5A623','#29cc85']
   */
  nodeColors?: Array<string>;
  /**
   * 连线颜色
   * 注：disabled默认取第一个颜色
   * @default ['#aaa','#ffb000','#ee4242']
   */
  linkColors?: Array<string>;
  /**
   * 连线label颜色
   * 注：disabled默认取第一个颜色
   * @default ['#777','#D17600','#DA4724']
   */
  labelColors?: Array<string>;
  /**
   * 当前节点ID，当前节点位置将居中
   */
  curNodeId?: string;
  /**
   * cur node tooltip内容
   */
  renderCurNodeTooltip?: (data) => CurNodeTooltip;
  /**
   * 节点tooltip内容
   */
  renderNodeTooltip?: Tooltip;
  /**
   * link tooltip内容
   */
  renderLinkTooltip?: Tooltip;
  /**
   * 外部控制缩放大小
   */
  topoScale?: number;
  /**
   * 缩放值变化回调
   */
  onScaleChange?: (scale) => void;
  /**
   * icon img src
   * 注：src不同源时将会产生跨域问题
   */
  imgHrefConvertToBase64?: boolean;

  /**
   * 数据为空时的渲染内容
   *
   * 默认展示`暂无数据`
   */
  emptyContent?: React.ReactNode;

  /**
   * 宽高差，用于存在左侧panel时，定位居中
   */
  diffTransformCalc?: Calc;
  /**
   * 是否单向高亮
   */
  hightlightSingle?: boolean;
}

export interface SvgClick {
  /**
   * 点击回调
   */
  clickCallback?: (e) => void;
  /**
   * 是否需要还原样式
   * @default true
   */
  initHightlight?: boolean;
  /**
   * 是否不支持后续默认事件内容
   * @default false
   */
  eventBreak?: (curData) => boolean;
}

export interface Calc {
  /**
   * 宽度差
   */
  diffWidth?: number;
  /**
   * 高度差
   */
  diffHeight?: number;
}

export interface Tooltip {
  /**
   * Tooltip渲染函数
   */
  render: (item) => React.ReactNode;
  /**
   * Tooltip 宽度
   *
   * width限定，当tooltip内容过大时需要修改width
   *
   * @default 350
   */
  width?: number;
  /**
   * Tooltip 高度
   *
   * height 限定，当tooltip内容过大时需要修改height
   *
   */
  height?: number;
  /**
   * tooltip x偏移，默认在画布右下角
   */
  translateX?: number;
  /**
   * tooltip y偏移，默认在画布右下角
   */
  translateY?: number;
  /**
   * 点击回调
   */
  clickCallback?: (curData, event) => void;
}

export interface CurNodeTooltip {
  /**
   * curNode 内容
   */
  content: string;
  /**
   * 宽度
   */
  width: number;
  /**
   * 高度
   */
  height: number;
  /**
   * 点击回调
   */
  clickCallback?: (curData, event) => void;
}

export interface CurData {
  /**
   * sourceId
   */
  curSource: string;
  /**
   * targetId
   */
  curTarget: string;
}

export interface TopoData {
  /**
   * 节点数据
   */
  nodes: Array<NodeItem>;
  /**
   * 连线数据
   */
  traces: Array<TraceItem>;
}

export interface NodeItem extends Record<string, any> {
  /**
   * ID，唯一标识
   */
  id: string;
  /**
   * 名称
   */
  name: string;
  /**
   * 节点展示内容
   */
  nodeShowText?: (data) => string;
  /**
   * icon外链
   */
  imgHref?: string;
  /**
   * disabled状态，颜色灰色，且不展示中间title等内容
   */
  disabled?: boolean;
  /**
   * 节点下方展示的内容，不填默认展示name
   */
  labels?: Array<string>;

  /**
   * 中间文本内容展示，目前最多适配两个
   */
  nodeCenterContent?: (data) => Array<string>;
}

export interface TraceItem extends Record<string, any> {
  /**
   * 源节点ID
   */
  sourceId: string;
  /**
   * 目标节点ID
   */
  targetId: string;
  /**
   * 连线展示的文本内容
   */
  linkShowText?: (data) => string;
  /**
   * 连线颜色
   */
  linkColor?: string;
  /**
   * 连线上文本颜色
   */
  labelColor?: string;
}
