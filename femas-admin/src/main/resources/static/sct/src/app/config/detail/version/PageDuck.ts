import { ConfigItem } from './../../types';
import Base, { Filter as BaseFilter } from '@src/common/ducks/GridPage';
import { ComposedId } from '../types';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { VersionItem } from '../../types';
import { put, select, takeEvery, takeLatest } from 'redux-saga/effects';
import { SortBy } from 'tea-component/lib/table/addons';
import { resolvePromise } from 'saga-duck/build/helper';
import create from '../../operations/create';
import { Action } from '@src/common/types';
import { EDIT_TYPE } from '../../operations/create/CreateDuck';
import { deleteConfigVersions, fetchConfigVersions } from '../../model';
import { Modal } from 'tea-component';

interface Filter extends BaseFilter {
  configId: string;
  namespaceId: string;
  sortValue?: Array<SortBy>;
  releaseStatus?: string;
}

export default class BaseInfoDuck extends Base {
  Filter: Filter;
  Item: VersionItem;

  get baseUrl() {
    return null;
  }

  get recordKey() {
    return 'configVersionId';
  }

  get initialFetch() {
    return false;
  }

  get watchTypes() {
    return [...super.watchTypes, this.types.SET_COMPOSED_ID, this.types.SET_SORT, this.types.SET_STATUS];
  }

  get quickTypes() {
    enum Types {
      LOAD,
      SET_COMPOSED_ID,
      SET_DATA,
      SET_CURRENT_VERSION,
      CONFIGURE_VERSION,
      SET_SORT,
      SET_STATUS,
      DELETE,
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
      composedId: reduceFromPayload(types.SET_COMPOSED_ID, {} as ComposedId),
      data: reduceFromPayload(types.SET_DATA, {} as ConfigItem),
      currentVersion: reduceFromPayload(types.SET_CURRENT_VERSION, {} as this['Item']),
      sortValue: reduceFromPayload(types.SET_SORT, [] as Array<SortBy>),
      releaseStatus: reduceFromPayload(types.SET_STATUS, ''),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      load: (composedId, data) => ({
        type: types.LOAD,
        payload: { composedId, data },
      }),
      selectVersion: createToPayload(types.SET_CURRENT_VERSION),
      configureVersion: createToPayload<{
        item: VersionItem;
        editType: EDIT_TYPE;
      }>(types.CONFIGURE_VERSION),
      setSort: createToPayload<SortBy>(types.SET_SORT),
      setStatus: createToPayload<string>(types.SET_STATUS),
      delete: createToPayload<string[]>(types.DELETE),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      filter: (state: State) => ({
        page: state.page,
        count: state.count,
        keyword: state.keyword,
        configId: state.composedId.configId,
        namespaceId: state.composedId.namespaceId,
        sortValue: state.sortValue,
        releaseStatus: state.releaseStatus,
      }),
      composedId: (state: State) => state.composedId,
      data: (state: State) => state.data,
      currentVersion: (state: State) => state.currentVersion,
      sortValue: (state: State) => state.sortValue,
      releaseStatus: (state: State) => state.releaseStatus,
    };
  }

  *saga() {
    yield* super.saga();
    const {
      ducks: { grid },
      selectors,
      types,
      creators,
    } = this;
    yield takeLatest(types.LOAD, function*(action: Action<{ composedId: ComposedId; data: ConfigItem }>) {
      const { composedId, data } = action.payload;
      yield put({ type: types.SET_COMPOSED_ID, payload: composedId });
      yield put({ type: types.SET_DATA, payload: data });
    });
    yield takeEvery(grid.types.FETCH_DONE, function*() {
      const list = grid.selectors.list(yield select());
      yield put({ type: types.SET_CURRENT_VERSION, payload: list[0] || {} });
    });
    yield takeLatest(types.CONFIGURE_VERSION, function*(action: Action<{ item: VersionItem; editType: EDIT_TYPE }>) {
      const { item, editType } = action.payload;
      const { configValue, configVersion, configVersionId } = item;
      const data = selectors.data(yield select());
      const { namespaceId, configId } = selectors.composedId(yield select());
      const res = yield* resolvePromise(
        create({ ...data, configValue } as ConfigItem, {
          namespaceId,
          configId,
          editType,
          configVersion,
          configVersionId,
        }),
      );
      if (res) {
        yield put(creators.reload());
      }
    });
    yield takeLatest(types.DELETE, function*(action: Action<ConfigItem[]>) {
      const { configId } = selectors.composedId(yield select());
      const yes = yield Modal.confirm({
        message: '确认删除当前所选版本？',
        okText: '删除',
        cancelText: '取消',
      });
      if (!yes) return;
      yield deleteConfigVersions({
        configVersionIdList: action.payload,
        configId,
      });
      yield put(creators.reload());
    });
  }

  async getData(filter: this['Filter']) {
    const { configId, namespaceId, page, count, releaseStatus, sortValue } = filter;
    const params = {
      configId,
      namespaceId,
      pageNo: page,
      pageSize: count,
      releaseStatus,
    } as any;
    if (sortValue?.length) {
      params.orderBy = sortValue[0].by;
      params.orderType = sortValue[0].order === 'desc' ? 0 : 1;
    }
    const res = await fetchConfigVersions(params);

    return {
      totalCount: res.totalCount,
      list: res.list as Array<VersionItem>,
    };
  }
}
