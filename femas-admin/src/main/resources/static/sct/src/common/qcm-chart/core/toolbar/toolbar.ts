/* eslint-disable prettier/prettier */
// @ts-nocheck
import * as d3 from 'd3';
import {generateToolbarContent} from './toolbarContent';
import {GraphConfig, toolBarTheme} from '../../theme/theme';
import {drawCustomIcon, drawDownload, drawZoomIcon} from './icon';
import {toolbarDefaultOptions} from '../../type/chart';

export const drawToolBar = context => {
  let {toolBar = {}} = context.props;
  toolBar = {...toolbarDefaultOptions, ...toolBar};

  const {svgGroup, diagramWidth, parentId} = context.state;
  if (!toolBar.show) return;

  svgGroup.select('g.tool-bar').remove();

  const bar = svgGroup
  .append('g')
  .attr('class', 'tool-bar')
  .attr('cursor', 'pointer')
  .attr(
      'transform',
      // 16为icon宽度
      `translate(${diagramWidth - 16}, ${GraphConfig.MINI_PADDING})`,
  );

  // 自定义icon
  toolBar.customIconList?.length && drawCustomIcon(context, bar);

  // 渲染保存图片
  toolBar.saveAsImage.show && drawDownload(context, bar);

  // 渲染放缩
  toolBar.zoom.show && drawZoomIcon(context, bar);

  if (!toolBar.more) return;

  // 渲染更多
  const moreGroup = bar
  .append('g')
  .attr('class', 'more')
  .attr('transform', `translate(0, -${toolBarTheme.padding})`)
  .on('click', function () {
    d3.event.stopPropagation();
    // 展开菜单
    d3.select(`#${parentId}`)
    .select('section.chart')
    .selectAll('div.toolbar')
    .data([null])
    .join('div')
    .attr('class', 'toolbar');

    const toolbar = document.querySelector(`#${parentId} .toolbar`);

    toolbar.setAttribute('style', 'display: block');
    toolbar.innerHTML = generateToolbarContent(toolBar.more, diagramWidth);

    toolbar.addEventListener('click', function (e) {
      const target = e.target as any;
      const index = target.getAttribute('index');
      const item = toolBar.more[index];
      // 调用more中对应的onSelect方法
      item.onSelect && item.onSelect(item.value);
      toolbar.setAttribute('style', 'display: none');
    });
  });

  moreGroup
  .append('foreignObject')
  .attr('width', toolBarTheme.rectWidth)
  .attr('height', toolBarTheme.rectHeight)
  .html('<i type="more" class="tea-icon tea-icon-more"></i>');
};
