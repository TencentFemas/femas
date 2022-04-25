// @ts-nocheck
import { InnerRadiusDiff, NodeRadius, ReqFontSize, TextDiff, TextDiffEm } from '../../theme/theme';
import * as d3 from 'd3';
import { LayoutType } from '../../type/topology';

// 获取points
export const getLinkPoints = (link, layoutType) => {
  const { source, target } = link;
  const points = [];
  if (source.x === undefined || target.x === undefined) {
    // 容错处理
    return points;
  }
  const dx = target.x - source.x;
  const dy = target.y - source.y;

  // 考虑箭头，radius+10
  const targetDisX = ((target.nodeRadius + 5) * Math.abs(dx)) / Math.sqrt(dx * dx + dy * dy);
  const targetDisY = ((target.nodeRadius + 5) * Math.abs(dy)) / Math.sqrt(dx * dx + dy * dy);
  const sourceDisX = ((source.nodeRadius + 5) * Math.abs(dx)) / Math.sqrt(dx * dx + dy * dy);
  const sourceDisY = ((source.nodeRadius + 5) * Math.abs(dy)) / Math.sqrt(dx * dx + dy * dy);

  const sourceX = source.x + (dx > 0 ? 1 : -1) * sourceDisX;
  const sourceY = source.y + (dy > 0 ? 1 : -1) * sourceDisY;
  const targetX = target.x + (dx > 0 ? -1 : 1) * targetDisX;
  const targetY = target.y + (dy > 0 ? -1 : 1) * targetDisY;

  const midX = sourceX + (targetX - sourceX) / 2;
  const midY = sourceY + (targetY - sourceY) / 2;

  if (link.isOwn) {
    // 自调用节点
    return [
      {
        x: source.x + source.nodeRadius - 10,
        y: source.y + source.nodeRadius / 2,
      },
      {
        x: source.x + source.nodeRadius * 2,
        y: source.y,
      },
      {
        x: source.x + source.nodeRadius,
        y: source.y - source.nodeRadius / 2,
      },
    ];
  }

  // 调整连线上文字的方向，根据dx考虑先放source还是target
  // 对应于使用marker-end还是mark-start
  points.push({ x: dx > 0 ? sourceX : targetX, y: dx > 0 ? sourceY : targetY });

  // 力导图才需处理
  if (layoutType === LayoutType.force) {
    if (link.linkNum % 2 === 0) {
      // 下
      points.push({
        x: midX + (Math.abs(dy) > 100 ? 10 : 0),
        y: midY + (Math.abs(dx) > 100 ? 50 : 10),
      });
    } else {
      // 上
      points.push({
        x: midX - (Math.abs(dy) > 100 ? 10 : 0),
        y: midY - (Math.abs(dx) > 100 ? 50 : 10),
      });
    }
  }

  points.push({ x: dx > 0 ? targetX : sourceX, y: dx > 0 ? targetY : sourceY });

  return points;
};

// 处理id 以便select 进行ID选择
export const convertNodeId = id => {
  return encodeURIComponent(btoa(encodeURIComponent(id))).replace(/%/g, '_');
};

export const calNodeSize = (rangs, node, nodeCalMode) => {
  const index = node[nodeCalMode] ? d3.bisector(d => d).left(rangs, node[nodeCalMode], 0) : 0;
  return {
    nodeRadius: NodeRadius[index],
    innerNodeRadiusDiff: InnerRadiusDiff[index],
    reqFontSize: ReqFontSize[index],
    textDiffEm: TextDiffEm[index],
    textDiff: TextDiff[index],
  };
};

// 计算数据等分区间
export const calculateRange = (datas, nodeCalMode) => {
  const rangs = [0, 0, 0, 0, 0];
  const nums = datas?.filter(d => d[nodeCalMode] !== null && d[nodeCalMode] !== undefined)?.map(d => d[nodeCalMode]);
  const min = d3.min(nums);
  const max = d3.max(nums);
  const diff = Math.ceil((max - min) / 4);
  return rangs.map((d, i) => min + diff * i);
};

export const convertImgToBase64 = (url, width, height, callback) => {
  if (!url) return;
  let canvas = document.createElement('canvas'); //创建canvas DOM元素
  const ctx = canvas.getContext('2d');
  const img = new Image();
  img.src = url;
  img.crossOrigin = 'Anonymous';
  // eslint-disable-next-line prettier/prettier
  img.onload = function () {
    canvas.height = width; //指定画板的高度,自定义
    canvas.width = height; //指定画板的宽度，自定义
    ctx.drawImage(img, 0, 0, width, height); //参数可自定义
    const dataURL = canvas.toDataURL('image/png');
    callback.call(this, dataURL); //回掉函数获取Base64编码
    canvas = null;
  };
};
