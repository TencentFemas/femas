import NamespaceSelectDuck from "@src/app/namespace/components/NamespaceSelectDuck";
import GridPageDuck, { Filter as BaseFilter } from "@src/common/ducks/GridPage";
import { put, select, takeLatest } from "redux-saga/effects";
import { createToPayload } from "saga-duck";
import { resolvePromise } from "saga-duck/build/helper";
import { Modal } from "tea-component";
import {
  adjustLaneRulePriority,
  configureLaneRule,
  deleteLaneRule,
  fetchLaneRulePages,
} from "./model";
import { LaneRuleItem } from "./types";
import { Action } from "@src/common/types";
import create from "./operations/create";
import { EDIT_TYPE } from "./operations/create/CreateDuck";

interface Filter extends BaseFilter {
  remark: string;
  ruleId: string;
  ruleName: string;
}

export default class LaneRulePageDuck extends GridPageDuck {
  Filter: Filter;
  Item: LaneRuleItem;

  get recordKey() {
    return "ruleId";
  }

  get baseUrl() {
    return "/publish";
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
      CHANGE_ORDER,
      CHANGE_ENABLE,
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
      delete: createToPayload<LaneRuleItem>(types.DELETE),
      changeOrder: createToPayload<{
        laneId: string;
        targetLaneId: string;
      }>(types.CHANGE_ORDER),
      changeEnable: createToPayload<LaneRuleItem>(types.CHANGE_ENABLE),
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
      filter: (state: State) => ({
        page: state.page,
        count: state.count,
        remark: "",
        ruleId: "",
        ruleName: state.keyword,
      }),
    };
  }

  *saga() {
    yield* super.saga();
    const { types, creators, selectors } = this;

    yield takeLatest(types.ADD, function*() {
      const res = yield* resolvePromise(
        create({} as LaneRuleItem, { editType: EDIT_TYPE.create })
      );
      if (res) {
        yield put(creators.reload());
      }
    });
    yield takeLatest(types.DELETE, function*(action: Action<LaneRuleItem>) {
      const { ruleId } = action.payload;
      const yes = yield Modal.confirm({
        message: "确认删除当前所选泳道？",
        description: "删除后，该泳道将会被清空，且无法恢复。",
        okText: "删除",
        cancelText: "取消",
      });
      if (!yes) return;
      yield deleteLaneRule({ id: ruleId });
      yield put(creators.reload());
    });
    yield takeLatest(types.CHANGE_ENABLE, function*(
      action: Action<LaneRuleItem>
    ) {
      yield configureLaneRule(action.payload);
      yield put(creators.reload());
    });
    yield takeLatest(types.CHANGE_ORDER, function*(
      action: Action<{
        laneId: string;
        targetLaneId: string;
      }>
    ) {
      const { laneId, targetLaneId } = action.payload;
      yield adjustLaneRulePriority({ laneId, targetLaneId });
      yield put(creators.reload());
    });
  }

  async getData(filters: this["Filter"]) {
    const { page, count, remark, ruleId, ruleName } = filters;

    const res = await fetchLaneRulePages({
      pageNo: page,
      pageSize: count,
      remark,
      ruleId,
      ruleName,
    });

    return {
      totalCount: res.totalCount,
      list: res.list as Array<LaneRuleItem>,
    };
  }
}
