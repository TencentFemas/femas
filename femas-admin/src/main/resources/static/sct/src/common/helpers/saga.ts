/**
 * 在redux-saga中有一种场景，需要监听action，同时又希望只在值变化时才触发
 * @author cluezhang
 */
import { call, cancel, fork, Pattern, select, take } from 'redux-saga/effects';
import { tryCatch } from 'redux-saga-catch';
import * as deepEqual from 'fast-deep-equal';

function named<T extends Function>(name: string, fn: T) {
  Object.defineProperty(fn, 'name', {
    value: name,
  });
  return fn;
}

function watch<T>(types: Pattern, selector: (globalState: any) => T, ignoreOnce = false) {
  let lastData: T;
  return call(function*() {
    while (1) {
      // 可指定跳过首次
      if (ignoreOnce) {
        ignoreOnce = false;
      } else {
        // 等待触发
        yield take(types);
      }
      // 判断值是否有变化
      const data = selector(yield select());
      if (!deepEqual(data, lastData)) {
        lastData = data;
        // 有变化，开始执行任务
        return data;
      }
    }
  });
}

export function watchLatest<T>(
  types: Pattern,
  selector: (globalState: any) => T,
  saga: (data: T) => IterableIterator<any>,
  runFirst = false,
) {
  saga = tryCatch(saga);
  const watcher = watch(types, selector, runFirst);
  return fork(
    named(runFirst ? `runAndWatchLatest(${saga.name})` : `watchLatest(${saga.name})`, function*() {
      let lastTask;
      while (true) {
        const data: T = yield watcher;
        if (lastTask) {
          yield cancel(lastTask); // cancel is no-op if the task has already terminated
        }
        lastTask = yield fork(saga, data);
      }
    }),
  );
}

export function runAndWatchLatest<T>(
  types: Pattern,
  selector: (globalState: any) => T,
  saga: (data: T) => IterableIterator<any>,
) {
  return watchLatest(types, selector, saga, true);
}

/**
 * 用于保留saga中 `yield promise` 调用的返回值，用法：
 * ```typescript
 * const result = yield *resolvePromise(promise)
 * ```
 * @param promise
 */
export function* resolvePromise<T>(promise: Promise<T>): Generator<any, T, any> {
  return yield promise;
}
