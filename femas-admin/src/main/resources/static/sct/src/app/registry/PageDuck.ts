import GridPageDuck, { Filter as BaseFilter } from '../../common/ducks/GridPage';
import { RegistryItem } from './types';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { put } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import create from './operations/create';
import { resolvePromise } from 'saga-duck/build/helper';
import { Action } from '@src/common/types';
import remove from './operations/remove';
import { describeRegistryClusters } from './model';

interface Filter extends BaseFilter {
  registryType: string;
  status: string;
}

export default class NamespacePageDuck extends GridPageDuck {
  Filter: Filter;
  Item: RegistryItem;

  get baseUrl() {
    return '/registry';
  }

  get quickTypes() {
    enum Types {
      CREATE,
      EDIT,
      REMOVE,
      SET_FILTER_TYPE,
      SET_FILTER_STATUS,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get watchTypes() {
    return [...super.watchTypes, this.types.SET_FILTER_TYPE, this.types.SET_FILTER_STATUS];
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      registryTypeFilter: reduceFromPayload(types.SET_FILTER_TYPE, ''),
      statusFilter: reduceFromPayload(types.SET_FILTER_STATUS, ''),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      create: createToPayload<void>(types.CREATE),
      edit: createToPayload<RegistryItem>(types.EDIT),
      remove: createToPayload<RegistryItem>(types.REMOVE),
      setTypeFilter: createToPayload<string>(types.SET_FILTER_TYPE),
      setStatusFilter: createToPayload<string>(types.SET_FILTER_STATUS),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      filter: (state: State) => ({
        page: state.page,
        count: state.count,
        registryType: state.registryTypeFilter,
        status: state.statusFilter,
      }),
    };
  }

  *saga() {
    yield* super.saga();
    const { types, creators } = this;
    yield takeLatest(types.CREATE, function*() {
      const res = yield* resolvePromise(create(null, { addMode: true }));
      if (res) {
        yield put(creators.reload());
      }
    });
    yield takeLatest(types.EDIT, function*(action: Action<RegistryItem>) {
      const item = action.payload;
      const res = yield* resolvePromise(
        create(item, {
          addMode: false,
          registryId: item.registryId,
        }),
      );
      if (res) {
        yield put(creators.reload());
      }
    });
    yield takeLatest(types.REMOVE, function*(action: Action<RegistryItem>) {
      const item = action.payload;
      const res = yield* resolvePromise(remove(item));
      if (res) {
        yield put(creators.reload());
      }
    });
  }

  async getData(params: Filter) {
    const { registryType, status } = params;
    const data = await describeRegistryClusters({
      registryType,
      status: status && +status,
    });
    return data;
  }
}
