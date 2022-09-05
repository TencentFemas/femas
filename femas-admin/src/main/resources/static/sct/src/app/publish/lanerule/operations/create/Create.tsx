import React from "react";
import Dialog from "@src/common/duckComponents/Dialog";
import { DuckCmpProps, memorize, purify } from "saga-duck";
import Duck from "./CreateDuck";
import { Form, Stepper, Button, Row, Col, useClassNames } from "tea-component";
import { STEPS, STEPS_LABLES } from "./types";
import { BaseInfo } from "../baseInfo";
import { ParamInfo } from "../paramInfo";
import { TargetInfo } from "../targetInfo";

const steps = Object.keys(STEPS_LABLES).map((id: STEPS) => ({
  id,
  label: STEPS_LABLES[id],
}));

export default function Create(props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const {
    selectors,
    creators,
    ducks: { form },
  } = duck;
  const { Margin } = useClassNames();

  const handlers = React.useMemo(
    () => ({
      prevStep: () => dispatch(creators.prevStep()),
      nextStep: () => dispatch(creators.nextStep()),
      submit: () => dispatch(creators.submit(store)),
    }),
    []
  );
  const step = selectors.step(store);
  // const formApi = form.getAPI(store, dispatch);
  // const { laneServiceList } = formApi.getFields(["laneServiceList"]);

  return (
    <Dialog
      duck={duck}
      store={store}
      dispatch={dispatch}
      size={800}
      showFooter={false}
      title={`新建灰度发布规则`}
    >
      <Stepper
        steps={steps}
        current={step}
        className={Margin.Bottom["4n"]}
      ></Stepper>
      {step === STEPS.BASE ? (
        <>
          <BaseInfo duck={duck} store={store} dispatch={dispatch}></BaseInfo>
          <div
            className={Margin.Top["4n"]}
            style={{
              textAlign: "center",
            }}
          >
            <Button type="primary" onClick={handlers.nextStep}>
              下一步
            </Button>
          </div>
        </>
      ) : (
        ""
      )}
      {step === STEPS.PARAM ? (
        <>
          <ParamInfo duck={duck} store={store} dispatch={dispatch}></ParamInfo>
          <div
            className={Margin.Top["4n"]}
            style={{
              textAlign: "center",
            }}
          >
            <Button type="weak" onClick={handlers.prevStep}>
              上一步
            </Button>
            <Button
              type="primary"
              onClick={handlers.nextStep}
              style={{ marginLeft: "10px" }}
            >
              下一步
            </Button>
          </div>
        </>
      ) : (
        ""
      )}

      {step === STEPS.TARGET ? (
        <>
          <TargetInfo duck={duck} store={store} dispatch={dispatch} />

          <div
            className={Margin.Top["4n"]}
            style={{
              textAlign: "center",
            }}
          >
            <Button type="weak" onClick={handlers.prevStep}>
              上一步
            </Button>
            <Button
              type="primary"
              onClick={handlers.submit}
              style={{ marginLeft: "10px" }}
            >
              提交
            </Button>
          </div>
        </>
      ) : (
        ""
      )}
    </Dialog>
  );
}
