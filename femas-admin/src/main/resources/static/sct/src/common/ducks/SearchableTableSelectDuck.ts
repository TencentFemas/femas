import SearchableSelect from './SearchableSelect';
import { put, select, take } from 'redux-saga/effects';
import { SearchParam } from './SearchableList';
import { runAndTakeLatest } from 'redux-saga-catch';
import { createToPayload, reduceFromPayload } from 'saga-duck';

export default abstract class SearchableTableSelectDuck extends SearchableSelect {
  get localSearch() {
    return true;
  }

  get defaultId() {
    return '';
  }

  get quickTypes() {
    enum Types {
      SELECT_IDS,
      SET_FILTER_LIST,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      selectedItems: (state: State) => {
        const { ids } = state;
        const selectedItems = [];
        const selectedIds = [];
        for (const id of ids) {
          if (id) {
            const item = this.find(state.list, id);
            if (item) {
              selectedItems.push(item);
              selectedIds.push(id);
            }
          }
        }
        return selectedItems;
      },
      selectIds: (state: State) => state.ids,
      filterList: (state: State) => {
        return state.filterList;
      },
      list: (state: State) => state.fetcher.data,
    };
  }

  get creators() {
    const { types } = this;
    type TID = this['ID'];
    return {
      ...super.creators,
      selectIds: createToPayload<TID[]>(types.SELECT_IDS),
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      ids: reduceFromPayload(types.SELECT_IDS, []),
      filterList: reduceFromPayload(types.SET_FILTER_LIST, []),
    };
  }

  abstract getId(o: this['Item']): this['ID'];

  //指定filterFunction来自定义需要搜索的项与匹配规则，只在localSearch=true时有效
  filterFunction(item: this['Item'], keyword: string): boolean {
    return this.getId(item).indexOf(keyword) >= 0;
  }

  *sagaLoadAndWatchSearch(param: this['Param']) {
    type Item = this['Item'];
    type Data = this['Data'];
    const self = this;
    const {
      types,
      selector,
      ducks: { fetcher },
      pageSize,
    } = this;
    // 更新参数
    yield put({
      type: types.LOAD_START,
      payload: param,
    });
    const baseParam = selector(yield select()).param;
    // 默认以空串开始搜索，同时新搜索会重置当前数据
    yield runAndTakeLatest(types.SEARCH, function*() {
      const { pendingKeyword: keyword } = selector(yield select());
      let list: Item[] = [];
      let filterList: Item[] = [];
      let totalCount = 0;
      do {
        // 加载下一页内容
        const searchParam: SearchParam = self.localSearch
          ? {
              offset: list.length,
              limit: pageSize,
            }
          : {
              offset: list.length,
              limit: pageSize,
              keyword,
            };
        const data: Data = yield* fetcher.fetch({
          ...baseParam,
          ...searchParam,
        });
        totalCount = data.totalCount;
        // 合并列表
        if (self.localSearch) {
          filterList = filterList.concat(
            data.list.filter(item => {
              return self.filterFunction(item, keyword);
            }),
          );
        }
        list = list.concat(data.list);
        // 更新结果
        yield put({
          type: types.SEARCH_DONE,
          payload: keyword,
        });
        yield put({
          type: types.SET_LIST,
          payload: list,
        });
        //如果不是localSearch, 则后台已经代替搜索
        if (!self.localSearch) filterList = list;
        yield put({
          type: types.SET_FILTER_LIST,
          payload: filterList,
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
        // 等待下一次加载更多的指令
      } while (yield take(types.MORE));
    });
  }
}
