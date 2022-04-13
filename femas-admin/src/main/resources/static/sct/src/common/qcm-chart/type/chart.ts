/* eslint-disable @typescript-eslint/camelcase */
import { getStatisticPeriod } from '../core/lib/helper';

export interface Value {
  /**
   * 数据名
   *
   * 用于展示tooltip，横坐标轴等信息
   */
  label: string;
  /**
   * 数据值
   */
  value: number;
  /**
   * tooltip中label展示别名
   */
  tipLabel?: string;
  /**
   * tooltip中value展示别名
   */
  tipValue?: number | string;
}

export interface Data {
  /**
   * 数据名
   *
   * `唯一`时颜色、legend效果才会生效
   */
  name: string;

  /**
   * 数据
   */
  value: Array<Value>;

  /**
   * 所属y轴
   *
   * - `0` 正方向，可不传
   * - `1` 负方向
   *
   * @default 0
   */
  yIndex?: number; // 仅传外部就可以，Value会在Line中处理

  /**
   * 数据单位
   *
   * 在Tooltip中显示
   */
  unit?: string;

  /**
   * 附加信息
   *
   * 在Tooltip中显示
   */
  additionalTip?: string | Array<string>;

  /**
   * 用于展示Tooltip提示信息
   *
   * 一般在对比折线图中需要使用
   *
   * 不传默认使用`name`
   */
  tipName?: string;

  /**
   * 当前value不想在Y坐标轴上显示可用
   *
   * `仅支持散点图`
   *
   * @default false
   */
  hideValue?: boolean;
}

export interface AdditionalLine {
  /**
   * 数值
   */
  value: number;

  /**
   * 描述
   */
  desc?: string;

  /**
   * 线颜色，默认与当前折线相同
   */
  color?: string;

  /**
   * 描述文字样式
   *
   * 如：'font-size: 12px;fill:red'
   *
   * 颜色默认与当前折线相同
   */
  textStyle?: string;

  /**
   * x 偏移量
   */
  formatterOffsetX?: (textWidth: number, width: number, padding: number) => number;

  /**
   * y 偏移量
   *
   * valueY为当前值所在y坐标
   */
  formatterOffsetY?: (textHeight: number, valueY: number, height: number, padding: number) => number;

  /**
   * 线宽度
   *
   * @default 1
   */
  lineWidth?: number;

  /**
   * 虚线样式，`none`则为实线
   *
   * @default '3 5'
   */
  lineDash?: string;
}

export interface DataProps {
  /**
   * 图表的数据源
   */
  data: Array<Data>;
}

export interface LineData extends Data {
  /**
   * 虚线样式，不设置则为实线，仅支持`折线图`
   *
   * 设置示例：'3 5' 表示点大小为3，间隔为5
   *
   * @default 'none'
   *
   */
  lineDash?: string;

  /**
   * 附加线，支持`折线图`中展示
   *
   * 对比折线图、堆叠折线图暂不支持
   *
   * 不在Tooltip&Legend中展示
   */
  additionalLines?: Array<AdditionalLine>;
}

export interface LineDataProps {
  /**
   * 图表的数据源
   */
  data: Array<LineData>;
}

export interface StackBarData extends Data {
  /**
   * 堆叠index，支持`堆叠柱状图`
   */
  stackIndex?: number;
}

export interface StackBarDataProps {
  /**
   * 图表的数据源
   */
  data: Array<StackBarData>;
}

export interface BaseProps {
  /**
   * @ignore
   */
  data: any;

  /**
   * 自定义图表颜色
   *
   * `火焰图`及`任务流图`暂不支持
   *
   * @default ['#2d70f6','#6fd598','#f6ca54','#eb6455','#8b4bd2','#5dbdca','#94d553','#f1a03f','#d34a83','#6360de','#71c860','#af40b1',]
   */
  colors?: Array<string>;

  /**
   * 是否需要展示Tooltip，推荐使用 [tooltip]
   *
   * @deprecated
   */
  needTooltip?: boolean;

  /**
   * 支持自定义Tooltip
   */
  tooltip?: Tooltip;

  /**
   * 图例
   *
   * `任务流图`、`火焰图`暂不支持
   */
  legend?: Legend;

  /**
   * 定义工具栏
   */
  toolBar?: ToolBar;

  /**
   * 标题
   */
  title?: Title | string;

  /**
   * 是否需要响应式处理，即监听resize
   *
   * @default false
   */
  resizeNeedReload?: boolean;

  /**
   * 数据为空时的渲染内容
   *
   * 默认展示`暂无数据`
   */
  emptyContent?: React.ReactNode;

  /**
   * 图表高度
   *
   * - `简易折线图`默认为`200`
   * - `火焰图`暂不支持
   */
  height?: number;

  /**
   * 加载状态
   * 注：当且仅当loading为true，silentReload为false时，会展示loading内容
   */
  loading?: boolean;

  /**
   * 数据更新方式
   * 为true时，将不展示loading内容，静默更新
   *
   *  @default false
   */
  silentReload?: boolean;

  /**
   * position
   *
   * @default relative
   */
  position?: string;

  /**
   * window.resize 触发阈值，单位ms
   * @default 300ms
   */
  threshold?: number;
}

export interface Tooltip {
  /**
   * 是否展示
   *
   * 向前兼容，会受needTooltip影响
   *
   * @default true
   */
  show?: boolean;

  /**
   * 附加显示信息
   *
   * 不想重写`formatter`又需要添加附加信息时使用
   */
  additionalTip?: string | Array<string>;

  /**
   * 是否展示分割线
   *
   * @default false
   */
  showSplitLine?: boolean;

  /**
   * 数据格式化
   *
   * data：折线图返回为数组，其他返回该类型数据
   *
   * colorMap：仅支持散点图，用于适配渐变颜色等设置
   *
   * `火焰图`没有colors内容
   */
  formatter?: (data, colors, colorMap?) => string;

  /**
   * hover回调
   * 支持Line，与Line内部抛出的mouseHoverHandler一起使用，实现图表联动功能
   */
  mouseHoverCallback?: (mouse_x: number, mouse_y: number) => void;
  /**
   * mouse out回调
   * 支持Line，与Line内部抛出的mouseHoverHandler一起使用，实现图表联动功能
   */
  mouseoutCallback?: () => void;
}

export interface Title {
  /**
   * 标题内容
   */
  text: string;

  /**
   * 标题字体大小
   *
   * @default 14
   */
  titleFontSize?: number;

  /**
   * 标题字体颜色
   *
   * @default #000
   */
  titleColor?: string;

  /**
   * 描述
   */
  desc?: string;

  /**
   * 描述字体大小
   *
   * @default 12
   */
  descFontSize?: number;

  /**
   * 描述字体颜色
   *
   * @default #888
   */
  descColor?: string;

  /**
   * 是否展示提示
   *
   * @default false
   */
  showBubble?: boolean;

  /**
   * icon type
   *
   * 支持Tea Icon type
   *
   * @default info
   */
  iconType?: string;

  /**
   * 提示内容
   */
  tipContent?: string;
}

export interface Legend {
  /**
   * 是否展示legend
   *
   * @default true
   */
  show?: boolean;

  /**
   * 默认disable的legend
   *
   * 折线图、柱状图传：`name`数组
   *
   * 饼图传`label`数组
   */
  disabledKeys?: Array<string>;
}

export interface ToolBar {
  /**
   * 是否展示工具栏
   *
   * @default true
   */
  show: boolean;

  /**
   * 保存为图片
   */
  saveAsImage?: SaveAsImageProps;

  /**
   * 点击放缩
   */
  zoom?: Zoom;

  /**
   * 自定义操作
   *
   * @default true
   */
  more?: Array<MoreListItem>;

  /**
   * 自定义icon及回调
   *
   * @default []
   */
  customIconList?: Array<CustomIcon>;
}

export interface CustomIcon {
  /**
   * icon显示
   * 如：<i type="download" class="tea-icon tea-icon-download"></i>
   */
  icon: string;

  /**
   * icon提示title
   *
   * @default 自定义
   */
  title?: string;

  /**
   * 点击回调
   */
  onClick?: () => void;
}

export interface Zoom {
  /**
   * 是否展示放缩功能
   *
   *
   * @default false
   */
  show: boolean;

  /**
   * 放大-回调函数
   */
  amplifyCallback?: () => void;

  /**
   * 缩小-回调函数
   */
  shrinkCallback?: () => void;
}

export interface MoreListItem {
  /**
   * 操作名
   */
  text: string;

  /**
   * 操作值
   */
  value: string;

  /**
   * 操作回调函数
   */
  onSelect?: (value) => void;
}

export interface SaveAsImageProps {
  /**
   * 是否展示下载图片功能
   *
   * @default true
   */
  show: boolean;
  /**
   * 图片名称
   *
   * @default 数据图
   */
  title?: string;
}

export interface AxisProps {
  /**
   * 是否需要隐藏X轴
   *
   * @default false
   */
  hideX?: boolean;

  /**
   * 是否需要隐藏Y轴
   *
   * @default false
   */
  hideY?: boolean;

  /**
   * 坐标轴滚轮缩放
   */
  axisZoom?: AxisZoom;

  /**
   * 选框缩放
   */
  axisBrushZoom?: AxisBrushZoom;

  /**
   * 坐标轴分割线
   */
  axisSplitLine?: AxisSplitLine;

  /**
   * 坐标轴箭头
   */
  axisArrow?: AxisArrow;

  /**
   * 坐标轴描述
   */
  axisDesc?: AxisDesc;

  /**
   * 坐标轴line
   */
  axisLine?: AxisLine;

  /**
   * 是否需要展示双Y轴
   * @default false
   */
  needDoubleYAxis?: boolean;

  /**
   * 横坐标轴是否为时间轴
   * @default true
   */
  isTimeAxis?: boolean;

  /**
   * 数据的值域，值域的值尽量设置为 5 或 10 的整数倍，请勿将 domain 设置为 0, 0
   */
  axisDomain?: AxisDomain;

  /**
   * 坐标轴tick展示格式
   */
  axisTickFormatter?: AxisTickFormatter;

  /**
   * 坐标轴左右padding
   *
   * 注：坐标轴展示箭头时，需要给一定的padding
   *
   * @default 0
   */
  padding?: number;

  /**
   * X轴首部留白间距大小
   *
   * @default 20
   */
  paddingXForHead?: number;

  /**
   * X轴尾部留白间距大小
   *
   * @default 10
   */
  paddingXForTail?: number;

  /**
   * 坐标轴diffSize，用于处理坐标轴上点显示不全问题
   *
   * `注：适用于折线图&散点图，开启滚轮缩放时建议不使用`
   *
   * @default 1
   */
  axisDiffSize?: number;

  /**
   * y轴坐标轴类型，默认为线性增长
   *
   * @default linear
   */
  yAxisType?: 'linear' | 'sqrt';

  /**
   * y轴tick高度
   * @default 40
   */
  yTickHeight?: number;
}

export interface AxisTickFormatter {
  /**
   * x轴
   */
  xAxisTickFormatter?: (tick: Date | string) => string;

  /**
   * y轴
   */
  yAxisTickFormatter?: (tick: string) => string;

  /**
   * y2 轴
   */
  y2AxisTickFormatter?: (tick: string) => string;
}

export interface AxisDomain {
  /**
   * y轴
   */
  yDomain?: number[];

  /**
   * y2轴
   */
  y2Domain?: number[];
}

export interface AxisLine {
  /**
   * X轴是否展示line
   *
   * @default true
   */
  xAxisNeedLine?: boolean;

  /**
   * Y轴是否展示line
   *
   * @default false
   */
  yAxisNeedLine?: boolean;
}

export interface AxisSplitLine {
  /**
   * X轴是否展示SplitLine，即纵向splitline
   *
   * @default false
   */
  xAxisNeedSplitLine?: boolean;

  /**
   * Y轴是否展示SplitLine，即横向splitline
   *
   * @default true
   */
  yAxisNeedSplitLine?: boolean;
}

export interface AxisArrow {
  /**
   * X轴是否需要箭头
   *
   * @default false
   */
  xAxisNeedArrow?: boolean;

  /**
   * Y轴是否需要箭头
   *
   * @default false
   */
  yAxisNeedArrow?: boolean;
}

export interface AxisDesc {
  /**
   * X轴描述
   */
  xAxisDesc?: string | JSX.Element;

  /**
   * Y轴描述
   */
  yAxisDesc?: string | JSX.Element;

  /**
   * Y2轴描述，即双纵轴时，`负方向`轴描述
   */
  y2AxisDesc?: string | JSX.Element;
}

export interface AxisZoom {
  /**
   * 是否需要开启滚轮缩放效果
   *
   * @default false
   */
  show?: boolean;

  /**
   * X轴是否需要缩放
   *
   * @default true
   */
  xAxisNeedZoom?: boolean;

  /**
   * Y轴是否需要缩放
   *
   * @default true
   */
  yAxisNeedZoom?: boolean;
}

export interface AxisBrushZoom {
  /**
   * 是否需要开启选框缩放效果
   *
   * @default false
   */
  show?: boolean;

  /**
   * 是否仅需要开启选框效果
   *
   * @default false
   */
  onlyBrush?: boolean;

  /**
   * 是否需要开启Y轴选框自定义大小效果
   *
   * 注：仅支持onlyBrush为true的情况
   *
   * @default false
   */
  showCustomBrushY?: boolean;

  /**
   * 选框Tooltip渲染
   *
   * `仅支持仅支持原生dom组成的字符串`
   */
  genBrushTooltip?: (xValues: Array<Date | string>, yValues: number[], x?: number, y?: number) => string;

  /**
   * 选框Tooltip 点击事件回调
   */
  brushTipClickCallback?: (event, xValues?: Array<Date | string>, yValues?: number[]) => void;

  /**
   * Y轴是否需要缩放
   *
   * @default true
   */
  yAxisNeedBrushZoom?: boolean;

  /**
   * 重置文本
   */
  resetText?: string;

  /**
   * 框选回调函数
   *
   * yValues: 单Y轴返回 `[ymin, ymax]`, 如 `[10, 40]`, 双Y轴返回 `[ymin, ymax, y2min, y2max]`
   */
  brushCallback?: (xValues: Array<Date | string>, yValues?: Array<number>) => void;
}

export const toolbarDefaultOptions = {
  show: true,
  saveAsImage: { show: true, title: '数据图' },
  zoom: {
    show: false,
  },
  more: null,
  customIconList: [],
};

export const compareLineDefaultOptions = {
  isCompare: false,
  calculatePeriod: (startTime, endTime) => {
    return getStatisticPeriod(startTime, endTime);
  },
};

export const axisZoomDefault = {
  show: false,
  xAxisNeedZoom: true,
  yAxisNeedZoom: true,
};

export const axisBrushDefault = {
  show: false,
  yAxisNeedBrushZoom: true,
  onlyBrush: false,
  resetText: '重置',
};

export const LegendDefault = {
  show: true,
  disabledKeys: [],
};

export const AxisSplitLineDefault = {
  xAxisNeedSplitLine: false,
  yAxisNeedSplitLine: true,
};

export const AxisArrowDefault = {
  xAxisNeedArrow: false,
  yAxisNeedArrow: false,
};

export const AxisDescDefault = {
  xAxisDesc: '',
  yAxisDesc: '',
  y2AxisDesc: '',
};

export const AxisLineDefault = {
  xAxisNeedLine: true,
  yAxisNeedLine: false,
};

export const TitleDefault = {
  titleFontSize: 14,
  titleColor: '#000',
  descFontSize: 12,
  descColor: '#888',
  showBubble: false,
  iconType: 'info',
};

export const TooltipDefault = {
  show: true,
} as Tooltip;
