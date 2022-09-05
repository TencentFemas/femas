import DetailPageDuck from "@src/common/ducks/DetailPage";
import { createToPayload, reduceFromPayload } from "saga-duck";
import { put, select } from "redux-saga/effects";
import { resolvePromise, runAndWatchLatest } from "@src/common/helpers/saga";
import { fetchLaneRuleById } from "../model";
import { takeLatest } from "redux-saga-catch";
import { baseInfoCreate } from "../operations/baseInfo";
import { paramInfoCreate } from "../operations/paramInfo";
import { targetInfoCreate } from "../operations/targetInfo";
import { EDIT_TYPE } from "../operations/create/CreateDuck";
import { LaneRuleItem } from "../types";
import { STEPS } from "../operations/create/types";
import { Action } from "@src/common/types";

export interface ComposedId {
  ruleId: string;
}

export default class RegistryDetailDuck extends DetailPageDuck {
  ComposedId: ComposedId;
  Data: LaneRuleItem;

  get baseUrl() {
    return "/publish/lane-rule";
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
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      switch: createToPayload<string>(types.SWITCH),
      edit: createToPayload<STEPS>(types.EDIT),
    };
  }

  get rawSelectors() {
    type State = this["State"];
    return {
      ...super.rawSelectors,
      composedId: (state: State) => ({
        ruleId: state.id,
      }),
    };
  }

  get watchTypes() {
    const { types, ducks } = this;
    return [...super.watchTypes];
  }

  *saga() {
    yield* super.saga();
    yield* this.routeInitialized();
    const duck = this;
    const { types, selector, ducks, selectors, creators } = duck;

    const editType2Fn = {
      [STEPS.BASE]: baseInfoCreate,
      [STEPS.PARAM]: paramInfoCreate,
      [STEPS.TARGET]: targetInfoCreate,
    };
    yield takeLatest(types.EDIT, function*(action: Action<STEPS>) {
      const editType = action.payload;
      const { data } = selector(yield select());
      const createFn = editType2Fn[editType];
      const res = yield* resolvePromise(
        createFn(data, { editType: EDIT_TYPE.create })
      );
      if (res) {
        yield put(creators.reload());
      }
    });
  }

  async getData(param: this["ComposedId"]) {
    const { ruleId } = param;
    const res = await fetchLaneRuleById({ id: ruleId });
    return res;
  }
}
