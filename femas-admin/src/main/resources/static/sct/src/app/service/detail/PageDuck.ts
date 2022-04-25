import DetailPageDuck from '@src/common/ducks/DetailPage';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { put, select, takeLatest } from 'redux-saga/effects';
import { ComposedId, TAB } from './types';
import BaseInfoDuck from './baseinfo/PageDuck';
import ApiDuck from './api/PageDuck';
import EventDuck from './event/PageDuck';
import AuthDuck from './auth/PageDuck';
import LimitDuck from './limit/PageDuck';
import RouteDuck from './route/PageDuck';
import FuseDuck from './fuse/PageDuck';
import { describeServiceOverview } from '../model';
import { ServiceItem } from '../types';

export default class RegistryDetailDuck extends DetailPageDuck {
  ComposedId: ComposedId;
  Data: ServiceItem;

  get baseUrl() {
    return '/service/detail';
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
        key: 'registryId',
        type: types.SET_REGISTRY_ID,
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
      SET_REGISTRY_ID,
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
      [TAB.Api]: ApiDuck,
      [TAB.Event]: EventDuck,
      [TAB.Auth]: AuthDuck,
      [TAB.Limit]: LimitDuck,
      [TAB.Fuse]: FuseDuck,
      [TAB.Route]: RouteDuck,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      tab: reduceFromPayload(types.SWITCH, TAB.BaseInfo),
      namespaceId: reduceFromPayload(types.SET_NAMESPACE_ID, ''),
      registryId: reduceFromPayload(types.SET_REGISTRY_ID, ''),
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
        serviceName: state.id,
        namespaceId: state.namespaceId,
        registryId: state.registryId,
      }),
      tab: (state: State) => state.tab,
    };
  }

  async getData(composedId: this['ComposedId']) {
    const { namespaceId, serviceName } = composedId;
    return (await describeServiceOverview({
      namespaceId,
      serviceName,
    })) as ServiceItem;
  }

  *saga() {
    yield* super.saga();
    yield* this.routeInitialized();
    yield* this.watchTabs();
  }

  *watchTabs() {
    const duck = this;
    const { types, ducks, selectors } = duck;
    yield takeLatest([types.SWITCH, types.FETCH_DONE], function*() {
      const composedId = selectors.composedId(yield select());
      const tab = selectors.tab(yield select());
      const data = selectors.data(yield select());
      if (!composedId || !data) {
        return;
      }
      const subDuck = ducks[tab];
      yield put(subDuck.creators.load({ ...composedId, versions: data?.versions }));
    });
  }
}
