import React from "react";
import { DuckCmpProps } from "saga-duck";
import LaneDetailDuck from "./PageDuck";
import DetailPage from "@src/common/duckComponents/DetailPage";
import ServiceInfo from "./serviceInfo/Page";
import MonitorInfo from "./monitorInfo/Page";
import {
  Card,
  LoadingTip,
  Form,
  Row,
  Col,
  Tab,
  Tabs,
  TabPanel,
} from "tea-component";
import formatDate from "@src/common/util/formatDate";
import { TAB, TAB_LABLES } from "./types";
import { Button } from "tea-component";

const tabs: Array<Tab> = Object.keys(TAB_LABLES).map((id) => ({
  id,
  label: TAB_LABLES[id],
}));

export default function LaneDetailPage(props: DuckCmpProps<LaneDetailDuck>) {
  const { duck, store, dispatch } = props;
  const { selector, creators, selectors, ducks } = duck;
  const { id, data: laneInfo, loading, tab } = selector(store);

  const handlers = React.useMemo(
    () => ({
      switch: (tab: Tab) => dispatch(creators.switch(tab.id)),
      edit: () => dispatch(creators.edit()),
    }),
    []
  );

  const getContent = () => {
    if (loading) return <LoadingTip />;
    if (!laneInfo) return <noscript />;

    return (
      <>
        <Card>
          <Card.Body
            title="基本信息"
            operation={
              <Button type="link" onClick={handlers.edit}>
                编辑
              </Button>
            }
          >
            <Form layout="inline">
              <Row>
                <Col>
                  <Form.Item label={"名称"}>
                    <Form.Text>{laneInfo.laneName}</Form.Text>
                  </Form.Item>
                  <Form.Item label={"创建时间"}>
                    <Form.Text>{formatDate(laneInfo.createTime)}</Form.Text>
                  </Form.Item>
                  <Form.Item label={"标签"}>
                    <Form.Text></Form.Text>
                  </Form.Item>
                  <Form.Item label={"备注"}>
                    <Form.Text>{laneInfo.remark}</Form.Text>
                  </Form.Item>
                </Col>
              </Row>
            </Form>
          </Card.Body>
        </Card>

        <Card>
          <Card.Body>
            <Tabs tabs={tabs} activeId={tab} onActive={handlers.switch}>
              <TabPanel id={TAB.GROUP}>
                <ServiceInfo
                  duck={ducks[TAB.GROUP]}
                  store={store}
                  dispatch={dispatch}
                />
              </TabPanel>
              <TabPanel id={TAB.MOMITOR}>
                <MonitorInfo
                  duck={ducks[TAB.MOMITOR]}
                  store={store}
                  dispatch={dispatch}
                />
              </TabPanel>
            </Tabs>
          </Card.Body>
        </Card>
      </>
    );
  };

  return (
    <DetailPage
      store={store}
      duck={duck}
      dispatch={dispatch}
      title={id}
      backRoute={"/publish"}
    >
      {getContent()}
    </DetailPage>
  );
}
