import Base from '@src/common/ducks/SearchableSelect';
import { ServiceItem } from '../types';
import { runAndTakeLatest } from 'redux-saga-catch';
import { describeRegisterService, describeServiceOverview } from '../model';
import { put, select } from 'redux-saga/effects';

export const NAMESPACE_STORAGE_KEY = 'atom_service_id';

interface EmptyItem {
  serviceName: string;
  text: string;
  value: string;
}

export default class ServiceSelectDuck extends Base {
  Param: {
    namespaceId: string;
    needEmptyItem?: boolean;
    emptyItem?: EmptyItem;
  };
  Item: ServiceItem;

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      id: (state: State) => state.id,
      isEmpty: (state: State) => state.totalCount === 0,
    };
  }

  getId(o: this['Item']) {
    return o.serviceName;
  }

  *saga() {
    yield* super.saga();
    const { types, creators, selector } = this;
    yield runAndTakeLatest(types.SET_LIST, function*() {
      const { id, list, param } = selector(yield select());
      // needEmptyItem开启时，处理需要设置默认值为空的情况
      if (!id && param.needEmptyItem) {
        yield put(creators.select(param.emptyItem?.serviceName || ''));
        return;
      }
      if (!id && list?.length > 0) {
        yield put(creators.select(list[0].serviceName));
      }
    });
  }

  *getSelectedItem() {
    const { list, id, param } = this.selector(yield select());
    const isEmpty = this.selectors.isEmpty(yield select());
    if (isEmpty) return null;
    const selected = (list || []).find(item => this.getId(item) === id) || null;
    if (!id) {
      return selected;
    }
    if (!selected) {
      const { namespaceId } = param;
      const data = yield describeServiceOverview({
        namespaceId,
        serviceName: id,
      });
      return param.needEmptyItem && !data ? param.emptyItem : data;
    }
    return selected;
  }

  async getData(param): Promise<this['Data']> {
    const { namespaceId, offset, limit } = param;
    if (!namespaceId) {
      return { totalCount: 0, list: [] };
    }
    const res = await describeRegisterService({
      namespaceId,
      pageSize: limit,
      pageNo: offset / limit + 1,
    });
    return res;
  }
}
