import React from "react";
import { DuckCmpProps } from "saga-duck";
import LaneDetailDuck from "./PageDuck";
import DetailPage from "@src/common/duckComponents/DetailPage";
import {
  Card,
  LoadingTip,
  Form,
  Row,
  Col,
  Tab,
  Tabs,
  TabPanel,
  Text,
  Table,
  Tag,
} from "tea-component";
import formatDate from "@src/common/util/formatDate";
import { Button } from "tea-component";
import { STEPS_LABLES, STEPS } from "../operations/create/types";
import {
  RELEATION_LABEL,
  TAGOPERATORENUM_LABEL,
  GRAYTYPE_LABEL,
  ENABLE,
  ENABLE_LABEL,
} from "../types";

export default function LaneDetailPage(props: DuckCmpProps<LaneDetailDuck>) {
  const { duck, store, dispatch } = props;
  const { selector, creators, selectors, ducks } = duck;
  const { id, data: laneInfo, loading } = selector(store);

  const handlers = React.useMemo(
    () => ({
      edit: (type) => dispatch(creators.edit(type)),
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
            title={STEPS_LABLES[STEPS.BASE]}
            operation={
              <Button type="link" onClick={() => handlers.edit(STEPS.BASE)}>
                编辑
              </Button>
            }
          >
            <Form layout="inline">
              <Row>
                <Col>
                  <Form.Item label={"名称"}>
                    <Form.Text>{laneInfo.ruleName}</Form.Text>
                  </Form.Item>
                  <Form.Item label={"灰度类型"}>
                    <Form.Text>
                      <Tag>{GRAYTYPE_LABEL[laneInfo.grayType]}</Tag>
                    </Form.Text>
                  </Form.Item>
                  <Form.Item label={"生效状态"}>
                    <Form.Text>
                      <Text
                        theme={
                          laneInfo.enable === ENABLE.OPEN ? "success" : "danger"
                        }
                      >
                        {ENABLE_LABEL[laneInfo.enable]}
                      </Text>
                    </Form.Text>
                  </Form.Item>
                  <Form.Item label={"创建时间"}>
                    <Form.Text>{formatDate(laneInfo.createTime)}</Form.Text>
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
          <Card.Body
            title={STEPS_LABLES[STEPS.PARAM]}
            operation={
              <Button type="link" onClick={() => handlers.edit(STEPS.PARAM)}>
                编辑
              </Button>
            }
          >
            <Form layout="inline">
              <Row>
                <Col>
                  <Form.Item label={"规则生效关系"}>
                    <Form.Text>
                      {RELEATION_LABEL[laneInfo.ruleTagRelationship]}
                    </Form.Text>
                  </Form.Item>
                  <Table
                    bordered
                    records={laneInfo.ruleTagList}
                    columns={[
                      {
                        key: "tagName",
                        header: "标签名",
                        render: (x, rowKey, recordIndex) => (
                          <>
                            <Text overflow tooltip={x.tagName}>
                              {x.tagName}
                            </Text>
                          </>
                        ),
                      },
                      {
                        key: "tagOperator",
                        header: "逻辑关系",
                        render: (x, rowKey, recordIndex) => (
                          <Text overflow tooltip={x.tagOperator}>
                            {TAGOPERATORENUM_LABEL[x.tagOperator]}
                          </Text>
                        ),
                      },
                      {
                        key: "tagValue",
                        header: "标签值",
                        render: (x, rowKey, recordIndex) => (
                          <>
                            <Text overflow tooltip={x.tagValue}>
                              {x.tagValue}
                            </Text>
                          </>
                        ),
                      },
                    ]}
                  ></Table>
                </Col>
              </Row>
            </Form>
          </Card.Body>
        </Card>

        <Card>
          <Card.Body
            title={STEPS_LABLES[STEPS.TARGET]}
            operation={
              <Button type="link" onClick={() => handlers.edit(STEPS.TARGET)}>
                编辑
              </Button>
            }
          >
            <Form layout="vertical">
              <Form.Item label={"灰度发布目的地"}>
                <Table
                  bordered
                  records={Object.keys(laneInfo.relativeLane).reduce((a, b) => {
                    return [...a, { key: b, value: laneInfo.relativeLane[b] }];
                  }, [])}
                  columns={[
                    {
                      key: "key",
                      header: "泳道",
                      render: (x, rowKey, recordIndex) => (
                        <>
                          <Text overflow tooltip={x.key}>
                            {x.key}
                          </Text>
                        </>
                      ),
                    },
                    {
                      key: "value",
                      header: "权重",
                      render: (x, rowKey, recordIndex) => (
                        <Text overflow tooltip={x.value}>
                          {x.value}
                        </Text>
                      ),
                    },
                  ]}
                ></Table>
              </Form.Item>
            </Form>
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
