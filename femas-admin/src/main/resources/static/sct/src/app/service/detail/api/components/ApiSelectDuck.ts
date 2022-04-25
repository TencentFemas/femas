import Base from '@src/common/ducks/SearchableSelect';
import { InterfaceItem } from '../types';
import { runAndTakeLatest } from 'redux-saga-catch';
import { describeServiceApi } from '@src/app/service/model';
import { put, select } from 'redux-saga/effects';

export const NAMESPACE_STORAGE_KEY = 'atom_namespace_id';

export default class ApiSelectDuck extends Base {
  Param: {
    namespaceId: string;
    serviceName: string;
  };
  Item: InterfaceItem;

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      id: (state: State) => state.id,
    };
  }

  getId(o: this['Item']) {
    return o.id;
  }

  *saga() {
    yield* super.saga();
    const { types, creators, selector } = this;
    yield runAndTakeLatest(types.SET_LIST, function*() {
      const { id, list } = selector(yield select());
      if (!id && list?.length > 0) {
        yield put(creators.select(list[0].id));
      }
    });
  }

  async getData(param): Promise<this['Data']> {
    const { namespaceId, offset, limit, serviceName } = param;
    if (!namespaceId || !serviceName) {
      return { totalCount: 0, list: [] };
    }
    const res = await describeServiceApi({
      namespaceId,
      serviceName,
      pageSize: limit,
      pageNo: offset / limit + 1,
    });
    return res;
  }
}
