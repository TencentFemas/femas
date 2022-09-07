import { TAB } from "./types";
import { createToPayload, DuckMap, reduceFromPayload } from "saga-duck";
import PageDuck from "@src/common/ducks/Page";
import { takeEvery, takeLatest } from "redux-saga-catch";

import { Action } from "@src/common/types";

export const TAB_STORAGE_KEY = "publish_tab_id";
export default class PlushPageDuck extends PageDuck {
  get baseUrl(): string {
    return "/publish";
  }

  get params(): this["Params"] {
    return [
      ...super.params,
      {
        key: "tab",
        type: this.types.SWITCH,
        selector: (g) => this.selector(g).tab,
      },
    ];
  }

  get syncLocalStorage() {
    return true;
  }

  get lskey() {
    return TAB_STORAGE_KEY;
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

  get reducers() {
    const { types, lskey } = this;
    return {
      ...super.reducers,
      tab: reduceFromPayload(
        types.SWITCH,
        localStorage.getItem(lskey) || TAB.LANERULE
      ),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      switch: createToPayload<TAB>(types.SWITCH),
    };
  }

  get rawSelectors() {
    type State = this["State"];
    return {
      ...super.rawSelectors,
      tab: (state: State) => state.tab,
    };
  }

  *saga() {
    yield* super.saga();
    yield* this.routeInitialized();
    const {
      types,
      ducks: {},
      lskey,
    } = this;

    yield takeEvery(types.SWITCH, function*(action: Action<TAB>) {
      const tab = action.payload;
      localStorage.setItem(lskey, tab);
    });
  }
}
