import { PageParam } from '@src/common/ducks/Page';
import Base from '@src/common/ducks/SearchableSelect';
import { NamespaceItem } from '../types';
import { runAndTakeLatest, takeEvery } from 'redux-saga-catch';
import { Action } from '@src/common/types';
import { fetchNamespaceById, fetchNamespaces } from '../model';
import { put, select } from 'redux-saga/effects';

export const NAMESPACE_STORAGE_KEY = 'atom_namespace_id';

export default class NamespaceSelectDuck extends Base {
  Param: {
    registryId?: string;
  };
  Item: NamespaceItem;

  get syncLocalStorage() {
    return true;
  }

  get lskey() {
    return NAMESPACE_STORAGE_KEY;
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      id: (state: State) => state.id,
    };
  }

  getId(o: this['Item']) {
    return o.namespaceId;
  }

  *saga() {
    yield* super.saga();
    const { types, syncLocalStorage, lskey, creators, selector } = this;
    yield runAndTakeLatest(types.SET_LIST, function*() {
      const { id, list } = selector(yield select());
      if (!id && list?.length > 0) {
        yield put(creators.select(list[0].namespaceId));
      }
    });
    yield takeEvery(types.SELECT, function*(action: Action<string>) {
      const namespaceId = action.payload;
      if (syncLocalStorage) {
        localStorage.setItem(lskey, namespaceId);
      }
    });
  }

  *getSelectedItem() {
    const { list, id, param } = this.selector(yield select());
    const selected = (list || []).find(item => this.getId(item) === id) || null;
    if (!id) {
      return selected;
    }
    if (!selected) {
      const namespace: NamespaceItem = yield fetchNamespaceById({
        registryId: param?.registryId,
        namespaceId: id,
      });
      return namespace;
    }
    return selected;
  }

  getRegistryParam(o: Partial<PageParam> = {}): PageParam<string> {
    const duck = this;
    return {
      key: 'namespaceId',
      route: 'namespaceId',
      history: true,
      order: -2,
      // 注意，如果开启了RouteParam，则此处defaults会更优先
      defaults: () => {
        return localStorage.getItem(this.lskey) || '';
      },
      alwaysVisible: true,
      type: duck.types.SELECT,
      selector: duck.selectors.id,
      creator: duck.creators.select,
      ...o,
    };
  }

  async getData(param): Promise<this['Data']> {
    const { registryId, offset, limit } = param;
    const res = await fetchNamespaces({
      registryId,
      pageSize: limit,
      pageNo: offset / limit + 1,
    });
    return res;
  }
}
