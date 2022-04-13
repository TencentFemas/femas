import { delay } from 'redux-saga';
import { all, call } from 'redux-saga/effects';

/**
 * 状态判断函数
 */
export type JudgeFunction = () => boolean;

/**
 * 状态变化监听，只触发一次
 */
export type WatchOnceFunction = (callback: () => void) => void;

/**
 * 条件等待函数
 * @param buffer 当条件满足时，延时buffer毫秒后完成，避免切换过于频繁
 * @returns 如果当前条件满足，返回true；否则等待条件满足后，返回false
 */
export type WaitFunction = (buffer?: number) => Promise<boolean>;

export function makeWaitFunction(judge: JudgeFunction, watch: WatchOnceFunction, defaultBuffer?: number): WaitFunction {
  async function waitReady() {
    return new Promise(resolve => {
      watch(() => resolve(true));
    });
  }

  return async function wait(buffer: number = defaultBuffer) {
    if (judge() === true) {
      // 同步完成
      return true;
    }
    do {
      await waitReady();
      await delay(buffer);
    } while (judge() !== true);
    // 异步完成
    return false;
  };
}

/**
 * 组合多个锁，只有全为true时能完成
 * @param waitFunctions
 * @param buffer 缓冲时间，避免快速切换
 */
export async function combineWaitFunction(waitFunctions: WaitFunction[], buffer = 1000) {
  let results: boolean[];
  // 是否是同步完成，即未等待状态
  let immediately = true;
  do {
    results = await Promise.all(waitFunctions.map(fn => fn(buffer)));
    if (results.some(x => x !== true)) {
      // 有任务等待后才完成，需要继续来一次确认全部状态
      immediately = false;
    } else {
      // 全部判断同步完成，满足条件
      break;
    }
    // 防死循环
    await delay(16);
  } while (true);
  return immediately;
}

/**
 * saga版的 combineWaitFunction
 */
export function* combineWaitSagas(waitTasks: WaitTask[], buffer = 1000) {
  let results: boolean[];
  // 是否是同步完成，即未等待状态
  let immediately = true;
  do {
    results = yield all(waitTasks.map(fn => call(fn, buffer)));
    if (results.some(x => x !== true)) {
      // 有任务等待后才完成，需要继续来一次确认全部状态
      immediately = false;
    } else {
      // 全部判断同步完成，满足条件
      break;
    }
    // 防死循环
    yield delay(16);
  } while (true);
  return immediately;
}

type WaitSaga = (buffer?: number) => Generator<any, boolean, any>;
type WaitTask = WaitFunction | WaitSaga;
