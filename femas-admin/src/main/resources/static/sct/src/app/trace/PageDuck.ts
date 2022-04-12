import { put, select } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import { takeLatest } from 'redux-saga-catch';
import { TAB } from './types';
import PageDuck from '@src/common/ducks/Page';
import SearchDuck from './search/PageDuck';
import SpanDuck from './detail/PageDuck';
import { reduceFromPayload } from 'saga-duck';
import NamespaceSelectDuck from '../namespace/components/NamespaceSelectDuck';

enum Types {
  SWITCH,
  SET_LS,
}

export default class TraceDuck extends PageDuck {
  get baseUrl() {
    return '/trace';
  }

  get params() {
    return [
      ...super.params,
      this.ducks.namespace.getRegistryParam(),
      {
        key: 'needLS',
        type: this.types.SET_LS,
        defaults: '',
      },
      {
        key: 'tab',
        type: this.types.SWITCH,
        defaults: TAB.SEARCH,
      },
    ];
  }

  get quickTypes() {
    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      search: SearchDuck,
      span: SpanDuck,
      namespace: NamespaceSelectDuck,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      tab: reduceFromPayload(types.SWITCH, TAB.SEARCH),
      needLS: reduceFromPayload(types.SET_LS, ''),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      switch: (payload: TAB) => ({ type: types.SWITCH, payload }),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      tab: (state: State) => state.tab,
      needLS: (state: State) => state.needLS,
    };
  }

  *saga() {
    const {
      types,
      selectors,
      ducks,
      ducks: { namespace },
    } = this;
    yield* namespace.load(null);
    yield* super.saga();
    yield takeLatest(namespace.types.SET_SELECTED, function*() {
      // 重置Tab数据
      // 切换地域，重置Tab数据
      const tab = yield select(selectors.tab);
      const spanDuck = ducks[TAB.SPAN];
      const searchDuck = ducks[TAB.SEARCH];
      const needLS = yield select(selectors.needLS);
      const namespaceId = namespace.selectors.id(yield select());

      const searchCreators = searchDuck.creators;
      yield put({ type: searchDuck.types.SET_SEARCHED, payload: false });
      yield put({ type: spanDuck.types.SET_SEARCHED, payload: false });
      yield put(
        searchCreators.load({
          namespaceId,
          needLS,
          tabActive: tab === TAB.SEARCH,
          clearStorage: true,
        }),
      );
      const detailCreators = spanDuck.creators;
      yield put(
        detailCreators.load({
          namespaceId,
          needLS,
          tabActive: tab === TAB.SPAN,
        }),
      );
    });

    yield takeLatest(types.SWITCH, function*() {
      yield delay(10);
      const tab = yield select(selectors.tab);
      const childDuck = ducks[tab];
      const namespaceId = namespace.selectors.id(yield select());
      if (!childDuck) {
        return;
      }
      const childCreators = ducks[tab].creators;
      // 如果子页没有定义load，无视
      if (!childCreators.load) {
        return;
      }

      if (childDuck.selectors) {
        const searched = yield select(childDuck.selectors.searched);
        if (searched) {
          return;
        }
      }

      const needLS = yield select(selectors.needLS);
      yield put(childCreators.load({ namespaceId, needLS, tabActive: true }));
    });
    yield takeLatest(ducks.span.types.INSPECT_DONE, function*() {
      const namespaceId = namespace.selectors.id(yield select());
      yield put({ type: types.SET_LS, payload: 1 });
      yield put(ducks.search.creators.load({ namespaceId, needLS: 1 }));
      yield put({ type: types.SWITCH, payload: TAB.SEARCH });
    });
  }
}
