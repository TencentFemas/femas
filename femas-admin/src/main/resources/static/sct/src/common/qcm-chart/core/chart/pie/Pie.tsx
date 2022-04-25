/* eslint-disable prettier/prettier */
// @ts-nocheck
import Base from '../Base';
import * as d3 from 'd3';
import {originColors, pieTheme} from '../../../theme/theme';
import {generateCustomTooltip, generatePieTooltip} from '../../tooltip/Tooltip';
import {calTooltipPos, convertTranslate, nodeWidth} from '../../lib/helper';
import {DataProps, Value} from '../../../type/chart';

export interface PieProps {
  /**
   * 圆大小
   *
   * 默认根据外层容器大小进行计算
   *
   */
  outerRadius?: number;

  /**
   * 圆环大小
   *
   */
  ringSize?: number;
  /**
   * 中心Title，一张图可绘制多个环形图
   *
   * 建议`环形图`中使用
   *
   * @default name
   *
   */
  centerTitle?: Array<Array<CenterTitle> | string>;

  /**
   * 数据文本
   */
  dataLabels?: Label;

  /**
   * 选中
   */
  selected?: Selected;
}

export interface CenterTitle {
  /**
   * 展示文本
   */
  text: string;
  /**
   * 元素样式
   *
   * 如： 'font-size: 12px;fill:red;'
   */
  textStyle?: string;

  /**
   * x 偏移量
   */
  offsetX?: number;

  /**
   * y 偏移量
   */
  offsetY?: number;
}

export interface Selected {
  /**
   * 是否需要展示选中效果
   *
   * @default false
   */
  show?: boolean;

  /**
   * 默认选中的数据
   *
   * `label`
   */
  selectedLabel?: string;

  /**
   * 选中回调函数
   */
  selectedCallback?: (item: Value) => void;
}

export interface Label {
  /**
   * 是否展示
   *
   * @default false
   */
  show?: boolean;

  /**
   * 展示内容
   */
  formatter?: (data: Value, total: number) => string;
}

const LabelDefault = {
  show: false,
  formatter: (data: Value, total: number) => {
    const {value} = data;
    return `${value} (${total ? ((value / total) * 100).toFixed(2) : 0}%)`;
  },
};

const SelectedDefault = {
  show: false,
};

export class Pie extends Base<PieProps & DataProps> {
  get colors() {
    const {colors: propColors} = this.props;
    const {convertedData} = this.state;
    const dataColors = {};
    const labels = [];
    // 根据name来设定颜色
    convertedData.forEach(d => {
      labels.push(...d.value);
    });
    labels.forEach((v, i) => {
      dataColors[v.label] = (propColors || originColors)[i];
    });
    return dataColors;
  }

  get diffHeight() {
    const {diagramHeight} = this.state;
    return diagramHeight / 2;
  }

  get pieWidth() {
    const {convertedData, diagramWidth} = this.state;
    return diagramWidth / convertedData.length;
  }

  get radius() {
    const {outerRadius} = this.props;
    if (outerRadius) return outerRadius;
    return Math.min((this.pieWidth - this.padding) / 2, pieTheme.maxRadius);
  }

  get innerRadius() {
    const {ringSize = this.radius} = this.props;
    return !ringSize || this.radius - ringSize < 0 ? 0 : this.radius - ringSize;
  }

  // 处理Tooltip

  get arc() {
    return d3
    .arc()
    .innerRadius(this.innerRadius)
    .outerRadius(this.radius);
  }

  get hoverArc() {
    return d3
    .arc()
    .innerRadius(this.innerRadius)
    .outerRadius(this.radius + pieTheme.diffRadius);
  }

  get labelsInnerArc() {
    return d3
    .arc()
    .innerRadius(this.radius)
    .outerRadius(this.radius);
  }

  get pie() {
    return d3
    .pie()
    .value(d => d.value)
    .startAngle(90 * (Math.PI / 180))
    .endAngle(450 * (Math.PI / 180));
  }

  convertData() {
    const {data} = this.props;
    const {disabledKeys} = this.legendOptions;

    // const disableLegend = []
    data.forEach((d: any, i) => {
      d.value.forEach((v: any) => {
        v.pieIndex = i;
        // if (!v.value) {
        //   disableLegend.push(v.label)
        // }
      });
      d.pieIndex = i;
    });

    // 初始化legendDisableKeys
    this.legendDisableKeys = disabledKeys;
    this.setState({
      convertedData: data,
    });
  }

  reload() {
    // reload时重新渲染
    const {pieGroup, svgGroup} = this.state;
    if (pieGroup !== null) {
      svgGroup.selectAll('g').remove();
      this.setState({
        pieGroup: null,
      });
    }
    super.reload();
  }

  drawChart() {
    // 可支持展示多个pie
    const {svgGroup, pieGroup, convertedData, diagramWidth} = this.state;
    const {show} = {
      ...SelectedDefault,
      ...this.props.selected,
    };

    if (!convertedData || !convertedData.length) return;

    if (pieGroup && pieGroup !== null) {
      return;
    }
    const curPieGroup = svgGroup.append('g').attr('class', 'd3-pie');
    this.overlayRect
    .attr('width', diagramWidth)
    .attr('height', this.height)
    .attr('transform', `translate(0, ${this.topPadding})`)
    .lower()
    .on('click', () => {
      if (!show) return;

      this.initSelected();
    });
    this.setState(
        {
          pieGroup: curPieGroup,
        },
        async () => {
          this._drawPies();
        },
    );
  }

  initSelected() {
    const {pieGroup} = this.state;
    pieGroup
    .select('g.pies')
    .selectAll('g.pie-item')
    .selectAll('g.pie-path')
    .select('g.selected-pie')
    .select('path')
    .transition()
    .duration(600)
    .attr('opacity', 0)
    .ease();
  }

  _drawPies() {
    const {pieGroup, convertedData, parentId} = this.state;
    const {centerTitle, ringSize} = this.props;
    const {pieWidth, diffHeight, legendDisableKeys} = this;
    const {show, formatter} = {...LabelDefault, ...this.props.dataLabels};
    const {show: selectedShow, selectedCallback, selectedLabel} = {
      ...SelectedDefault,
      ...this.props.selected,
    };
    const {show: needTooltip} = this.tooltip;

    pieGroup.select('g.pies').remove();

    const self = this;

    const pieItem = pieGroup
    .append('g')
    .attr('class', 'pies')
    .selectAll('g.pie-item')
    .data(
        convertedData.map(d => ({
          ...d,
          total: d.value.reduce((pre, cur) => pre + cur.value, 0),
        })),
    )
    .enter()
    .append('g')
    .attr('class', 'pie-item')
    .attr('transform', (d, i) => `translate(${pieWidth / 2 + i * pieWidth}, ${diffHeight})`);

    // 渲染中心 title
    if (ringSize && centerTitle) {
      pieItem
      .selectAll('text.center-title')
      .data(d => {
        const title = centerTitle[d.pieIndex] || d.name;
        if (typeof title === 'string') return [title];
        return title;
      })
      .enter()
      .append('text')
      .attr('class', 'center-title')
      .text(d => {
        let title = d;
        if (typeof d === 'object') {
          title = d.text;
        }
        return title;
      })
      .attr('style', d => d.textStyle || 'font-size: 12px;')
      .attr('transform', function (d) {
        return `translate(-${nodeWidth(d3.select(this).node()) / 2 + (d.offsetX || 0)}, ${d.offsetY || 0})`;
      });
    }

    let index = 0;

    const pie = pieItem
    .selectAll('g.pie-path')
    .data(d =>
        [
          ...this.pie(
              d.value
              .map(v => ({
                ...v,
                originName: d.name,
                unit: d.unit,
                additionalTip: d.additionalTip,
                total: d.total,
              }))
              .filter(d => legendDisableKeys.indexOf(d.label) < 0),
          ).sort((cur, next) => cur.index - next.index),
        ].map(v => {
          if (!v.value || v.endAngle - v.startAngle < Math.PI / 12) index++;
          return {
            ...v,
            zeroIndex: index,
          };
        }),
    )
    .enter()
    .append('g')
    .attr('class', 'pie-path')
    .on('mouseover', function () {
      // hover 样式
      d3.select(this)
      .select('g.hover-pie')
      .select('path')
      .transition()
      .duration(500)
      .attr('opacity', pieTheme.hoverOpacity)
      .ease();
    })
    .on('mouseleave', function () {
      // 还原样式
      d3.select(this)
      .select('g.hover-pie')
      .select('path')
      .transition()
      .attr('opacity', 0)
      .ease();
    })
    .on('mousemove', function ({data}) {
      // eslint-disable-next-line @typescript-eslint/camelcase
      const [mouse_x, mouse_y] = d3.mouse(this);
      if (!needTooltip) return;
      self.tooltipCallback({...data, name: data.label}, mouse_x, mouse_y);
    })
    .on('mouseout', function () {
      if (!needTooltip) return;
      const tooltip = document.querySelector(`#${parentId} .tooltip`);
      needTooltip && tooltip && tooltip.setAttribute('style', 'display: none');
    })
    .on('click', function ({data}) {
      // 选中
      if (!selectedShow) return;

      self.initSelected();

      d3.select(this)
      .select('g.selected-pie')
      .select('path')
      .transition()
      .duration(600)
      .attr('opacity', 1)
      .ease();

      selectedCallback && selectedCallback(data);
    });

    pie
    .append('g')
    .attr('class', 'selected-pie')
    .append('path')
    .attr('d', d => this.hoverArc(d))
    .attr('fill', ({data}) => this.colors[data.label])
    .attr('opacity', ({data}) => (selectedLabel === data.label ? 1 : 0));

    pie
    .append('g')
    .attr('class', 'hover-pie')
    .append('path')
    .attr('d', d => this.hoverArc(d))
    .attr('fill', ({data}) => this.colors[data.label])
    .attr('opacity', 0);

    pie
    .append('g')
    .attr('class', 'real-pie')
    .append('path')
    .attr('d', d => this.arc(d))
    .attr('fill', ({data}) => this.colors[data.label]);

    if (!show) return;

    const midAngle = d => {
      return d.startAngle + (d.endAngle - d.startAngle) / 2;
    };
    // 渲染labels
    const label = pie.append('g').attr('class', 'pie-label');

    label
    .append('polyline')
    .transition()
    .duration(500)
    .attrTween('points', function (d) {
      this._current = this._current || d;
      const interpolate = d3.interpolate(this._current, d);
      return function (t) {
        const d2 = interpolate(t);
        const pos = self.labelsArc().centroid(d2);
        const tmp = [...pos];
        pos[0] = pos[0] + (midAngle(d2) % (2 * Math.PI) < Math.PI ? 1 : -1) * 20;
        tmp[0] = tmp[0] + (midAngle(d2) % (2 * Math.PI) < Math.PI ? 1 : -1) * 10;

        if (d.zeroIndex) {
          pos[1] = pos[1] + (d.zeroIndex - 1) * 10;
          tmp[1] = tmp[1] + (d.zeroIndex - 1) * 10;
        }
        return [self.labelsInnerArc.centroid(d2), tmp, pos];
      };
    })
    .style('fill', 'none')
    .style('stroke', ({data}) => this.colors[data.label])
    .attr('stroke-width', 1);

    label
    .append('text')
    .text(({data}) => {
      const {total} = data;
      return formatter(data, total);
    })
    .style('font-size', '10px')
    .transition()
    .duration(500)
    .attr('dy', '.25em')
    .attrTween('transform', function (d) {
      const interpolate = d3.interpolate(this._current, d);
      const self = this;
      return function (t) {
        const d2 = interpolate(t);
        self._current = d2;
        const pos = self.labelsArc().centroid(d2);
        pos[0] = pos[0] + (midAngle(d2) % (2 * Math.PI) < Math.PI ? 1 : -1) * 25;
        if (d.zeroIndex) {
          pos[1] = pos[1] + (d.zeroIndex - 1) * 10;
        }
        return 'translate(' + pos + ')';
      };
    })
    .styleTween('text-anchor', function (d) {
      const interpolate = d3.interpolate(this._current, d);
      return function (t) {
        const d2 = interpolate(t);
        return midAngle(d2) % (2 * Math.PI) < Math.PI ? 'start' : 'end';
      };
    });
  }

  // eslint-disable-next-line @typescript-eslint/camelcase
  tooltipCallback(hoverData, mouse_x, mouse_y) {
    const {pieWidth} = this;
    const {diagramHeight, diagramWidth, parentId} = this.state;
    const {formatter} = this.tooltip;

    const tooltipContainer = d3
    .select(`#${parentId}`)
    .select('section.chart')
    .selectAll('div.tooltip')
    .data([null])
    .join('div')
    .attr('class', 'tooltip');

    const tooltip = document.querySelector(`#${parentId} .tooltip`);

    tooltip.innerHTML = formatter
        ? generateCustomTooltip(formatter, hoverData, this.colors)
        : generatePieTooltip(hoverData, this.colors);

    calTooltipPos(
        tooltipContainer,
        diagramWidth,
        diagramHeight,
        parentId,
        // eslint-disable-next-line @typescript-eslint/camelcase
        mouse_x + pieWidth / 2 + hoverData.pieIndex * pieWidth,
        // eslint-disable-next-line @typescript-eslint/camelcase
        mouse_y + diagramHeight / 2 - this.topPadding,
    );
  }

  drawLegend() {
    const {convertedData, svgGroup} = this.state;
    const data = [];
    convertedData.forEach(d => {
      d.value.forEach(v => {
        data.push({...v, name: v.label});
      });
    });

    super.drawLegend(data);
    const translate = svgGroup.select('g.legend').attr('transform');
    const [x, y] = convertTranslate(translate);
    svgGroup.select('g.legend').attr('transform', `translate(${x}, ${y - 5})`);
  }

  // hover legend
  onLegendItemHover(data) {
    const {pieGroup} = this.state;

    pieGroup
    .select('g.pies')
    .selectAll('g.pie-item')
    .call(g => {
      g.each(function highlight({pieIndex}) {
        const ele = d3.select(this);
        if (pieIndex !== data.pieIndex) {
          return;
        }
        ele.selectAll('g.pie-path').call(g => {
          g.each(function path({data: d}) {
            if (d.label !== data.label) {
              return;
            }
            d3.select(this)
            .select('g.hover-pie')
            .select('path')
            .transition()
            .attr('opacity', pieTheme.hoverOpacity)
            .ease();
          });
        });
      });
    });
  }

  // 移出
  onLegendItemLeave() {
    const {pieGroup} = this.state;
    pieGroup
    .selectAll('g.hover-pie')
    .select('path')
    .attr('opacity', 0);
  }

  // legend click
  onLegendItemClick(data) {
    super.onLegendItemClick(data);
    // 重新绘制pie
    this._drawPies();
  }

  labelsArc() {
    return d3
    .arc()
    .innerRadius(this.radius + 10)
    .outerRadius(this.radius + 10);
  }
}
