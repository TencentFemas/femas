import Form from "@src/common/ducks/Form";
import FormDialog from "@src/common/ducks/FormDialog";
import { nameTipMessage } from "@src/common/types";
import { put, select } from "redux-saga/effects";
import { createToPayload, reduceFromPayload } from "saga-duck";
import { resolvePromise } from "saga-duck/build/helper";
import { configureLaneRule } from "../../model";
import { GRAYTYPE, LaneRuleItem, LaneRuleTag } from "../../types";
import { STEPS, STEPS_LABLES } from "./types";
import { takeEvery, takeLatest } from "redux-saga-catch";

export enum EDIT_TYPE {
  create = "create",
}

export interface DialogOptions {
  laneId?: string;
  editType: EDIT_TYPE;
}

export class CreateForm extends Form {
  Values: LaneRuleItem;
  Meta: {};

  validate(v: this["Values"], meta: this["Meta"]) {
    return validator(v, meta);
  }
}

const validator = CreateForm.combineValidators<LaneRuleItem, {}>({
  ruleName(v) {
    if (!v) return "请填写规则名称";
    if (v?.length > 60 || !/^[a-z0-9]([-_a-z0-9]*[a-z0-9])?$/.test(v))
      return nameTipMessage;
  },
  grayType(v, data) {
    if (!v) return "请选择灰度类型";
    // 编辑时， 蓝绿灰度验证
    if (
      data.ruleId &&
      v === GRAYTYPE.TAG &&
      (!data.ruleTagList || data.ruleTagList.length <= 0)
    )
      return `蓝绿灰度类型的tag至少需要一条`;
  },
  ruleTagList(v, data) {
    if (data.grayType === GRAYTYPE.TAG && (!v || v.length <= 0)) {
      return `蓝绿灰度类型的tag至少需要一条`;
    }

    if (v && v.length !== 0) {
      let errIndex = null;
      for (let i in v) {
        if (!v[i].tagName || !v[i].tagOperator || !v[i].tagValue) {
          errIndex = i;
          break;
        }
      }
      if (errIndex !== null)
        return `请确认第${errIndex + 1}行发布规则，填写完整`;
    }
  },
  ruleTagRelationship(v) {
    if (!v) return "请选择规则生效关系";
  },
  relativeLane(v) {
    if (!v || Object.keys(v).length <= 0) return "请选择目标泳道";
    if (Object.values(v).reduce((a, b) => Number(a) + Number(b), 0) !== 100)
      return "权重之和等于100";
  },
});

const step2validatField = {
  [STEPS.BASE]: ["ruleName", "grayType"],
  [STEPS.PARAM]: ["ruleTagList", "ruleTagRelationship"],
  [STEPS.TARGET]: ["relativeLane"],
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
    const { types } = this;
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
      ruleId,
      ruleName,
      remark,
      enable,
      grayType,
      priority,
      createTime,
      updateTime,
      relativeLane,
      ruleTagList,
      ruleTagRelationship,
    } = form.selectors.values(yield select());
    const res = yield* resolvePromise(
      configureLaneRule({
        ruleId,
        ruleName,
        remark,
        enable: !enable ? 0 : 1,
        grayType,
        priority,
        createTime,
        updateTime,
        relativeLane,
        ruleTagList,
        ruleTagRelationship,
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
