import { delay } from 'redux-saga';
import { UPDATE } from './Base';
import { wrapListUpdate } from './wrapListUpdate';

/**
 * 列表更新缓冲，合并多个更新请求，配置 Completer使用
 *
 * 示例：
 * ```ts
 * const update: UPDATE<{foo: string}> = ...
 * const bufferUpdate = getBufferUpdate(update, 100)
 * // 以下零散频繁的操作
 * yield* bufferUpdate(0, {foo: 'a'})
 * yield* bufferUpdate(1, {foo: 'b'})
 * yield* bufferUpdate([null, {bar:'c'}, {foo:'d'}])
 * // 最终会合并为下面的操作
 * yield* update([{foo: 'a'}, {foo: 'b', bar: 'c'}, {foo:'d'}])
 * ```
 *
 * @param updateList 列表更新方法
 * @param buffer 缓冲时间，单位毫秒，默认为100
 */
export function getBufferUpdate<T>(_updateList: (list: T[]) => IterableIterator<any>, buffer = 100): UPDATE<T> {
  let cacheList: T[];
  let lastToken: symbol = null;
  let lastTime: number = Date.now();

  /**
   * 缓存版批量更新列表
   */
  function* updateList(list: T[]) {
    if (!cacheList) {
      cacheList = list;
    } else {
      cacheList = cacheList.slice(0);
      list.forEach((v, index) => {
        if (!v) {
          return;
        }
        cacheList[index] = Object.assign({}, cacheList[index], v);
      });
    }
    yield* bufferFlush();
  }

  /**
   * 提交缓存中的改动
   */
  function* bufferFlush() {
    // 目前的策略是，达到了buffer间隔就立即更新
    const ellapsed = Date.now() - lastTime;
    const token = (lastToken = Symbol('flushToken'));

    if (ellapsed < buffer) {
      // 如果距离上次执行未超过时限，延迟执行
      yield delay(buffer - ellapsed);
    }
    // token不一致，跳过
    if (token !== lastToken) {
      return;
    }
    // 清空列表并执行
    const _list = cacheList;
    cacheList = null;
    lastTime = Date.now();
    yield* _updateList(_list);
  }

  return wrapListUpdate(updateList);
}
