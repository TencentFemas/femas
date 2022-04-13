/**
 * @method formatDate
 * @param {Object} date Date对象
 * @param {String} fmt 格式化目标格式
 * @description 定义Date发format方法用于格式化日期
 * @author brianlin
 */
export default function formatDate(date, fmt = 'yyyy-MM-dd hh:mm:ss') {
  if (!date) {
    return '';
  }
  if (typeof date === 'string') {
    return date;
  }
  // 兼容时间戳
  if (typeof date === 'number') {
    date = new Date(date);
  }
  let result = fmt;
  if (/(y+)/.test(result)) result = result.replace(RegExp.$1, `${date.getFullYear()}`.substr(4 - RegExp.$1.length));
  const expMap = {
    'M+': date.getMonth() + 1,
    'd+': date.getDate(),
    'h+': date.getHours(),
    'm+': date.getMinutes(),
    's+': date.getSeconds(),
    'S+': date.getMilliseconds(),
  };
  for (const exp in expMap) {
    if (new RegExp(`(${exp})`).test(fmt)) {
      result = result.replace(RegExp.$1, `${expMap[exp]}`.padStart(RegExp.$1.length, '0'));
    }
  }
  return result;
}
