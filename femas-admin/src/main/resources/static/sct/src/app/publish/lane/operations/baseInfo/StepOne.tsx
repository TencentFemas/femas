import FormField from "@src/common/duckComponents/form/Field";
import { nameTipMessage } from "@src/common/types";
import React from "react";
import { DuckCmpProps, purify } from "saga-duck";
import { Form } from "tea-component";
import Input from "@src/common/duckComponents/form/Input";
import Duck from "../create/CreateDuck";
import { showDialog } from "@src/common/helpers/showDialog";
import CreateDuck, { DialogOptions } from "../create/CreateDuck";
import { LaneItem } from "../../types";
import Dialog from "@src/common/duckComponents/Dialog";

export const StepOne = purify(function StepOne(props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const {
    ducks: { form },
  } = duck;

  const formApi = form.getAPI(store, dispatch);
  const { laneName, remark } = formApi.getFields(["laneName", "remark"]);

  return (
    <>
      <Form>
        <FormField
          field={laneName}
          label={"泳道名称"}
          message={nameTipMessage}
          required
        >
          <Input
            field={laneName}
            maxLength={60}
            placeholder={"请输入泳道名称"}
          />
        </FormField>
        <FormField field={remark} label={"备注"}>
          <Input field={remark} multiline />
        </FormField>
      </Form>
    </>
  );
});

const StepOneForm = purify(function StepOneForm(props: DuckCmpProps<Duck>) {
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
      title={`基本信息`}
    >
      <StepOne duck={duck} store={store} dispatch={dispatch} />
    </Dialog>
  );
});

export function create(instance: LaneItem, options: DialogOptions) {
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
