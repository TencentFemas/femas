import FormField from "@src/common/duckComponents/form/Field";
import { nameTipMessage } from "@src/common/types";
import React from "react";
import { DuckCmpProps, purify } from "saga-duck";
import { Form, Switch, Select } from "tea-component";
import Input from "@src/common/duckComponents/form/Input";
import CreateDuck, { DialogOptions } from "../create/CreateDuck";
import { showDialog } from "@src/common/helpers/showDialog";

import { LaneRuleItem, GRAYTYPE_LABEL, GRAYTYPE } from "../../types";
import Dialog from "@src/common/duckComponents/Dialog";
import { STEPS, STEPS_LABLES } from "../create/types";

export const BaseInfo = purify(function BaseInfo(
  props: DuckCmpProps<CreateDuck>
) {
  const { duck, store, dispatch } = props;
  const {
    ducks: { form },
  } = duck;

  const formApi = form.getAPI(store, dispatch);
  const { ruleName, grayType, remark, enable } = formApi.getFields([
    "ruleName",
    "grayType",
    "remark",
    "enable",
  ]);

  return (
    <>
      <Form>
        <FormField
          field={ruleName}
          label={"规则名称"}
          message={nameTipMessage}
          required
        >
          <Input
            field={ruleName}
            maxLength={60}
            placeholder={"请输入规则名称"}
          />
        </FormField>
        <FormField field={grayType} label={"灰度类型"} required>
          <Select
            size="auto"
            value={grayType.getValue()}
            appearance="button"
            options={Object.keys(GRAYTYPE_LABEL).map((value) => ({
              value,
              text: GRAYTYPE_LABEL[value],
            }))}
            onChange={(val: GRAYTYPE) => grayType.setValue(val)}
          ></Select>
        </FormField>
        <FormField field={remark} label={"备注"}>
          <Input field={remark} multiline />
        </FormField>
        <FormField field={enable} label={"生效状态"}>
          <Switch
            value={enable.getValue() === 1}
            onChange={(val) => enable.setValue(val ? 1 : 0)}
          />
        </FormField>
      </Form>
    </>
  );
});

const StepOneForm = purify(function StepOneForm(
  props: DuckCmpProps<CreateDuck>
) {
  const { duck, store, dispatch } = props;
  const {
    selectors,
    creators,
    ducks: { form },
  } = duck;
  return (
    <Dialog
      duck={duck}
      store={store}
      dispatch={dispatch}
      size={800}
      title={STEPS_LABLES[STEPS.BASE]}
    >
      <BaseInfo duck={duck} store={store} dispatch={dispatch} />
    </Dialog>
  );
});

export function baseInfoCreate(instance: LaneRuleItem, options: DialogOptions) {
  return new Promise((resolve) => {
    showDialog(StepOneForm, CreateDuck, function*(duck: CreateDuck) {
      try {
        resolve(yield* duck.execute(instance, options));
      } finally {
        resolve(false);
      }
    });
  });
}
