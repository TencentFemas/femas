import DetailPageDuck from '@src/common/ducks/DetailPage';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { put, select, takeLatest } from 'redux-saga/effects';
import { ComposedId, TAB } from './types';
import BaseInfoDuck from './baseinfo/PageDuck';
import VersionDuck from './version/PageDuck';
import { ConfigItem } from '../types';
import { fetchConfigById } from '../model';

export default class RegistryDetailDuck extends DetailPageDuck {
  ComposedId: ComposedId;
  Data: {};

  get baseUrl() {
    return '/config/detail';
  }

  get params() {
    const { types } = this;
    return [
      ...super.params,
      {
        key: 'namespaceId',
        type: types.SET_NAMESPACE_ID,
        defaults: '',
      },
      {
        key: 'tab',
        type: types.SWITCH,
        defaults: TAB.BaseInfo,
      },
    ];
  }

  get quickTypes() {
    enum Types {
      SWITCH,
      SET_NAMESPACE_ID,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      [TAB.BaseInfo]: BaseInfoDuck,
      [TAB.Version]: VersionDuck,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      tab: reduceFromPayload(types.SWITCH, TAB.BaseInfo),
      namespaceId: reduceFromPayload(types.SET_NAMESPACE_ID, ''),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      switch: createToPayload<string>(types.SWITCH),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      composedId: (state: State) => ({
        configId: state.id,
        namespaceId: state.namespaceId,
      }),
      tab: (state: State) => state.tab,
    };
  }

  async getData(param: this['ComposedId']) {
    const { configId, namespaceId } = param;
    const res = await fetchConfigById({ configId, namespaceId });
    return res as ConfigItem;
  }

  *saga() {
    yield* super.saga();
    yield* this.routeInitialized();
    yield* this.watchTabs();
  }

  *watchTabs() {
    const duck = this;
    const { types, ducks, selectors, creators } = duck;
    yield takeLatest([types.SWITCH, types.FETCH_DONE], function*() {
      const composedId = selectors.composedId(yield select());
      const tab = selectors.tab(yield select());
      const data = selectors.data(yield select());

      if (!composedId) {
        return;
      }
      const subDuck = ducks[tab];
      yield put(subDuck.creators.load(composedId, data));
    });
    yield takeLatest(ducks.version.types.RELOAD, function*() {
      yield put(creators.reload());
    });
  }
}
