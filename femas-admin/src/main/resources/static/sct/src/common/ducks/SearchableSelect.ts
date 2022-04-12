/**
 * 可搜索单选（AsyncSelect进阶版）
 * 1. 可搜索（也可关闭此功能）
 * 2. 可分页加载更多
 *
 * 一些优化：
 * 1. 不再需要用户定义Fetcher，简化使用
 * 2. 不再有同步(syncList)的过程，Fetcher将作为一个工具使用
 */

import Base from './SearchableList';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { put, select, take } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { runAndWatchLatest } from '../helpers/saga';

export default abstract class SearchableSelect extends Base {
  ID: string;

  get isAllowValueEmpty(): boolean {
    return true;
  }

  get quickTypes() {
    enum Types {
      SELECT,
      SET_SELECTED,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  /**
   * LocalStory 存储健
   * 如果 !lskey 为 true 就不存储
   */
  get lskey(): string {
    return null;
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      id: reduceFromPayload(types.SELECT, this.defaultId),
      selected: reduceFromPayload<this['Item']>(types.SET_SELECTED, null),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      selected: (state: State) => state.selected,
      isEmpty: (state: State) => {
        const hasErr = !!state.fetcher.error;
        const hasSelected = !!state.selected;
        const hasNoData = state.fetcher.data && !state.fetcher.data.totalCount;
        // 这里 ```(hasErr || hasNoData)``` 标识请求完成状态
        return !hasSelected && (hasErr || hasNoData);
      },
    };
  }

  get creators() {
    const { types } = this;
    type TID = this['ID'];
    return {
      ...super.creators,
      select: createToPayload<TID>(types.SELECT),
    };
  }

  /** 默认值 */
  get defaultId(): this['ID'] {
    return null;
  }

  abstract getId(o: this['Item']): this['ID'];

  *getSelectedItem(): any {
    const { list, id } = this.selector(yield select());
    return (list || []).find(item => this.getId(item) === id) || null;
  }

  *initList() {
    const { types } = this;
    const { list } = this.selector(yield select());
    if (list) return;
    yield take(types.SET_LIST);
  }

  *saga() {
    yield* super.saga();
    const { types, selector, creators } = this;
    const duck = this;
    yield* this.initList();
    yield runAndWatchLatest(
      types.SELECT,
      state => {
        return selector(state).id;
      },
      function*() {
        const { id, list } = selector(yield select());
        if (!id && duck.isAllowValueEmpty) {
          yield put({ type: types.SET_SELECTED, payload: null });
          return;
        }
        localStorage.setItem(duck.lskey, id);
        const selected = yield duck.getSelectedItem();
        if (selected) {
          yield put({ type: types.SET_SELECTED, payload: selected });
          return;
        }
        if (list?.length > 0) {
          yield put(creators.select(duck.getId(list[0])));
        }
      },
    );

    yield takeLatest(types.LOAD, function*() {
      yield put(creators.select(''));
    });
  }

  protected find(list: this['Item'][], id: this['ID']): this['Item'] {
    return (list || []).find(item => this.getId(item) === id) || null;
  }
}
