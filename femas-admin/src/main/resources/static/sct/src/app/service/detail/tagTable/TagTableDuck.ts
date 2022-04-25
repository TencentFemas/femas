import Base from '@src/common/ducks/Form';
import { put, takeEvery } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { reduceFromPayload } from 'saga-duck';
import DynamicSelectDuck from './DynamicSelectDuck';

export default class PageDuck extends Base {
  get quickTypes() {
    enum Types {
      LOAD,
      SET_DATA,
      CREATE_DUCK,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      selectDuck: DynamicSelectDuck,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      data: reduceFromPayload(types.SET_DATA, {}),
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
      createDuck: duckId => ({
        type: types.CREATE_DUCK,
        payload: duckId,
      }),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      data: (state: State) => state.data,
    };
  }

  *saga() {
    yield* super.saga();
    const { ducks, types } = this;
    yield takeLatest(types.LOAD, function*(action) {
      const { data } = action.payload;
      yield put({ type: types.SET_DATA, payload: data });
    });
    yield takeEvery(types.CREATE_DUCK, function*(action: any) {
      const { selectDuck } = ducks;
      const { options, duckId } = action.payload;
      yield put(selectDuck.creators.createDuck(duckId));
      const subDuck = selectDuck.getDuck(duckId);
      yield put(subDuck.creators.load(options));
    });
  }
}
