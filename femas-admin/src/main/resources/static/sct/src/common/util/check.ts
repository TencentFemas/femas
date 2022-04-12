/**
 * 检查是否为 Object 不含null
 * @param obj
 */
export function isObject(obj) {
  return obj === Object(obj);
}

/**
 * 检查是否为数字 不含无穷
 * @param n
 */
export function isNumeric(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

/**
 * 判断ip是否合法
 *
 * @export
 * @param {*} ip
 * @returns
 */
export function isValidIp(ip) {
  return /^((2[0-4]\d|25[0-5]|[01]?\d\d?)\.){3}(2[0-4]\d|25[0-5]|[01]?\d\d?)$/.test(ip);
}

// 多个path检查 https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)
export function checkPath(v) {
  if (
    !/^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$/.test(
      v.trim(),
    )
  ) {
    return '请输入合法的URL，以 http:// 或者 https:// 开头';
  }
}
