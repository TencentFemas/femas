export const GraphConfig = {
  // ===常量===
  // 画布内边距PX
  PADDING: 40,
  // 画布最小宽度PX
  MIN_WIDTH: 200,
  // 画布高度PX
  HEIGHT: 400,
  // PointSize Hover差值
  pointSizeDiff: 4,
  // 画布宽度临界值
  THRESHOLD_WIDTH: 1000,
  THRESHOLD_HEIGHT: 400,
  MINI_PADDING: 15,
  AXIS_DIFF: 1,
  defaultPointSize: 2,
};

export const originColors = [
  '#2d70f6',
  '#6fd598',
  '#f6ca54',
  '#eb6455',
  '#8b4bd2',
  '#5dbdca',
  '#94d553',
  '#f1a03f',
  '#d34a83',
  '#6360de',
  '#71c860',
  '#af40b1',
];

export const legendTheme = {
  rectWidth: 16,
  rectHeight: 5,
  fontSize: 12,
  disabledColor: '#CECECE',
  color: '#2B2B2B',
};

export const toolBarTheme = {
  rectWidth: 16,
  rectHeight: 16,
  padding: 10,
};

export const axisTheme = {
  fontSize: 10,
  miniFontSize: 8,
  descFontSize: 10,
  tickTextColor: '#B0B0B0',
  lineColor: '#CECECE',
  splitLineColor: '#eee',
  descColor: '#777',
  descFontWeight: 300,
  splitLineWidth: 1,
  arrowColor: '#B0B0B0',
  diagramMinWidth: 850,
  miniTickSize: 5,
  normalTickSize: 8,
  yTickHeight: 40,
};

export const lineTheme = {
  hoverPointSize: 5,
  hoverCircleStrokeWidth: 2,
};

export const titleTheme = {
  fontSize: 14,
  fontColor: '#000',
  fontWeight: 600,
  diffDistance: 45,
  descFontSize: 12,
  descFontColor: '#888',
};

export const pieTheme = {
  maxRadius: 150,
  diffRadius: 10,
  hoverOpacity: 0.5,
};

export const FlameTheme = {
  ceilHeight: 20,
  hoverHeight: 22,
  minSize: 0,
  beginColor: '#eeeeee',
  beginOffset: 5,
  endColor: '#eeeeb0',
  endOffset: 95,
  descColor: '#666',
  fontSize: 12,
};

export const BarTheme = {
  barPadding: 5,
  hoverRectColor: '#666',
  stackFontColor: '#333',
  innerPadding: 5,
  maxWidth: 100,
  barMargin: 30,
};

export const FlowTheme = {
  NodeCircle: 16,
  borderColor: '#bbb',
  borderSize: 1.5,
  nodeWidth: 120,
  nodeHeight: 50,
  rectRadius: 5,
  hoverColor: '#23abfd',
  lineColor: '#F6AC04',
  arrowWidth: 10,
  diff: 10,
  nodeTooltipWidth: 280,
  nodeTooltipHeight: 160,
  traceTooltipWidth: 280,
  traceTooltipHeight: 160,
  lineWidth: 1.5,
  emphaisisLineWidth: 2.5,
  hintCircle: 6,
  errorColor: '#eb6455',
  tickSize: 20,
};

export const DefaultPaddingXForHead = 20;
export const DefaultPaddingXForTail = 10;

/** topo */
export const TopoTheme = {
  defaultWidth: 700,
  minHeight: 700,
  maxHeight: 700,
  lineWidth: 1.5,
  focusLineWidth: 3,
  markerSize: 18,
  nodeLabelDiff: 15,
  defaultScale: 0.6,
  maxZoom: 2,
  minZoom: 0.1,
};

// 节点的颜色， 橙 绿
export const circleColors = ['#999999', '#F5A623', '#29cc85'];
// 调用链的颜色： 灰 橙 红
export const lineColors = ['#aaa', '#ffb000', '#ee4242'];
// 调用链上label的颜色，对应colors的顺序
export const labelColors = ['#777', '#D17600', '#DA4724'];

export const NodeRadius = [50, 65, 80, 95, 110];
export const InnerRadiusDiff = [8, 10, 12, 14, 16];
export const ReqFontSize = [12, 16, 20, 24, 28];
export const TextDiffEm = [1, 1.05, 1.1, 1.15, 1.2];
export const TextDiff = [0, 0.4, 0.4, 0.8, 0.8];

/** topo */
