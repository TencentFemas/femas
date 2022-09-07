import DetailPage from "@src/common/ducks/DetailPage";
import { ComposedId } from "../PageDuck";
import { createToPayload, reduceFromPayload } from "saga-duck";
import { LaneItem } from "../../types";
import { Modal } from "tea-component";
import { configureLane } from "../../model";
import { Action } from "@src/common/types";
import { create } from "../../operations/goupInfo";
import { EDIT_TYPE } from "../../operations/create/CreateDuck";
import { resolvePromise } from "@src/common/helpers/saga";
import GridPageDuck, { Filter as BaseFilter } from "@src/common/ducks/GridPage";
import { FilterTime } from "@src/common/types";

type CHANGE_ENTRANCE_ACTION = {
  index: number;
  val: boolean;
};

interface Filter extends BaseFilter {
  filterTime: FilterTime;
}

export default class BaseInfoDuck extends GridPageDuck {
  Filter: Filter;
  Item: {};
  baseUrl = null;
  Data: LaneItem;

  get initialFetch() {
    return false;
  }

  get watchTypes() {
    return [...super.watchTypes, this.types.LOAD_DATA];
  }

  get quickTypes() {
    enum Types {
      LOAD_DATA,
      DELETE,
      CHANGE_DONE,
      CHANGE_ENTRANCE,
      EDIT,
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
      data: reduceFromPayload<LaneItem>(types.LOAD_DATA, null),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      loadData: createToPayload<ComposedId>(types.LOAD_DATA),
      changeEntrance: createToPayload<CHANGE_ENTRANCE_ACTION>(
        types.CHANGE_ENTRANCE
      ),
      delete: createToPayload<number>(types.DELETE),
      edit: createToPayload<void>(types.EDIT),
    };
  }

  get rawSelectors() {
    type State = this["State"];
    return {
      ...super.rawSelectors,
    };
  }

  *saga() {
    yield* super.saga();
    const { types, creators, selectors, selector } = this;
  }

  async getData() {
    return { list: [], totalCount: 0 };
  }
}
