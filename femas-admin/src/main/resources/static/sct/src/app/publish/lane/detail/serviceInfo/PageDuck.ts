import DetailPage from "@src/common/ducks/DetailPage";
import { ComposedId } from "../PageDuck";
import { createToPayload, reduceFromPayload } from "saga-duck";
import { LaneItem } from "../../types";
import { select, put } from "redux-saga/effects";
import { takeLatest } from "redux-saga-catch";
import { Modal } from "tea-component";
import { configureLane } from "../../model";
import { Action } from "@src/common/types";
import { create } from "../../operations/goupInfo";
import { EDIT_TYPE } from "../../operations/create/CreateDuck";
import { resolvePromise } from "@src/common/helpers/saga";

type CHANGE_ENTRANCE_ACTION = {
  index: number;
  val: boolean;
};

export default class BaseInfoDuck extends DetailPage {
  baseUrl = null;
  ComposedId: ComposedId;
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

    yield takeLatest(types.CHANGE_ENTRANCE, function*(
      action: Action<CHANGE_ENTRANCE_ACTION>
    ) {
      let { data } = selector(yield select());
      const { index, val } = action.payload;
      data.laneServiceList[index].entrance = val;
      yield configureLane(data);
      yield put({ type: types.CHANGE_DONE });
    });

    yield takeLatest(types.DELETE, function*(action: Action<number>) {
      let { data } = selector(yield select());
      const index = action.payload;
      const yes = yield Modal.confirm({
        message: "移除部署组",
        description:
          "确认移除部署组？ 移除部署组后， 设置在泳道上的灰度规则将不会对此部署组生效",
        okText: "删除",
        cancelText: "取消",
      });
      if (!yes) return;

      data.laneServiceList.splice(index, 1);
      yield configureLane(data);
      yield put({ type: types.CHANGE_DONE });
    });

    yield takeLatest(types.EDIT, function*() {
      const { data } = selector(yield select());
      const res = yield* resolvePromise(
        create(data, { editType: EDIT_TYPE.create })
      );
      yield put({ type: types.CHANGE_DONE });
    });
  }

  async getData() {
    return {} as LaneItem;
  }
}
