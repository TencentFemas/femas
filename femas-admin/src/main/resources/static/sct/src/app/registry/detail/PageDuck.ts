import DetailPageDuck from '@src/common/ducks/DetailPage';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { put, select } from 'redux-saga/effects';
import { TAB } from './types';
import BaseInfoDuck from './baseinfo/PageDuck';
import { runAndWatchLatest } from '@src/common/helpers/saga';

export interface ComposedId {
  registryId: string;
}

export default class RegistryDetailDuck extends DetailPageDuck {
  ComposedId: ComposedId;
  Data: {};

  get baseUrl() {
    return '/registry/detail';
  }

  get quickTypes() {
    enum Types {
      SWITCH,
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
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      tab: reduceFromPayload(types.SWITCH, TAB.BaseInfo),
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
        registryId: state.id,
      }),
    };
  }

  async getData() {
    return {};
  }

  *saga() {
    yield* super.saga();
    yield* this.routeInitialized();
    yield* this.watchTabs();
  }

  *watchTabs() {
    const duck = this;
    const { types, selector, ducks, selectors } = duck;
    yield runAndWatchLatest(
      [types.SWITCH],
      g => selector(g).tab,
      function*(tab) {
        const composedId = selectors.composedId(yield select());

        if (!composedId) {
          return;
        }
        const subDuck = ducks[tab];
        yield put(subDuck.creators.load(composedId));
      },
    );
  }
}
