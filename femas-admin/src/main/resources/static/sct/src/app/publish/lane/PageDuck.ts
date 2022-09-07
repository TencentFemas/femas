import NamespaceSelectDuck from "@src/app/namespace/components/NamespaceSelectDuck";
import GridPageDuck, { Filter as BaseFilter } from "@src/common/ducks/GridPage";
import { put, select } from "redux-saga/effects";
import { takeEvery, takeLatest } from "redux-saga-catch";
import { createToPayload } from "saga-duck";
import { resolvePromise } from "saga-duck/build/helper";
import { Modal } from "tea-component";
import { deleteLane, fetchLaneInfoPages } from "./model";
import { LaneItem } from "./types";
import { Action } from "@src/common/types";
import create from "./operations/create";
import { EDIT_TYPE } from "./operations/create/CreateDuck";
import { TAB } from "../types";

interface Filter extends BaseFilter {
  laneId: string;
  laneName: string;
  remark: string;
}

export default class LaneRulePageDuck extends GridPageDuck {
  Filter: Filter;
  Item: LaneItem;

  get recordKey() {
    return "laneId";
  }

  get baseUrl() {
    return "/publish?tab=" + TAB.LANE;
  }

  get initialFetch() {
    return false;
  }

  get watchTypes() {
    return [...super.watchTypes];
  }

  get params() {
    return [...super.params];
  }

  get quickTypes() {
    enum Types {
      ADD,
      DELETE,
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
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      add: createToPayload<void>(types.ADD),
      delete: createToPayload<LaneItem>(types.DELETE),
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
    };
  }

  get rawSelectors() {
    type State = this["State"];
    return {
      ...super.rawSelectors,
      filter: (state: State) => {
        return {
          page: state.page,
          count: state.count,
          // keyword: state.keyword,
          laneId: "",
          laneName: state.keyword,
          remark: "",
        };
      },
    };
  }

  *saga() {
    yield* super.saga();
    const { types, creators, selectors } = this;

    yield takeLatest(types.ADD, function*() {
      const res = yield* resolvePromise(
        create({} as LaneItem, { editType: EDIT_TYPE.create })
      );
      if (res) {
        yield put(creators.reload());
      }
    });
    yield takeLatest(types.DELETE, function*(action: Action<LaneItem>) {
      const { laneId } = action.payload;
      const yes = yield Modal.confirm({
        message: "确认删除当前所选泳道？",
        description: "删除后，该泳道将会被清空，且无法恢复。",
        okText: "删除",
        cancelText: "取消",
      });
      if (!yes) return;
      yield deleteLane({ id: laneId });
      yield put(creators.reload());
    });
  }

  async getData(filters: this["Filter"]) {
    const { page, count, keyword, laneId, laneName, remark } = filters;

    const res = await fetchLaneInfoPages({
      pageNo: page,
      pageSize: count,
      laneId,
      laneName,
      remark,
    });

    return {
      totalCount: res.totalCount,
      list: res.list as Array<LaneItem>,
    };
  }
}
