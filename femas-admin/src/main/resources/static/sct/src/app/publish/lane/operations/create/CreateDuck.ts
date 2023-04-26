import Form from "@src/common/ducks/Form";
import FormDialog from "@src/common/ducks/FormDialog";
import { nameTipMessage } from "@src/common/types";
import { put, select } from "redux-saga/effects";
import { createToPayload, reduceFromPayload } from "saga-duck";
import { resolvePromise } from "saga-duck/build/helper";
import { configureLane } from "../../model";
import { LaneItem } from "../../types";
import { STEPS, STEPS_LABLES } from "./types";
import { takeLatest } from "redux-saga-catch";

export enum EDIT_TYPE {
  create = "create",
}

export interface DialogOptions {
  laneId?: string;
  editType: EDIT_TYPE;
}

export class CreateForm extends Form {
  Values: LaneItem;
  Meta: {};

  validate(v: this["Values"], meta: this["Meta"]) {
    return validator(v, meta);
  }
}

const validator = CreateForm.combineValidators<LaneItem, {}>({
  laneName(v) {
    if (!v) return "请填写泳道名称";
    if (v?.length > 60 || !/^[a-z0-9]([-_a-z0-9]*[a-z0-9])?$/.test(v))
      return nameTipMessage;
  },
  laneServiceList(v, record) {
    if (!v || v.length <= 0) return "请选择部署组";
    // if (!record.stableServiceList || record.stableServiceList.length <= 0)
    //   return "请指定稳定版本";
  },
});

const step2validatField = {
  [STEPS.BASE]: ["laneName"],
  [STEPS.NAMESPACE]: ["laneServiceList"],
};

export default class CreateDuck extends FormDialog {
  get Form() {
    return CreateForm;
  }

  get quickTypes() {
    enum Types {
      CHANGE_STEP,
      PREV_STEP,
      NEXT_STEP,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducers() {
    const {
      types,
      ducks: { form },
    } = this;
    return {
      ...super.reducers,
      step: reduceFromPayload(types.CHANGE_STEP, STEPS.BASE),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      changeStep: createToPayload(types.CHANGE_STEP),
      prevStep: createToPayload<void>(types.PREV_STEP),
      nextStep: createToPayload<void>(types.NEXT_STEP),
    };
  }

  get rawSelectors() {
    type State = this["State"];
    return {
      ...super.rawSelectors,
      step: (state: State) => state.step,
    };
  }

  *saga() {
    yield* super.saga();
    const {
      types,
      selectors,
      creators,
      selector,
      ducks: { form },
    } = this;

    yield takeLatest(types.PREV_STEP, function*() {
      const { step } = selector(yield select());
      const labels = Object.keys(STEPS_LABLES) as Array<STEPS>;
      let findIndex = labels.findIndex((v) => v === step);
      if (findIndex !== -1 && findIndex !== 0) findIndex -= 1;
      else findIndex = 0;
      yield put(creators.changeStep(labels[findIndex]));
    });
    yield takeLatest(types.NEXT_STEP, function*() {
      const { step } = selector(yield select());

      // TODO， 未找到 获取错误字段的api， 暂时循环
      for (let field of step2validatField[step]) {
        const invalid = form.selectors.firstInvalid(yield select(), [field]);
        if (invalid) {
          yield put(form.creators.setAllTouched(true));
          return;
        }
      }
      yield put(form.creators.setAllTouched(false));

      const labels = Object.keys(STEPS_LABLES) as Array<STEPS>;
      let findIndex = labels.findIndex((v) => v === step);
      if (findIndex !== -1 && findIndex !== labels.length) findIndex += 1;
      else findIndex = labels.length;
      yield put(creators.changeStep(labels[findIndex]));
    });
  }

  *onSubmit() {
    const {
      selectors,
      ducks: { form },
    } = this;
    const {
      laneId,
      laneName,
      laneServiceList = [],
      stableServiceList = [],
      remark,
    } = form.selectors.values(yield select());
    const res = yield* resolvePromise(
      configureLane({
        laneId,
        laneName,
        laneServiceList: laneServiceList.map(
          ({ entrance, namespaceId, namespaceName, serviceName, version }) => {
            return {
              entrance,
              namespaceId,
              namespaceName,
              serviceName,
              version,
            };
          }
        ),
        stableServiceList: stableServiceList.map(
          ({ entrance, namespaceId, namespaceName, serviceName, version }) => {
            return {
              entrance,
              namespaceId,
              namespaceName,
              serviceName,
              version,
            };
          }
        ),
        remark,
      })
    );
    return res;
  }

  *onShow() {
    yield* super.onShow();
    const {
      selectors,
      ducks: { form },
    } = this;
    console.log("show");
    // yield put(form.creators.setMeta(options));
  }
}
