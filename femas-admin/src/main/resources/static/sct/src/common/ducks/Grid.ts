/**
 * grid常用业务逻辑
 */
import { createToPayload, DuckMap, reduceFromPayload } from 'saga-duck';
import { call, fork, put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { delay } from 'redux-saga';
import { FetchState } from './Fetcher';

export interface GridResult<T = any> {
  list: T[];
  totalCount: number;
}

enum Types {
  'FETCH',
  'FETCH_DONE',
  'FETCH_FAIL',
  /** 重置请求状态 */
  RESET,
  'UPDATE',
  'BATCH_UPDATE',
  'SELECT',
  SET_SEARCH_CONDITION,
}

export default abstract class GridDuck extends DuckMap {
  abstract Filter;
  abstract Item;

  /**
   * 每条记录的ID属性，用于Table组件渲染时的key，以及选择功能的索引。默认为 "id"
   */
  get recordKey() {
    return 'id';
  }

  get quickTypes() {
    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducers() {
    const { types } = this;
    type Item = this['Item'];
    return {
      ...super.reducers,
      list: (state = [], action): Item[] => {
        switch (action.type) {
          case types.FETCH_DONE:
            return action.payload.list;
          case types.FETCH_FAIL:
            return [];
          case types.BATCH_UPDATE:
            return state.map((item, index) => {
              const updates = action.list[index];
              if (!updates) {
                return item;
              }
              return Object.assign({}, item, updates);
            });
          case types.UPDATE: {
            // 单条更新
            const list = state.slice(0);
            let index = +action.index;
            // index也可以传对象，但必须是原样的
            if (isNaN(index)) {
              index = list.indexOf(action.index);
            }
            list[index] = Object.assign({}, list[index], action.data);
            return list;
          }
          case types.RESET:
            return [];
          default:
            return state;
        }
      },
      totalCount: (state = 0, action): number => {
        switch (action.type) {
          case types.FETCH_DONE:
            return action.payload.totalCount;
          case types.FETCH_FAIL:
          case types.RESET:
            return 0;
          default:
            return state;
        }
      },
      error: (state = null, action) => {
        switch (action.type) {
          case types.FETCH_DONE:
          case types.RESET:
            return null;
          case types.FETCH_FAIL:
            return action.payload;
          default:
            return state;
        }
      },
      loading: (state = false, action): boolean => {
        switch (action.type) {
          case types.FETCH:
            return true;
          case types.FETCH_DONE:
          case types.FETCH_FAIL:
          case types.RESET:
            return false;
          default:
            return state;
        }
      },
      filter: reduceFromPayload<this['Filter']>(types.FETCH, null),
      selection: reduceFromPayload<Item[]>(types.SELECT, []),
      searchCondition: reduceFromPayload<string>(types.SET_SEARCH_CONDITION, ''),
    };
  }

  get creators() {
    const { types } = this;
    type Item = this['Item'];
    return {
      ...super.creators,
      /** 按指定过滤条件加载数据 */
      fetch: (filter: this['Filter']) => ({
        type: types.FETCH,
        payload: filter,
      }),
      /** 重置加载状态 */
      reset: () => ({ type: types.RESET }),
      /** 更新指定行数据 */
      update: (index: number, data: Partial<Item>) => ({
        type: types.UPDATE,
        index,
        data,
      }),
      /** 批量更新 */
      updateList: (list: Partial<Item>[]) => ({
        type: types.BATCH_UPDATE,
        list,
      }),
      /** 设置选中项 */
      select: (list: Item[]) => ({ type: types.SELECT, payload: list }),
      /** 设置搜索条件（在列表内容上方展示，会替代filter.keyword） */
      setSearchCondition: createToPayload<string>(types.SET_SEARCH_CONDITION),
    };
  }

  get rawSelectors() {
    const duck = this;
    type State = this['State'];
    return {
      ...super.rawSelectors,
      /** 供SmartTip兼容使用 */
      fetcher: (state: State) => ({
        fetchState: state.error ? FetchState.Failed : state.loading ? FetchState.Fetching : FetchState.Ready,
        loading: state.loading,
        data: {
          recordCount: state.totalCount,
        },
      }),
      /** 供SmartTip兼容使用 */
      query: (state: State) => ({
        search: duck.rawSelectors.searchCondition(state),
      }),
      /** 展示在列表头部的搜索条件，例如： xxxx => 搜索 “xxxx”，找到 1 条结果 返回原列表 */
      searchCondition: (state: State) => state.searchCondition || (state.filter && state.filter.keyword),
      filter: (state: State): this['Filter'] => state.filter,
      error: (state: State) => state.error,
      list: ({ list }: State): this['Item'][] => list,
      selection: (state: State): this['Item'][] => state.selection,
      totalCount: ({ totalCount }: State) => totalCount,
      loading: (state: State) => state.loading,
    };
  }

  /** 列表加载缓冲（单位：ms） */
  get buffer() {
    return 100;
  }

  *saga() {
    yield* super.saga();
    yield fork([this, this.sagaMain]);
  }

  *sagaMain(duck = this) {
    const { types } = duck;

    yield takeLatest(types.FETCH, function*(action) {
      if (action.manual) {
        return;
      }
      yield* duck.sagaFetch();
    });

    yield* duck.sagaWatchSelection();
  }

  /** 供外部完全掌握Grid的加载 */
  *sagaManualFetch(filter, searchDesc = '') {
    const { types, selector } = this;
    yield put({
      type: types.FETCH,
      payload: filter,
      manual: true,
    });
    // 当searchCondition与现有的不符时就更新
    const { searchCondition } = selector(yield select());
    if (searchDesc !== searchCondition) {
      yield put(this.creators.setSearchCondition(searchDesc));
    }

    return yield* this.sagaFetch();
  }

  *sagaFetch() {
    const duck = this;
    const { selectors, types, buffer, getData } = duck;
    // 缓冲
    yield call(delay, buffer || 100);
    const filter = yield select(selectors.filter);

    let dataFetched = false;
    try {
      // 加载数据
      const payload = yield getData(duck, filter, function* progressiveUpdate(payload) {
        yield put({
          type: types.FETCH_DONE,
          payload,
        });
        dataFetched = true;
      });
      // 展示
      yield put({
        type: types.FETCH_DONE,
        payload,
      });
      dataFetched = true;
    } catch (e) {
      if (!dataFetched) {
        yield put({
          type: types.FETCH_FAIL,
          payload: e,
        });
      }
      console.error(e);
    }
    return dataFetched;
  }

  /**
   * 获取Grid列表数据
   * 将方法从options移到类上，方便扩展
   * @param {*} duck
   * @param {*} filterW
   * @param {Saga} progressiveUpdate 渐进式更新，参数为({list:[],totalCount:0})
   * @return {list:[],totalCount:0}
   */
  abstract getData(me: this, filter, progressiveUpdate?: (result: GridResult) => any): any;

  /** 动态维护选中项，因为历史原因，这里已经使用了完整实例，而非ID作为列表，所以只能靠代码维护更新了 */
  *sagaWatchSelection() {
    const { types, selector, creators, recordKey } = this;
    // 当列表更新时，更新选中状态引用
    yield takeLatest([types.FETCH_DONE, types.UPDATE, types.BATCH_UPDATE], function*() {
      const { selection, list } = selector(yield select());
      if (!selection || !selection.length) {
        return;
      }
      const map = (list || []).reduce((map, o) => {
        map[o[recordKey]] = o;
        return map;
      }, {});
      const newSelection = selection.map(x => map[x[recordKey]]).filter(x => x);
      yield put(creators.select(newSelection));
    });
  }
}
