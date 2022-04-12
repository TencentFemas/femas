/**
 * 可搜索列表，有以下功能
 *
 * 1. 搜索（自行输入条件）
 * 2. 加载更多
 */

import { ComposableDuck as Base, createToPayload, reduceFromPayload } from 'saga-duck';
import { runAndTakeLatest, takeLatest } from 'redux-saga-catch';
import { delay } from 'redux-saga';
import { put, select, take } from 'redux-saga/effects';
import Fetcher from './Fetcher';

// import { cancelable } from '../helpers/saga'

export interface SearchParam {
  offset?: number;
  limit?: number;
}

interface ListResult<T> {
  list: T[];
  totalCount: number;
}

export default abstract class AbstractSearchableSelect extends Base {
  /** 基本搜索参数，由外部传入 */
  Param;
  /** 本地搜索参数 */
  LocalParam;
  /** 列表项 */
  Item;

  /** 类型声明 */
  get Data(): ListResult<this['Item']> {
    return null;
  }

  get GetDataParam(): ReturnType<this['getParam']> {
    return null;
  }

  get Fetcher(): FetcherType<this['Data'], this['GetDataParam']> {
    return null;
  }

  /**
   * 哪些操作会触发搜索条件变更（本组件内的条件变更）
   */
  get searchTypes() {
    return [this.types.SEARCH];
  }

  /**
   * 默认的搜索条件
   */
  abstract get defaultLocalParam(): this['LocalParam'];

  /** 分页接口一次查询最大值（根据后台接口要求设置） */
  get pageSize() {
    return 20;
  }

  /** 即用户输入后是否自动触发搜索 */
  get autoSearch() {
    return true;
  }

  /** 自动搜索延时buffer，默认300毫秒 */
  get autoSearchBuffer() {
    return 300;
  }

  get quickTypes() {
    enum Types {
      SET_LIST,
      /** 外部传入基本参数，开启加载 */
      LOAD,
      /** 内部动作，设置基本参数 */
      LOAD_START,
      /** 输入搜索条件 */
      SET_PENDING_SEARCH_PARAMS,
      // 历史兼容
      INPUT_KEYWORD,
      /** 触发搜索 */
      SEARCH,
      RESET,
      RELOAD,
      /** 搜索完成，更新关键字 */
      SEARCH_DONE,
      SET_TOTAL_COUNT,
      MORE,
      NOMORE,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      /** 当前列表对应的基本搜索条件 */
      param: reduceFromPayload<this['Param']>(types.LOAD_START, null),
      /** 搜索框中的用户输入条件 */
      pendingKeyword: (state = this.defaultLocalParam, action): this['LocalParam'] => {
        switch (action.type) {
          case types.SET_PENDING_SEARCH_PARAMS:
          case types.INPUT_KEYWORD:
          case types.SEARCH:
            return action.payload;
          case types.LOAD_START:
          case types.RESET:
            return this.defaultLocalParam;
          default:
            return state;
        }
      },
      /** 结果列表对应的搜索条件 */
      keyword: (state = this.defaultLocalParam, action): this['LocalParam'] => {
        switch (action.type) {
          case types.SEARCH_DONE:
            return action.payload;
          case types.LOAD_START:
          case types.RESET:
            return this.defaultLocalParam;
          default:
            return state;
        }
      },
      /** 当前查询到的列表 */
      list: (state = null, action): this['Item'][] => {
        switch (action.type) {
          case types.SET_LIST:
            return action.payload;
          case types.LOAD_START:
          // 输入关键字时，老是清空，有点体验不好
          // 但这样有助于区分是条件变更的搜索“刷新”还是“加载更多”
          case types.SEARCH:
          case types.RESET:
            return null;
          default:
            return state;
        }
      },
      /** 列表总条数（包括未加载完的，需要后端返回） */
      totalCount: (state = null, action): number => {
        switch (action.type) {
          case types.SET_TOTAL_COUNT:
            return action.payload;
          case types.LOAD_START:
          case types.RESET:
          case types.SEARCH:
            return null;
          default:
            return state;
        }
      },
      /** 列表是否已经加载完，即不会有“加载更多” */
      nomore: (state = false, action): boolean => {
        switch (action.type) {
          case types.NOMORE:
            return true;
          case types.LOAD_START:
          case types.RESET:
          case types.SEARCH:
          case types.RELOAD:
            return false;
          default:
            return state;
        }
      },
      /** 是否处于加载更多的模式，这时UI上loading需要展示在列表尾部 */
      loadingMore: (state = false, action): boolean => {
        switch (action.type) {
          case types.MORE:
            return true;
          case types.SET_LIST:
          case types.LOAD_START:
          case types.RESET:
          case types.SEARCH:
          case types.RELOAD:
            return false;
          default:
            return state;
        }
      },
    };
  }

  get creators() {
    const { types } = this;
    type Param = this['Param'];
    return {
      ...super.creators,
      load: createToPayload<Param>(types.LOAD),
      // 历史兼容
      inputKeyword: createToPayload<this['LocalParam']>(types.INPUT_KEYWORD),
      setPendingSearchParams: createToPayload<this['LocalParam']>(types.SET_PENDING_SEARCH_PARAMS),
      search: createToPayload<this['LocalParam']>(types.SEARCH),
      reload() {
        return { type: types.RELOAD };
      },
      reset() {
        return { type: types.RESET };
      },
      more() {
        return { type: types.MORE };
      },
    };
  }

  get quickDucks() {
    const duck = this;
    type Param = this['GetDataParam'];
    type Data = this['Data'];

    class MyFetcher extends Fetcher {
      Param: Param;
      Data: Data;

      async getDataAsync(param: MyFetcher['Param']) {
        return duck.getData(param);
      }
    }

    return {
      ...super.quickDucks,
      fetcher: (MyFetcher as unknown) as this['Fetcher'],
    };
  }

  /**
   * 如何构造Fetcher的查询条件
   * @param baseParam 基本搜索参数
   * @param localParam 本地搜索参数
   * @param pagingParam 分页参数
   */
  abstract getParam(baseParam: this['Param'], localParam: this['LocalParam'], pagingParam: SearchParam);

  abstract getData(param: this['GetDataParam']): Promise<ListResult<this['Item']>>;

  *saga() {
    yield* super.saga();
    if (this.autoSearch) {
      yield* this.sagaAutoSearch();
    }
    // 监听load
    yield* this.sagaWatchLoad();
  }

  *sagaAutoSearch() {
    const { types, autoSearchBuffer } = this;
    // 是否自动启用搜索
    yield takeLatest([types.SET_PENDING_SEARCH_PARAMS, types.SEARCH], function*({ type, payload }) {
      // 如果有触发SEARCH，中止自动触发
      if (type === types.SEARCH) {
        return;
      }
      yield delay(autoSearchBuffer);
      yield put({
        type: types.SEARCH,
        payload,
      });
    });
  }

  *sagaWatchLoad() {
    const duck = this;
    const { types } = this;
    yield takeLatest(types.LOAD, function*(action) {
      yield* duck.load(action.payload);
    });
  }

  /**
   * 供外界嵌入，以指定参数加载，并监听相关搜索、加载更多等操作。
   *
   * **注意：调用时需自行保证只有一个任务执行（即如果更改参数，需要取消前次的调用与运行）**
   *
   * **注意：请勿与`creators.load()`一起使用，否则会导致存在多套监听逻辑**
   */
  *load(param: this['Param']) {
    const duck = this;
    const { types } = this;
    yield runAndTakeLatest(types.RELOAD, function*() {
      yield* duck.sagaLoadAndWatchSearch(param);
    });
  }

  *sagaLoadAndWatchSearch(param: this['Param']) {
    const duck = this;
    const { types, selector } = this;

    // 更新参数
    yield put({
      type: types.LOAD_START,
      payload: param,
    });
    const baseParam = selector(yield select()).param;
    // 默认以初始条件搜索，同时新搜索会重置当前数据
    yield runAndTakeLatest(this.searchTypes, function*() {
      yield* duck.search(baseParam);
    });
  }

  /**
   * 搜索逻辑
   * @param baseParam
   */
  // @cancelable<AbstractSearchableSelect>(function* () {
  //   yield put(this.creators.reset())
  // })
  protected *search(baseParam: this['Param']) {
    type Item = this['Item'];
    type Data = this['Data'];

    const duck = this;

    const {
      types,
      selector,
      ducks: { fetcher },
      pageSize,
    } = this;

    const { pendingKeyword: pendingLocalParam } = selector(yield select());
    let list: Item[] = [];
    let totalCount = 0;
    do {
      try {
        // 加载下一页内容
        const searchParam: SearchParam = {
          offset: list.length,
          limit: pageSize,
        };
        const fetcherParam = duck.getParam(baseParam, pendingLocalParam, searchParam);
        const data: Data = yield* fetcher.fetch(fetcherParam);
        totalCount = data.totalCount;
        // 合并列表
        list = list.concat(data.list);
        // 更新结果
        yield put({
          type: types.SEARCH_DONE,
          payload: pendingLocalParam,
        });
        yield put({
          type: types.SET_LIST,
          payload: list,
        });
        if (totalCount !== selector(yield select()).totalCount) {
          yield put({
            type: types.SET_TOTAL_COUNT,
            payload: totalCount,
          });
        }
        // 如果已经加载完了，退出循环
        if (list.length === totalCount) {
          yield put({
            type: types.NOMORE,
          });
          break;
        }
      } catch (e) {
        // 出错了Fetcher那会展示出来，不处理，不中止MORE重试
      }
      // 等待下一次加载更多的指令
    } while (yield take(types.MORE));
  }
}

interface MyFetcher<TData, TParam> extends Fetcher {
  Param: TParam;
  Data: TData;
}

type FetcherType<TData, TParam> = {
  new (...any: any[]): MyFetcher<TData, TParam>;
};
