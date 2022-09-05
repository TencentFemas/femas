import CodeMirrorBox from "@src/common/components/CodeMirrorBox";
import Action from "@src/common/duckComponents/grid/Action";
import formatDate from "@src/common/util/formatDate";
import React from "react";
import { DuckCmpProps } from "saga-duck";
import {
  Card,
  Col,
  Form,
  LoadingTip,
  Row,
  Table,
  Text,
  Tab,
  Justify,
  Switch,
  Button,
} from "tea-component";
import BaseInfoDuck from "./PageDuck";

export default function BaseInfo(props: DuckCmpProps<BaseInfoDuck>) {
  const { duck, store, dispatch } = props;
  const { selector, creators } = duck;
  const { loading, data } = selector(store);
  if (loading) return <LoadingTip />;
  if (!data) return <noscript />;

  const handlers = React.useMemo(
    () => ({
      changeEntrance: (index, val) =>
        dispatch(creators.changeEntrance({ index, val })),
      delete: (index) => dispatch(creators.delete(index)),
      edit: () => dispatch(creators.edit()),
    }),
    []
  );
  const { laneServiceList } = data;

  return (
    <Table.ActionPanel>
      <Justify
        left={
          <Button type="primary" onClick={handlers.edit}>
            添加部署组
          </Button>
        }
        style={{ margin: "10px 0" }}
      />
      <Table
        bordered={true}
        records={laneServiceList}
        recordKey="serviceName"
        columns={[
          {
            key: "serviceName",
            header: "部署组",
            render: (cvm) => (
              <>
                <p>
                  <a>{cvm.serviceName}</a>
                </p>
                <p>{cvm.namespaceName}</p>
              </>
            ),
          },
          {
            key: "group",
            header: "部署组",
            render: (cvm) => (
              <>
                <p>
                  <a>{cvm.serviceName}</a>
                </p>
                <p>{cvm.namespaceName}</p>
              </>
            ),
          },
          {
            key: "namespace",
            header: "部署组",
            render: (cvm) => <p>{cvm.namespaceName}</p>,
          },
          {
            key: "entrance",
            header: "泳道入口",
            width: 100,
            render: (cvm, rowKey, recordIndex) => {
              return (
                <Switch
                  value={cvm.entrance}
                  onChange={(val) => handlers.changeEntrance(recordIndex, val)}
                  tooltip="禁用"
                ></Switch>
              );
            },
          },
          {
            key: "action",
            header: "操作",
            render: (cvm, rowKey, recordIndex) => (
              <React.Fragment>
                <Action text="移除" fn={() => handlers.delete(recordIndex)} />
              </React.Fragment>
            ),
          },
        ]}
      />
    </Table.ActionPanel>
  );
}
