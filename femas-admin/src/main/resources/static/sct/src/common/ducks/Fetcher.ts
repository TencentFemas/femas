/* eslint-disable @typescript-eslint/no-unused-vars */
// 仅用于加载单项数据的Duck
import { DuckMap as Base, reduceFromPayload } from 'saga-duck';
import { call, join, put, race, spawn } from 'redux-saga/effects';
import { delay, Task } from 'redux-saga';
import { runAndTakeLatest, takeLatest } from 'redux-saga-catch';

enum Types {
  FETCH,
  FETCH_START,
  SET_LOADING,
  FETCH_DONE,
  FETCH_FAIL,
  RESET,
  RELOAD,
}

export default abstract class Fetcher extends Base {
  /** 查询参数类型定义，仅供声明及获取类型 */
  abstract Param;
  /** 查询结果类型定义，仅供声明及获取类型 */
  abstract Data;

  get quickTypes() {
    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      filter: reduceFromPayload<this['Param']>(types.FETCH_START, null),
      data: (state = null, action): this['Data'] => {
        switch (action.type) {
          case types.RESET:
          case types.FETCH_FAIL:
            return null;
          case types.FETCH_DONE:
            return action.payload;
          default:
            return state;
        }
      },
      error: (state = null, action): any => {
        switch (action.type) {
          case types.RESET:
          case types.FETCH:
          case types.FETCH_DONE:
            return null;
          case types.FETCH_FAIL:
            return action.payload;
          default:
            return state;
        }
      },
      loading: (state = false, action): boolean => {
        switch (action.type) {
          case types.FETCH_START:
            // 开启deferLoading
            return this.deferLoading <= 0;
          case types.SET_LOADING:
            return action.payload;
          case types.RESET:
          case types.FETCH_DONE:
          case types.FETCH_FAIL:
            return false;
          default:
            return state;
        }
      },
    };
  }

  get creators() {
    type Param = this['Param'];
    const { types } = this;
    return {
      ...super.creators,
      /** 按指定过滤条件加载数据 */
      fetch: (param: Param) => ({ type: types.FETCH, payload: param }),
      /** 重置数据 */
      reset: () => ({ type: types.RESET }),
      /** 重试  注意，reload只有在fetch action后才能使用，直接使用duck.fetch()是不行的 */
      reload: () => ({ type: types.RELOAD }),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      filter: (state: State) => state.filter,
      data: (state: State) => state.data,
      error: (state: State) => state.error,
      loading: (state: State) => state.loading,
      fetched: (state: State) => state.error !== null || state.data !== null,
    };
  }

  /** 请求缓冲，在此缓冲时间内不会调用 getData/getDataAsync 方法，避免重复请求 */
  get buffer() {
    return 10;
  }

  /**
   * 是否开启延迟loading展示
   *
   * 当数据加载在deferLoading毫秒内完成时（通常是有缓存），不切换loading态，避免界面跳动
   *
   * 默认为0，即不开启 */
  get deferLoading() {
    return 0;
  }

  *saga() {
    yield* super.saga();
    const duck = this;
    const { types, fetch, buffer } = duck;

    yield takeLatest(types.FETCH, function*(action) {
      yield call([duck, duck.fetchAndWatchReload], action.payload);
    });
  }

  /**
   * 参考 {#fetch} 方法，不同之处在于，它会额外监听RELOAD动作
   *
   * **注意：调用时需自行保证只有一个任务执行（即如果更改参数，需要取消前次的调用与运行）**
   *
   * **注意：请勿与`creators.fetch()`一起使用，否则会导致存在多套监听逻辑**
   * @param param
   */
  public *fetchAndWatchReload(param: this['Param']) {
    const duck = this;
    const { types } = duck;
    // 刷新
    yield runAndTakeLatest(types.RELOAD, function*() {
      yield* duck.fetch(param);
    });
  }

  /**
   * 除creators外的另一种调用方法，用于在其它duck内插入逻辑
   * @param param
   */
  public *fetch(param: this['Param']): Generator<any, this['Data'], any> {
    const duck = this;
    const { buffer, types, creators, getData, deferLoading } = duck;
    try {
      yield put({ type: types.FETCH_START, payload: param });
      // 缓冲
      if (buffer > 0) {
        yield call(delay, buffer);
      }
      // 延迟loading
      const deferTask = deferLoading > 0 ? delay(deferLoading) : null;
      // 加载数据
      const loadTask: Task = yield spawn(
        {
          fn: getData,
          context: duck,
        },
        param,
      );
      if (deferTask) {
        try {
          yield race({
            defer: deferTask,
            load: loadTask.done || loadTask?.['toPromise']?.(),
          });
        } finally {
          if (loadTask.isRunning()) {
            yield put({
              type: types.SET_LOADING,
              payload: true,
            });
          }
        }
      }
      const data = yield join(loadTask);
      // 展示
      yield put({
        type: types.FETCH_DONE,
        payload: data,
      });
      return data;
    } catch (e) {
      yield put({
        type: types.FETCH_FAIL,
        payload: e || '加载失败',
      });
      throw e;
    }
  }

  /**
   * 供子类扩展，可以为async函数或generator
   * 如果为async方法，建议扩展`getDataAsync`
   * @param param
   */
  protected *getData(param: this['Param']): Generator<any, this['Data'], any> {
    return yield call([this, this.getDataAsync], param);
  }

  /**
   * 供子类扩展，专为async设计，同时约束返回值
   * @param param
   */
  protected async getDataAsync(param: this['Param']): Promise<this['Data']> {
    return null;
  }
}

export enum FetchState {
  /** indicates the data is up to date and ready to use */
  Ready = 'Ready' as any,

  /** indicates the data is out of date, and the new data is fetching */
  Fetching = 'Fetching' as any,

  /**
   * indicates the data is out of date, and the new data fetches failed
   */
  Failed = 'Failed' as any,
}

/** state for data fetcher */
export interface FetcherState<TData> {
  /**
   * current fetch state
   * */
  fetchState: FetchState;

  /**
   * 请求是否已完成
   */
  fetched?: boolean;

  /**
   * data fetched from the last time
   * */
  data?: TData;

  /**
   * error object when in fail state
   */
  error?: any;

  /**
   * If the fetch started for a while, the loading will be true.
   * You can specific the duration by passing `loadingTolerance` when generating action creator.
   * If the duration is not specific, loading will be true as well as the fetchState gets to `Fetching`
   * */
  loading?: boolean;
}

export interface QueryState<TFilter> {
  date?: {
    /**
     * 查询的起始日期
     */
    from?: string;

    /**
     * 查询的结束日期
     */
    to?: string;

    /**
     * 跨越的天数
     */
    length?: number;

    /**
     * 区间的时间粒度
     */
    stride?: number;
  };
  paging?: {
    /** 请求的页码，从 1 开始索引 */
    pageIndex?: number;

    /** 请求的每页记录数 */
    pageSize?: number;
  };
  search?: string;
  keyword?: string;
  filter?: TFilter;
  sort?: {
    by?: string;
    desc?: boolean;
  };
}
