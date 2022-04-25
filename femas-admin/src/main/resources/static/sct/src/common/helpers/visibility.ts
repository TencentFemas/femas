import { makeWaitFunction } from './wait';

const VISIBILITY_CHANGE_EVENT = 'visibilitychange';

function watchVisibleOnce(callback) {
  function cb() {
    document.removeEventListener(VISIBILITY_CHANGE_EVENT, cb);
    callback();
  }

  document.addEventListener(VISIBILITY_CHANGE_EVENT, cb);
}

/**
 * 确认当前标签页是否激活
 * 如果返回true，表示当前为可见，直接返回
 * 如果返回false，表示刚切过来
 */
const waitVisible = makeWaitFunction(isVisible, watchVisibleOnce, 1000);
export default waitVisible;

export function isVisible(): boolean {
  const state = document.visibilityState;
  // 如果不支持visibility功能，默认一直可见
  return !state || state === 'visible';
}

// 历史兼容
export { combineWaitFunction } from './wait';
