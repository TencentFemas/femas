/* eslint-disable prettier/prettier */
/**
 * csv导出相关
 * Created by cluezhang on 2016/6/7.
 */
export interface CsvColumn<TRecord = any, TProperty extends keyof TRecord = keyof TRecord> {
  /** 表头 */
  key: TProperty;
  /** 字段属性名 */
  name: string;
  /** 渲染函数，如果不指定，则直接使用原始值 */
  renderer?: (value: TRecord[TProperty], record?: TRecord, rowIndex?: number, meta?: any) => string;
}

// csv转义
export function encode(v: string): string {
  v = '' + (v === undefined || v === null ? '' : v);
  if (/[",\r\n]/.test(v)) {
    return '"' + v.replace(/"/g, '""') + '"';
  }
  return v;
}

/**
 * 将列表数据转成csv格式，使用类似grid columns配置进行列定义。
 * @param {Array} list 列表数据
 * @param {Array} columns 列定义，格式示例：
 *      [
 *          { name : '列头', key : '字段名', [renderer]:function(字段值, item, index, meta){ return 处理过的字串 } },
 *      ]
 * @param {Object} meta 额外信息，会传递给column.renderer
 * @param {Object} headerInfo 表头信息，格式示例
 *      [
 *          { label : '字段名称', value : '字段值'},
 *      ]
 * @returns {string}
 */
export function format<T = any>(
    list: T[],
    columns: CsvColumn<T>[],
    meta = null,
    headerInfo: { label: string; value: string }[] = [],
) {
  const nl = '\r\n';
  let lines = [];
  // 表头信息
  if (headerInfo.length > 0) {
    lines.push(
        headerInfo
        .map(function (info) {
          return info.label + info.value;
        })
        .map(encode),
    );
  }
  // csv头
  lines.push(
      columns
      .map(function (col) {
        return col.name;
      })
      .map(encode)
      .join(','),
  );
  // 列表
  lines = lines.concat(
      list.map(function (item) {
        return columns
        .map(function (col, index) {
          const value = item[col.key];
          return col.renderer ? col.renderer(value, item, index, meta) : value;
        })
        .map(encode)
        .join(',');
      }),
  );

  return lines.join(nl);
}

// 字节序标记，用于windows下识别为utf8文件。
export const BOM = '\ufeff';

/**
 * 是否支持下载
 */
export function isSupportSave() {
  return !/MSIE [1-9]\./.test(navigator.userAgent);
}

// Chrome data URI 目前最大支持2M，超出的会下载失败
const chromeMaxSize = 2 * 1024 * 1024;

/**
 * 保存文件，纯客户端实现
 * 需要IE10+
 * @returns invalid 如果出错，则返回具体消息。否则返回undefined
 */
export function save(fileName: string, csvString: string, onExceedTruncate = true): string {
  if (csvString.charAt(0) !== BOM) {
    csvString = BOM + csvString;
  }
  if (window.navigator.msSaveOrOpenBlob) {
    const blob = new Blob([csvString]);
    window.navigator.msSaveOrOpenBlob(blob, fileName);
  } else {
    let dataURI = 'data:attachment/csv,' + encodeURIComponent(csvString);
    let message;
    // 人为限定2M？chrome只支持这个数
    if (/Chrome\/\d+/.test(navigator.userAgent) && dataURI.length > chromeMaxSize) {
      message = '因为浏览器限制，此文件仅保存了部分内容';
      if (!onExceedTruncate) {
        return message;
      }
      dataURI = dataURI.slice(0, chromeMaxSize);
    }
    const a = document.createElement('a');
    a.href = dataURI;
    a.target = '_blank';
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);

    return message;
  }
}
