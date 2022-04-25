import { PageParam } from '@src/common/ducks/Page';
import Base from '@src/common/ducks/SearchableSelect';
import { describeRegistryCluster, describeRegistryClusters } from '../model';
import { RegistryItem } from '../types';
import { runAndTakeLatest, takeEvery } from 'redux-saga-catch';
import { Action } from '@src/common/types';
import { put, select } from 'redux-saga/effects';

export const REGISTRY_STORAGE_KEY = 'atom_registry_id';

export default class RegistrySelectDuck extends Base {
  Param: void;
  Item: RegistryItem;

  get syncLocalStorage() {
    return true;
  }

  get lskey() {
    return REGISTRY_STORAGE_KEY;
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      id: (state: State) => state.id,
    };
  }

  getId(o: this['Item']) {
    return o.registryId;
  }

  *getSelectedItem() {
    const { list, id } = this.selector(yield select());
    const selected = (list || []).find(item => this.getId(item) === id) || null;
    if (!id) {
      return selected;
    }
    if (!selected) {
      const registry: RegistryItem = yield describeRegistryCluster({
        registryId: id,
      });
      return registry;
    }
    return selected;
  }

  *saga() {
    yield* super.saga();
    const { types, syncLocalStorage, lskey, creators, selector } = this;
    yield runAndTakeLatest(types.SET_LIST, function*() {
      const { id, list } = selector(yield select());
      if (!id && list?.length > 0) {
        yield put(creators.select(list[0].registryId));
      }
    });
    yield takeEvery(types.SELECT, function*(action: Action<string>) {
      const registryId = action.payload;
      if (syncLocalStorage) {
        localStorage.setItem(lskey, registryId);
      }
    });
  }

  getRegistryParam(o: Partial<PageParam> = {}): PageParam<string> {
    return {
      key: 'registryId',
      route: 'registryId',
      history: true,
      order: -2,
      // 注意，如果开启了RouteParam，则此处defaults会更优先
      defaults: () => {
        return localStorage.getItem(this.lskey) || '';
      },
      alwaysVisible: true,
      type: this.types.SELECT,
      selector: this.selectors.id,
      creator: this.creators.select,
      ...o,
    };
  }

  async getData(): Promise<this['Data']> {
    return describeRegistryClusters();
  }
}
