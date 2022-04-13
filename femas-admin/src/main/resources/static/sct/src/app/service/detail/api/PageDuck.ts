import { ComposedId } from '../types';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { InterfaceItem } from './types';
import { describeServiceApi } from '../../model';
import GridPageDuck, { Filter as BaseFilter } from '@src/common/ducks/GridPage';

interface Filter extends BaseFilter {
  namespaceId: string;
  serviceName: string;
  status: string;
  serviceVersion: string;
}

export default class BaseInfoDuck extends GridPageDuck {
  baseUrl = null;
  Filter: Filter;
  Item: InterfaceItem;

  get initialFetch() {
    return false;
  }

  get watchTypes() {
    return [...super.watchTypes, this.types.LOAD, this.types.SET_STATUS, this.types.SET_VERSION];
  }

  get quickTypes() {
    enum Types {
      LOAD,
      SET_STATUS,
      SET_VERSION,
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
      composedId: reduceFromPayload(types.LOAD, {} as ComposedId),
      status: reduceFromPayload(types.SET_STATUS, ''),
      serviceVersion: reduceFromPayload(types.SET_VERSION, ''),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      load: createToPayload<ComposedId>(types.LOAD),
      setStatus: createToPayload(types.SET_STATUS),
      setVersion: createToPayload(types.SET_VERSION),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      composedId: (state: State) => state.composedId,
      filter: (state: State) => ({
        page: state.page,
        count: state.count,
        keyword: state.keyword,
        namespaceId: state.composedId.namespaceId,
        serviceName: state.composedId.serviceName,
        status: state.status,
        serviceVersion: state.serviceVersion,
      }),
      status: (state: State) => state.status,
      serviceVersion: (state: State) => state.serviceVersion,
    };
  }

  async getData(filters: this['Filter']) {
    const { page, count, keyword, namespaceId, serviceName, status, serviceVersion } = filters;
    return describeServiceApi({
      pageNo: page,
      pageSize: count,
      keyword,
      namespaceId,
      serviceName,
      status,
      serviceVersion,
    });
  }
}
