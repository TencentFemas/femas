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
import TimeSelect from "@src/common/components/TimeSelect";
import moment from "moment";

export default function BaseInfo(props: DuckCmpProps<BaseInfoDuck>) {
  const { duck, store, dispatch } = props;
  const { selector, creators } = duck;

  const handlers = React.useMemo(
    () => ({
      reload: () => dispatch(creators.reload()),
    }),
    []
  );

  return (
    <Table.ActionPanel>
      <Justify
        left={
          <TimeSelect
            tabs={[
              {
                text: "近5分钟",
                date: [moment().subtract(5, "minute"), moment()],
              },
              {
                text: "近10分钟",
                date: [moment().subtract(10, "minute"), moment()],
              },
              {
                text: "近30分钟",
                date: [moment().subtract(30, "minute"), moment()],
              },
              {
                text: "近1小时",
                date: [moment().subtract(60, "minute"), moment()],
              },
            ]}
            defaultIndex={0}
            changeDate={() => {}}
            range={{
              min: moment().subtract(5, "minute"),
              max: moment(),
              maxLength: 30,
            }}
          />
        }
        right={
          <Button type="icon" icon="refresh" onClick={handlers.reload}>
            刷新
          </Button>
        }
        style={{ margin: "10px 0" }}
      />
      <Table
        bordered={true}
        records={[]}
        recordKey="serviceName"
        columns={[
          {
            key: "id",
            header: "ID/部署组名称",
            render: (cvm, rowKey, recordIndex) => <></>,
          },
          {
            key: "namespace",
            header: "命名空间",
            render: (cvm, rowKey, recordIndex) => <></>,
          },
          {
            key: "monitor",
            header: "监控",
            render: (cvm, rowKey, recordIndex) => <></>,
          },
          {
            key: "qps",
            header: "请求量",
            render: (cvm, rowKey, recordIndex) => <></>,
          },
          {
            key: "error",
            header: "请求错误率",
            render: (cvm, rowKey, recordIndex) => <></>,
          },
          {
            key: "time",
            header: "平均耗时",
            render: (cvm, rowKey, recordIndex) => <></>,
          },
        ]}
      />
    </Table.ActionPanel>
  );
}
