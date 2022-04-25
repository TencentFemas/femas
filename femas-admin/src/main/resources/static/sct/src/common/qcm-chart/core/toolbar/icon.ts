/* eslint-disable prettier/prettier */
// @ts-nocheck
import tips from '@src/common/util/tips';
import * as d3 from 'd3';
import {BarTheme, toolBarTheme} from '../../theme/theme';
import {toolbarDefaultOptions} from '../../type/chart';

export const drawDownload = (context, group) => {
  let {toolBar = {}} = context.props;
  const {diagramWidth, svgSelection, diagramHeight} = context.state;

  toolBar = {...toolbarDefaultOptions, ...toolBar};

  const download = group
  .append('g')
  .attr('class', 'download')
  .attr(
      'transform',
      `translate(${
          toolBar.zoom.show || toolBar.more
              ? toolBar.zoom.show && toolBar.more
              ? -BarTheme.barMargin * 2
              : -BarTheme.barMargin
              : 0
      }, -${toolBarTheme.padding})`,
  )
  .on('click', function () {
    d3.event.stopPropagation();
    // 保存图片
    svgToPng(svgSelection, diagramWidth, diagramHeight + context.padding, toolBar.saveAsImage.title);
  });

  download.append('title').text('保存为图片');

  download
  .append('foreignObject')
  .attr('width', toolBarTheme.rectWidth)
  .attr('height', toolBarTheme.rectHeight)
  .html('<i type="download" class="tea-icon tea-icon-download"></i>');
};

export const svgToPng = (svg, pngWidth, pngHeight, title) => {
  const serializer = new XMLSerializer();
  const source = '<?xml version="1.0" standalone="no"?>\r\n' + serializer.serializeToString(svg.node());
  const image = new Image();
  image.src = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(source);

  const flag = setTimeout(function () {
    const canvas = document.createElement('canvas');
    canvas.width = pngWidth;
    canvas.height = pngHeight;
    const context = canvas.getContext('2d');
    context.fillStyle = '#fff'; //设置保存后的PNG 是白色的
    context.fillRect(0, 0, pngWidth, pngHeight);
    context.drawImage(image, 0, 0, pngWidth, pngHeight);
    const url = canvas.toDataURL('image/png');
    const a = document.createElement('a');
    a.download = `${title}.png`;
    a.href = url;
    a.click();
  }, 100);

  image.onerror = function () {
    tips.error({text: '当前浏览器不支持下载，推荐使用Chrome浏览器'});
    clearTimeout(flag);
  };
};

// 缩放图标
export const drawZoomIcon = (context, group) => {
  let {toolBar = {}} = context.props;
  const {isAmplify} = context.state;
  toolBar = {...toolbarDefaultOptions, ...toolBar};

  const amplify = group
  .append('g')
  .attr('class', 'zoom')
  .attr('transform', `translate(${toolBar.more ? -BarTheme.barMargin : 0}, -${toolBarTheme.padding})`)
  .on('click', function () {
    context.setState({isAmplify: !isAmplify});
    // 调用相应的事件
    isAmplify ? toolBar.zoom.amplifyCallback() : toolBar.zoom.shrinkCallback();

    context.reload();
  });

  amplify.append('title').text(isAmplify ? '放大图片' : '缩小图片');

  amplify
  .append('foreignObject')
  .attr('width', toolBarTheme.rectWidth)
  .attr('height', toolBarTheme.rectHeight)
  .html(
      isAmplify
          ? '<i type="fullscreen" class="tea-icon tea-icon-fullscreen"></i>'
          : '<i type="fullscreenquit" class="tea-icon tea-icon-fullscreenquit"></i>',
  );
};

// 自定义图标
export const drawCustomIcon = (context, group) => {
  let {toolBar = {}} = context.props;
  toolBar = {...toolbarDefaultOptions, ...toolBar};

  let showNum = 0;
  Object.values(toolBar).forEach((v: any) => {
    if (v?.show || v?.length) {
      showNum++;
    }
  });

  const custom = group
  .selectAll('g.custom')
  .data(toolBar.customIconList)
  .enter()
  .append('g')
  .attr('class', 'custom')
  .attr(
      'transform',
      (d, i) => `translate(-${(showNum - 1) * BarTheme.barMargin + i * BarTheme.barMargin}, -${toolBarTheme.padding})`,
  )
  .on('click', function (d) {
    d.onClick && d.onClick();
  });

  custom.append('title').text(d => d.title || '自定义');

  custom
  .append('foreignObject')
  .attr('width', toolBarTheme.rectWidth)
  .attr('height', toolBarTheme.rectHeight)
  .html(d => d.icon);
};
