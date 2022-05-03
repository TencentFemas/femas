import Base from '@src/common/ducks/Page';
import { put, takeEvery } from 'redux-saga/effects';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { DynamicVersionSelectDuck } from './DynamicSelectDuck';
import { ComposedId } from '../../types';

export default class PageDuck extends Base {
  baseUrl = null;

  get quickTypes() {
    enum Types {
      LOAD,
      CREATE_VERSION_DUCK,
      CHECK_SUB_DUCK_INIT,
      SET_DATA_LIST,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      versionSelectDuck: DynamicVersionSelectDuck,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      composedId: reduceFromPayload(types.LOAD, {} as ComposedId),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      load: createToPayload(types.LOAD),
      createVersionDuck: createToPayload(types.CREATE_VERSION_DUCK),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      composedId: (state: State) => state.composedId,
    };
  }

  *saga() {
    yield* super.saga();
    const { types, ducks } = this;
    yield takeEvery(types.CREATE_VERSION_DUCK, function*(action: any) {
      const { params, duckId } = action.payload;
      const { versionSelectDuck } = ducks;
      yield put(versionSelectDuck.creators.createDuck(duckId));
      const subDuck = versionSelectDuck.getDuck(duckId);
      yield put(subDuck.creators.load(params));
    });
  }
}
