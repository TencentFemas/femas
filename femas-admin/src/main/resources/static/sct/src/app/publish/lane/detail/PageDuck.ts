import DetailPageDuck from "@src/common/ducks/DetailPage";
import { createToPayload, reduceFromPayload } from "saga-duck";
import { put, select } from "redux-saga/effects";
import ServiceInfo from "./serviceInfo/PageDuck";
import MonitorInfo from "./monitorInfo/PageDuck";
import { resolvePromise, runAndWatchLatest } from "@src/common/helpers/saga";
import { fetchLaneById } from "../model";
import { LaneItem } from "../../lane/types";
import { TAB } from "./types";
import { takeLatest } from "redux-saga-catch";
import { create } from "../operations/baseInfo/StepOne";
import { EDIT_TYPE } from "../operations/create/CreateDuck";

export interface ComposedId {
  laneId: string;
}

export default class RegistryDetailDuck extends DetailPageDuck {
  ComposedId: ComposedId;
  Data: LaneItem;

  get baseUrl() {
    return "/publish/lane";
  }

  get quickTypes() {
    enum Types {
      SWITCH,
      EDIT,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      [TAB.GROUP]: ServiceInfo,
      [TAB.MOMITOR]: MonitorInfo,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      tab: reduceFromPayload(types.SWITCH, TAB.GROUP),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      switch: createToPayload<string>(types.SWITCH),
      edit: createToPayload<void>(types.EDIT),
    };
  }

  get rawSelectors() {
    type State = this["State"];
    return {
      ...super.rawSelectors,
      composedId: (state: State) => ({
        laneId: state.id,
      }),
    };
  }

  get watchTypes() {
    const { types, ducks } = this;
    return [...super.watchTypes, ducks[TAB.GROUP].types.CHANGE_DONE];
  }

  *saga() {
    yield* super.saga();
    yield* this.routeInitialized();
    // yield* this.watchTabs();
    const duck = this;
    const { types, selector, ducks, selectors, creators } = duck;

    yield takeLatest(types.FETCH_DONE, function*() {
      const { data, tab } = selector(yield select());
      const subDuck = ducks[tab];
      yield put(subDuck.creators.loadData(data));
    });

    yield takeLatest(types.EDIT, function*() {
      const { data } = selector(yield select());
      const res = yield* resolvePromise(
        create(data, { editType: EDIT_TYPE.create })
      );
      if (res) {
        yield put(creators.reload());
      }
    });
  }

  async getData(param: this["ComposedId"]) {
    const { laneId } = param;
    const res = await fetchLaneById({ id: laneId });
    return res;
  }

  *watchTabs() {
    const duck = this;
    const { types, selector, ducks, selectors } = duck;
    yield runAndWatchLatest(
      [types.SWITCH],
      (g) => selector(g).tab,
      function*(tab) {
        const composedId = selectors.composedId(yield select());
        const { data } = selector(yield select());
        if (!composedId) return;
        const subDuck = ducks[tab];

        yield put(subDuck.creators.loadData(data));
      }
    );
  }
}
