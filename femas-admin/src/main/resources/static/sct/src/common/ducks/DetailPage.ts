import { reduceFromPayload } from 'saga-duck';
import PageDuck from '../ducks/Page';
import { call, fork, put, select } from 'redux-saga/effects';
import { runAndTakeLatest, takeLatest } from 'redux-saga-catch';
import { delay } from 'redux-saga';
import { Completer, OperationNoTarget, OperationSingle, OperationType, UPDATE } from './GridPage';
import { notification } from 'tea-component';

export type Operation<T> = OperationNoTarget | OperationSingle<T>;

enum Types {
  SET_ID,
  RELOAD,
  FETCH,
  FETCH_DONE,
  FETCH_FAIL,
  UPDATE,
}

export default abstract class DetailPage extends PageDuck {
  /** 联合ID 类型定义 */
  abstract ComposedId;
  /** 数据 类型定义 */
  abstract Data;

  get quickTypes() {
    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  /** 是否需要初始加载 */
  get initialFetch() {
    return true;
  }

  get params() {
    const { types } = this;
    return [
      ...super.params,
      {
        key: 'id',
        defaults: '',
        type: types.SET_ID,
      },
    ];
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      id: reduceFromPayload(types.SET_ID, ''),
      loading: (state = this.initialFetch, action): boolean => {
        switch (action.type) {
          case types.FETCH:
            return true;
          case types.FETCH_DONE:
          case types.FETCH_FAIL:
            return false;
          default:
            return state;
        }
      },
      error: (state = null, action) => {
        switch (action.type) {
          case types.FETCH:
          case types.FETCH_DONE:
            return null;
          case types.FETCH_FAIL:
            return action.payload;
          default:
            return state;
        }
      },
      data: (state: this['Data'] = null, action): this['Data'] => {
        switch (action.type) {
          case types.FETCH_DONE:
            return action.payload;
          case types.UPDATE:
            return Object.assign({}, state, action.payload);
          default:
            return state;
        }
      },
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      /** 查询条件，按需重写，比如加上regionId */
      composedId: (state: State): this['ComposedId'] => state.id,
      id: (state: State) => state.id,
      loading: (state: State) => state.loading,
      error: (state: State) => state.error,
      data: (state: State): this['Data'] => state.data,
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      reload: () => ({ type: types.RELOAD }),
    };
  }

  get GetDataParams(): this['ComposedId'] {
    return null;
  }

  get GetDataResult(): Promise<this['Data']> {
    return null;
  }

  /** 哪些Action触发页面刷新 */
  get watchTypes() {
    const { types } = this;
    return [types.SET_ID];
  }

  // 对于与列表页（GridPage）结合的详情页，这里设计为可以复用列表页的Completer
  get Completer(): new (filter?) => Completer<any, any, any> {
    return null;
  }

  /** Completer类型声明 */
  get CompleterClass(): new (filter?) => Completer<any, any, any> {
    return null;
  }

  /** 对象操作快速映射 */
  get operations(): Operation<this['Data']>[] {
    return [];
  }

  /** 对象操作类型声明 */
  get Operations(): Operation<this['Data']>[] {
    return null;
  }

  /** async版的getData，严格约束返回类型 */
  abstract getData(composedId: this['GetDataParams']): this['GetDataResult'];

  *saga() {
    yield* super.saga();
    yield fork([this, this.sagaDetailPageMain]);
  }

  *sagaDetailPageMain() {
    yield* this.sagaWatchLoadData();
  }

  *sagaWatchLoadData() {
    const duck = this;
    const { types, watchTypes, initialFetch } = duck;
    const helper = initialFetch ? runAndTakeLatest : takeLatest;
    // 初始时，以及ID切换时加载数据
    yield helper([...watchTypes, types.RELOAD], function* detailFetchData() {
      // 缓冲
      yield call(delay, 10);
      yield* duck.sagaLoadData();
    });
  }

  /** 加载数据，返回 dataFetched as boolean  */
  *sagaLoadData() {
    const duck = this;
    const { types, selectors } = duck;
    // 也可以考虑进行一次深度对比，如果没有变化就无视
    yield put({ type: types.FETCH });

    const Completer = this.Completer;
    const composedId = selectors.composedId(yield select());

    // 数据补全器
    const completer = Completer ? new Completer() : null;
    if (completer) {
      yield fork([completer, completer.init], composedId);
    }
    try {
      const data = yield call([duck, duck.getData], composedId);
      yield put({ type: types.FETCH_DONE, payload: data });
    } catch (e) {
      yield put({ type: types.FETCH_FAIL, payload: e });
      return false;
    }
    if (completer) {
      const list = [selectors.data(yield select())];
      yield fork([completer, completer.fetched], list, list.length, this.getCompleterUpdater());
    }

    // 在加载完后才监听操作
    yield* this.sagaOperationsWatch();
  }

  getCompleterUpdater(): UPDATE<any> {
    const { types } = this;
    // 补全回调
    return function*(index: number | any[], data?: any) {
      // 列表更新
      if (arguments.length <= 1) {
        data = index[0];
        index = 0;
      }
      if (index === 0) {
        yield put({
          type: types.UPDATE,
          payload: data,
        });
      }
    };
  }

  /** 各操作映射 */
  *sagaOperationsWatch() {
    const { selector, creators, operations } = this;
    for (const operation of operations) {
      let wappedSaga: (action?: any) => any;
      switch (operation.type) {
        // 无目标操作
        case OperationType.NO_TARGET:
          wappedSaga = operation.fn;
          break;
        // 单条操作映射
        case OperationType.SINGLE:
          wappedSaga = function*(action) {
            const { data } = selector(yield select());
            return yield operation.fn(data, action);
          };
          break;
      }
      if (!wappedSaga) {
        continue;
      }
      yield takeLatest(operation.watch, function*(action) {
        const success = yield wappedSaga(action);
        if (success && operation.successTip) {
          notification.success({ description: operation.successTip });
        }
        if (success && operation.reload !== false) {
          yield put(creators.reload());
        }
      });
    }
  }
}
