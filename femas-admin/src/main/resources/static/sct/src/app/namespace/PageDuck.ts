import GridPageDuck, { Filter as BaseFilter } from '../../common/ducks/GridPage';
import { NamespaceItem } from './types';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { resolvePromise } from 'saga-duck/build/helper';
import create from './operations/create';
import { Action } from '@src/common/types';
import remove from './operations/remove';
import { fetchNamespaces } from './model';
import { describeRegistryClusters } from '../registry/model';
import { fetchMetricGrafanaAddress } from '../apm/model';

interface Filter extends BaseFilter {
  registryId: string;
}

export default class NamespacePageDuck extends GridPageDuck {
  filter: Filter;
  Filter: Filter;
  Item: NamespaceItem;

  get baseUrl() {
    return '/namespace';
  }

  get watchTypes() {
    return [...super.watchTypes, this.types.SET_REGISTRY_ID];
  }

  get quickTypes() {
    enum Types {
      CREATE,
      EDIT,
      REMOVE,
      SET_REGISTRY_ID,
      SET_REGISTRY_LIST,
      SET_GRAFANAURL,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      create: createToPayload<void>(types.CREATE),
      edit: createToPayload<NamespaceItem>(types.EDIT),
      remove: createToPayload<NamespaceItem>(types.REMOVE),
      setRegistryId: createToPayload<string>(types.SET_REGISTRY_ID),
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      registryId: reduceFromPayload(types.SET_REGISTRY_ID, ''),
      registryList: reduceFromPayload(types.SET_REGISTRY_LIST, []),
      grafanaUrl: reduceFromPayload(types.SET_GRAFANAURL, ''),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      filter: (state: State) => ({
        registryId: state.registryId,
        keyword: state.keyword,
        page: state.page,
        count: state.count,
      }),
      registryId: (state: State) => state.registryId,
      registryList: (state: State) => state.registryList,
      grafanaUrl: (state: State) => state.grafanaUrl,
    };
  }

  *saga() {
    yield* super.saga();
    const { types, creators, ducks, selectors } = this;
    yield takeLatest(types.ROUTE_INITIALIZED, function*() {
      const grafanaUrl = yield fetchMetricGrafanaAddress();
      yield put({ type: types.SET_GRAFANAURL, payload: grafanaUrl });
    });
    yield takeLatest(types.CREATE, function*() {
      const registryList = selectors.registryList(yield select());
      const res = yield* resolvePromise(
        create(null, {
          addMode: true,
          registryList,
        }),
      );
      if (res) {
        yield put(creators.reload());
      }
    });
    yield takeLatest(types.EDIT, function*(action: Action<NamespaceItem>) {
      const ns = action.payload;
      const registryList = selectors.registryList(yield select());
      const res = yield* resolvePromise(
        create(ns, {
          addMode: false,
          namespaceId: ns.namespaceId,
          registryList,
          registryId: ns.registryId,
          registryName: ns.registry?.find(d => d.registryId === ns.registryId)?.registryName || '',
        }),
      );
      if (res) {
        yield put(creators.reload());
      }
    });
    yield takeLatest(types.REMOVE, function*(action: Action<NamespaceItem>) {
      const res = yield* resolvePromise(remove(action.payload));
      if (res) {
        yield put(creators.reload());
      }
    });
    yield takeLatest(ducks.grid.types.FETCH_DONE, function*() {
      const { list } = yield describeRegistryClusters({});
      yield put({
        type: types.SET_REGISTRY_LIST,
        payload: list.map(d => ({
          ...d,
          value: d.registryId,
          text: d.registryName,
        })),
      });
    });
  }

  async getData(filter: this['Filter']) {
    const { registryId, page, count, keyword } = filter;

    const res = await fetchNamespaces({
      registryId,
      name: keyword,
      pageNo: page,
      pageSize: count,
    });
    return res;
  }
}
