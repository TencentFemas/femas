import * as React from "react";
import { LaneItem } from "./types";
import { DuckCmpProps } from "saga-duck";
import LaneRulePageDuck from "./PageDuck";
import { Link } from "react-router-dom";
import { Bubble, Button, Text } from "tea-component";
import formatDate from "@src/common/util/formatDate";
import CopyableText from "@src/common/components/CopyableText";
import { Column } from "@src/common/ducks/GridPage";
import Action from "@src/common/duckComponents/grid/Action";

export default ({
  duck: { creators },
  dispatch,
}: DuckCmpProps<LaneRulePageDuck>): Column<LaneItem>[] => [
  {
    key: "id",
    header: "ID/规则名",
    render: (x) => (
      <>
        <CopyableText
          text={x.laneId}
          component={
            <Link to={`/publish/lane?id=${x.laneId}`}>{x.laneId}</Link>
          }
        />
        <Text parent="p">
          <CopyableText text={x.laneName} />
        </Text>
      </>
    ),
  },
  {
    key: "create",
    header: "创建时间",
    render: (x) => (
      <Text overflow tooltip={formatDate(x.createTime)}>
        {formatDate(x.createTime)}
      </Text>
    ),
  },
  {
    key: "update",
    header: "更新时间",
    render: (x) => (
      <Text overflow tooltip={formatDate(x.updateTime)}>
        {formatDate(x.updateTime)}
      </Text>
    ),
  },
  // {
  //   key: "tag",
  //   header: "标签",
  //   render: (x) => (
  //     <Text overflow tooltip={""}>
  //       {""}
  //     </Text>
  //   ),
  // },
  {
    key: "remark",
    header: "备注",
    render: (x) => (
      <Text overflow tooltip={x.remark}>
        {x.remark}
      </Text>
    ),
  },
  {
    key: "action",
    header: "操作",
    render: (x) => (
      <React.Fragment>
        <Action text="删除" fn={(dispatch) => dispatch(creators.delete(x))} />
      </React.Fragment>
    ),
  },
];
