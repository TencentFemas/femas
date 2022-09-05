import FormField from "@src/common/duckComponents/form/Field";
import { nameTipMessage } from "@src/common/types";
import React from "react";
import { DuckCmpProps, purify } from "saga-duck";
import { Form, Radio, Table, Button, Input, Select, Icon } from "tea-component";
import CreateDuck, { DialogOptions } from "../create/CreateDuck";
import { showDialog } from "@src/common/helpers/showDialog";
import {
  LaneRuleItem,
  LaneRuleTag,
  RELEATION_LABEL,
  RELEATION,
  TAGOPERATORENUM_LABEL,
  TAGOPERATORENUM,
} from "../../types";
import Dialog from "@src/common/duckComponents/Dialog";
import { STEPS, STEPS_LABLES } from "../create/types";

export const ParamInfo = purify(function ParamInfo(
  props: DuckCmpProps<CreateDuck>
) {
  const { duck, store, dispatch } = props;
  const {
    ducks: { form },
  } = duck;

  const formApi = form.getAPI(store, dispatch);
  const { ruleTagList, ruleTagRelationship } = formApi.getFields([
    "ruleTagList",
    "ruleTagRelationship",
  ]);
  const handlers = {
    add: () => {
      let ruleTagListData = ruleTagList.getValue();
      if (!ruleTagListData) ruleTagListData = [];
      ruleTagListData.push({ tagName: "", tagOperator: null, tagValue: "" });
      ruleTagList.setValue([...ruleTagListData]);
    },
    delete: (recordIndex: number) => {
      let ruleTagListData = ruleTagList.getValue();
      ruleTagListData.splice(recordIndex, 1);
      ruleTagList.setValue([...ruleTagListData]);
    },
    changeValue: (
      recordIndex: number,
      field: keyof LaneRuleTag,
      value
    ): void => {
      let ruleTagListData = ruleTagList.getValue();
      ruleTagListData[recordIndex][field] = value;
      ruleTagList.setValue([...ruleTagListData]);
    },
  };

  return (
    <Form layout={"vertical"}>
      <FormField
        field={ruleTagList}
        label={"灰度发布规则"}
        showStatusIcon={false}
      >
        <Table
          bordered
          records={ruleTagList.getValue()}
          columns={[
            {
              key: "tagName",
              header: "标签名",
              render: (x, rowKey, recordIndex) => (
                <>
                  <Input
                    value={x.tagName}
                    onChange={(val) =>
                      handlers.changeValue(recordIndex, "tagName", val)
                    }
                  ></Input>
                </>
              ),
            },
            {
              key: "tagOperator",
              header: "逻辑关系",
              render: (x, rowKey, recordIndex) => (
                <Select
                  appearance="button"
                  size="full"
                  options={Object.keys(TAGOPERATORENUM_LABEL).map((value) => ({
                    value,
                    text: TAGOPERATORENUM_LABEL[value],
                  }))}
                  value={x.tagOperator}
                  onChange={(val: TAGOPERATORENUM) =>
                    handlers.changeValue(recordIndex, "tagOperator", val)
                  }
                ></Select>
              ),
            },
            {
              key: "tagValue",
              header: "标签值",
              render: (x, rowKey, recordIndex) => (
                <>
                  <Input
                    value={x.tagValue}
                    onChange={(val) =>
                      handlers.changeValue(recordIndex, "tagValue", val)
                    }
                  ></Input>
                </>
              ),
            },
            {
              key: "action",
              header: "删除",
              render: (x, rowKey, recordIndex) => (
                <Icon
                  type="close"
                  onClick={() => handlers.delete(recordIndex)}
                ></Icon>
              ),
            },
          ]}
          bottomTip={
            <Button type="link" onClick={() => handlers.add()}>
              新增
            </Button>
          }
        ></Table>
      </FormField>
      <FormField field={ruleTagRelationship} label={"规则生效关系"}>
        <Radio.Group
          value={ruleTagRelationship.getValue()}
          onChange={(value: RELEATION) => ruleTagRelationship.setValue(value)}
        >
          {Object.keys(RELEATION_LABEL).map((key) => {
            return (
              <Radio key={key} name={key}>
                {RELEATION_LABEL[key]}
              </Radio>
            );
          })}
        </Radio.Group>
      </FormField>
    </Form>
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
      title={STEPS_LABLES[STEPS.PARAM]}
    >
      <ParamInfo duck={duck} store={store} dispatch={dispatch} />
    </Dialog>
  );
});

export function paramInfoCreate(
  instance: LaneRuleItem,
  options: DialogOptions
) {
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
