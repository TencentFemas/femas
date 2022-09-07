import FormField from "@src/common/duckComponents/form/Field";
import React, { useState } from "react";
import { DuckCmpProps, purify } from "saga-duck";
import {
  Button,
  Form,
  Select,
  Table,
  Icon,
  Input,
  LoadingTip,
} from "tea-component";
import CreateDuck, { DialogOptions } from "../create/CreateDuck";
import { showDialog } from "@src/common/helpers/showDialog";
import { LaneRuleItem } from "../../types";
import Dialog from "@src/common/duckComponents/Dialog";
import router from "@src/common/util/router";
import { useEffect } from "react";
import { fetchAllLaneList } from "../../model";
import { STEPS, STEPS_LABLES } from "../create/types";
import { TAB } from "../../../types";

export const TargetInfo = purify(function TargetInfo(
  props: DuckCmpProps<CreateDuck>
) {
  const { duck, store, dispatch } = props;
  const {
    ducks: { form },
    creators,
  } = duck;

  const formApi = form.getAPI(store, dispatch);
  const { relativeLane } = formApi.getFields(["relativeLane"]);

  type relativeLaneItem = { key: string; value: string };
  const handlers = {
    getValue: () => {
      let relativeLaneData = relativeLane.getValue();
      if (!relativeLaneData) relativeLaneData = {};
      return Object.keys(relativeLaneData).reduce((a, b) => {
        return [...a, { key: b, value: relativeLaneData[b] }];
      }, []);
    },
    setValue: (relativeLaneData) => {
      relativeLane.setValue(
        relativeLaneData.reduce((a, b) => {
          return { ...a, [b.key]: b.value };
        }, {})
      );
    },
    add: () => {
      let data = handlers.getValue();
      if (!data) data = [];
      data.push({ key: "", value: "" });
      handlers.setValue(data);
    },
    delete: (recordIndex: number) => {
      let data = handlers.getValue();
      data.splice(recordIndex, 1);
      handlers.setValue(data);
    },
    changeValue: (
      recordIndex: number,
      field: keyof relativeLaneItem,
      value: relativeLaneItem[keyof relativeLaneItem]
    ): void => {
      let data = handlers.getValue();
      data[recordIndex][field] = value;
      handlers.setValue(data);
    },
  };

  const [laneOptions, setLaneOptions] = useState([]);
  useEffect(() => {
    fetchAllLaneList({}).then((res) => {
      setLaneOptions(
        res.list.map(({ laneId, laneName }) => ({
          text: laneName,
          value: laneId,
        }))
      );
    });
  }, []);

  return (
    <>
      <Form>
        <FormField
          field={relativeLane}
          label={"目的地泳道"}
          message={
            <p>
              如无合适泳道？ 现在
              <Button
                type="link"
                onClick={() => {
                  dispatch(creators.hide());
                  router.navigate(`/publish?tab=${TAB.LANE}`);
                }}
              >
                新建泳道
              </Button>
            </p>
          }
          required
          showStatusIcon={false}
        >
          <Table
            bordered
            records={handlers.getValue()}
            columns={[
              {
                key: "key",
                header: "泳道",
                render: (x, rowKey, recordIndex) => (
                  <Select
                    size="full"
                    value={x.key}
                    appearance="button"
                    options={laneOptions.map((v) => {
                      v.disabled = false;
                      if (
                        handlers
                          .getValue()
                          .map((j) => j.key)
                          .includes(v.value)
                      )
                        v.disabled = true;
                      return v;
                    })}
                    onChange={(val) =>
                      handlers.changeValue(recordIndex, "key", val)
                    }
                  ></Select>
                ),
              },
              {
                key: "value",
                header: "权重",
                render: (x, rowKey, recordIndex) => (
                  <Input
                    value={x.value}
                    onChange={(val) =>
                      handlers.changeValue(recordIndex, "value", val)
                    }
                  ></Input>
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
              <Button type="link" onClick={handlers.add}>
                新增
              </Button>
            }
          ></Table>
        </FormField>
      </Form>
    </>
  );
});

const TargetInfoForm = purify(function TargetInfoForm(
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
      title={STEPS_LABLES[STEPS.TARGET]}
    >
      <TargetInfo duck={duck} store={store} dispatch={dispatch} />
    </Dialog>
  );
});

export function targetInfoCreate(
  instance: LaneRuleItem,
  options: DialogOptions
) {
  return new Promise((resolve) => {
    showDialog(TargetInfoForm, CreateDuck, function*(duck: CreateDuck) {
      try {
        resolve(yield* duck.execute(instance, options));
      } finally {
        resolve(false);
      }
    });
  });
}
