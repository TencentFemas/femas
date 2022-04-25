import { createToPayload, reduceFromPayload } from 'saga-duck';
import GridPageDuck, { Filter as BaseFilter } from '@src/common/ducks/GridPage';
import { ServiceItem } from './types';
import NamespaceSelectDuck from '../namespace/components/NamespaceSelectDuck';
import { put, takeLatest } from 'redux-saga/effects';
import { describeRegisterService } from './model';
import { fetchMetricGrafanaAddress } from '../apm/model';

interface Filter extends BaseFilter {
  namespaceId: string;
  status: string;
}

export default class ServicePageDuck extends GridPageDuck {
  Filter: Filter;
  Item: ServiceItem;

  get baseUrl() {
    return '/service';
  }

  get quickTypes() {
    enum Types {
      SET_STATUS,
      SET_GRAFANAURL,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get initialFetch() {
    return false;
  }

  get watchTypes() {
    return [...super.watchTypes, this.ducks.namespace.types.SELECT, this.types.SET_STATUS];
  }

  get params() {
    return [...super.params, this.ducks.namespace.getRegistryParam()];
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      namespace: NamespaceSelectDuck,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      status: reduceFromPayload(types.SET_STATUS, ''),
      grafanaUrl: reduceFromPayload(types.SET_GRAFANAURL, ''),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      setStatus: createToPayload(types.SET_STATUS),
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
        namespaceId: state.namespace.id,
        status: state.status,
      }),
      status: (state: State) => state.status,
      grafanaUrl: (state: State) => state.grafanaUrl,
    };
  }

  *saga() {
    yield* this.sagaInitLoad();
    yield* super.saga();
    const { types } = this;
    yield takeLatest(types.ROUTE_INITIALIZED, function*() {
      const grafanaUrl = yield fetchMetricGrafanaAddress();
      yield put({ type: types.SET_GRAFANAURL, payload: grafanaUrl });
    });
  }

  *sagaInitLoad() {
    const { ducks } = this;
    yield* ducks.namespace.load(null);
  }

  async getData(filters: this['Filter']) {
    const { page, count, keyword, namespaceId, status } = filters;

    if (!namespaceId) {
      return { totalCount: 0, list: [] };
    }

    const result = await describeRegisterService({
      pageNo: page,
      pageSize: count,
      keyword,
      namespaceId,
      status,
    });

    return {
      totalCount: result.totalCount,
      list: result.list || [],
    };
  }
}
