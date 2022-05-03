function isDevMode() {
  if (typeof window === 'undefined') {
    return true;
  }
  // 开发模式
  if (window['devmode'] && window['devmode'].getStatus() === 'on') {
    return true;
  }
  // 预览模式
  const buffetData = window['g_buffet_data'];
  if (buffetData && buffetData.version !== 'master-master') {
    return true;
  }
}

/**
 * 只在开发模式下才打印信息
 * @param any
 */
export function log(...any: any[]) {
  if (!isDevMode()) {
    return;
  }
  return console.log(...any);
}

/**
 * 只在开发模式下才打印信息
 * @param any
 */
export function warn(...any: any[]) {
  if (!isDevMode()) {
    return;
  }
  return console.warn(...any);
}

export default log;
