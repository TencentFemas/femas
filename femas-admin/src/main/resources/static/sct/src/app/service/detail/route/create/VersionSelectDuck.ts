import SearchableSelect from '@src/common/ducks/SearchableSelect';
import { put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { reduceFromPayload } from 'saga-duck';
import { SelectItem } from './VersionSelect';
import { TSF_ROUTE_DEST_ELSE } from '../types';
import { describeServiceInstance } from '@src/app/service/model';
import { InstanceItem } from '@src/app/service/types';
import { getAllList } from '@src/common/util/apiRequest';

interface ComposedId {
  needEmptyItem: boolean;
  emptyItem: any;
  serviceName: string;
  namespaceId: string;
}

export default class VersionSelectDuck extends SearchableSelect {
  baseUrl = null;
  Param: ComposedId;

  get quickTypes() {
    enum Types {
      SET_DATA,
      SET_LOADING,
      SET_COMPOSE_ID,
      SET_INIT,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get autoSearch() {
    return false;
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      data: reduceFromPayload(types.SET_DATA, {}),
      init: reduceFromPayload(types.SET_INIT, false),
      composedId: reduceFromPayload(types.SET_COMPOSE_ID, {} as ComposedId),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      load: payload => ({
        type: types.LOAD,
        payload: {
          ...payload,
          needEmptyItem: true,
          emptyItem: {
            value: TSF_ROUTE_DEST_ELSE,
            name: '其他版本',
          },
        },
      }),
      setLoading: item => ({ type: types.SET_LOADING, payload: item }),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      init: (state: State) => state.init,
      data: (state: State) => state.data,
      composedId: (state: State) => state.composedId,
      params: (state: State) => ({
        ...state.composedId,
        keyword: state.keyword,
      }),
    };
  }

  *saga() {
    yield* super.saga();
    const { types } = this;
    yield takeLatest(types.LOAD, function*(action) {
      yield put({ type: types.SET_COMPOSE_ID, payload: action.payload });
      yield put({ type: types.SET_INIT, payload: true });
    });
  }

  async getData(param: this['GetDataParam']) {
    const { namespaceId, serviceName } = param;
    const res = await getAllList(describeServiceInstance)({
      namespaceId,
      serviceName,
    });
    return {
      totalCount: res?.totalCount || 0,
      list:
        res.list?.map((d: InstanceItem) => ({
          value: d.serviceVersion,
          text: d.serviceVersion,
        })) || [],
    };
  }

  *getSelectedItem(): any {
    const { id, param } = this.selector(yield select());
    let selected = yield super.getSelectedItem();
    if (!selected && param.needEmptyItem) {
      selected = id === this.getId(param.emptyItem) ? param.emptyItem : null;
    }
    return selected;
  }

  getId(item: SelectItem) {
    return item.value;
  }
}
