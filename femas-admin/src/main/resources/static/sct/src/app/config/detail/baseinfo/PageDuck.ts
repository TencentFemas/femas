import DetailPage from '@src/common/ducks/DetailPage';
import { ComposedId } from '../types';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { ConfigItem } from '../../types';
import { put, select, takeLatest } from 'redux-saga/effects';
import { resolvePromise } from 'saga-duck/build/helper';
import modify from '../../operations/modifyDesc';
import { Action } from '@src/common/types';
import { fetchConfigById } from '../../model';

export default class BaseInfoDuck extends DetailPage {
  Data: ConfigItem;
  ComposedId: ComposedId;

  get baseUrl() {
    return null;
  }

  get quickTypes() {
    enum Types {
      LOAD,
      EDIT_DESC,
      SET_COMPOSED_ID,
      SET_CONFIG_DATA,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      composedId: reduceFromPayload(types.SET_COMPOSED_ID, {} as ComposedId),
      configData: reduceFromPayload(types.SET_CONFIG_DATA, {} as ConfigItem),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      load: (composedId, data) => ({
        type: types.LOAD,
        payload: { composedId, data },
      }),
      editDesc: createToPayload<void>(types.EDIT_DESC),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      composedId: (state: State) => state.composedId,
      configData: (state: State) => state.configData,
    };
  }

  *saga() {
    yield* super.saga();
    const { types, selectors } = this;
    yield takeLatest(types.LOAD, function*(action: Action<{ composedId: ComposedId; data: ConfigItem }>) {
      const { composedId, data } = action.payload;
      yield put({ type: types.SET_COMPOSED_ID, payload: composedId });
      yield put({ type: types.SET_CONFIG_DATA, payload: data });
    });
    yield takeLatest(types.EDIT_DESC, function*() {
      const { namespaceId, configId } = selectors.composedId(yield select());
      const data = selectors.configData(yield select());
      const res = yield* resolvePromise(
        modify(data as ConfigItem, {
          namespaceId,
        }),
      );
      if (res) {
        const result = yield fetchConfigById({ configId, namespaceId });
        yield put({ type: types.SET_CONFIG_DATA, payload: result });
      }
    });
  }

  async getData() {
    return {} as ConfigItem;
  }
}
