import * as React from "react";
import { LaneRuleItem, TAGOPERATORENUM_LABEL } from "./types";
import { DuckCmpProps } from "saga-duck";
import LaneRulePageDuck from "./PageDuck";
import { Link } from "react-router-dom";
import { Bubble, Button, Text, Switch, Tag } from "tea-component";
import formatDate from "@src/common/util/formatDate";
import CopyableText from "@src/common/components/CopyableText";
import { Column } from "@src/common/ducks/GridPage";
import Action from "@src/common/duckComponents/grid/Action";

export default ({
  duck: { creators },
  dispatch,
}: DuckCmpProps<LaneRulePageDuck>): Column<LaneRuleItem>[] => [
  {
    key: "priority",
    header: "优先级",
    render: (x) => (
      <>
        <Text overflow tooltip={String(x.priority)}>
          {String(x.priority)}
        </Text>
      </>
    ),
  },
  {
    key: "id",
    header: "ID/规则名",
    render: (x) => (
      <>
        <CopyableText
          text={x.ruleId}
          component={
            <Link to={`/publish/lane-rule?id=${x.ruleId}`}>{x.ruleId}</Link>
          }
        />
        <Text parent="p">
          <CopyableText text={x.ruleName} />
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
    key: "enable",
    header: "生效状态",
    render: (x) => (
      <Switch
        value={x.enable === 1}
        onChange={(val) =>
          dispatch(creators.changeEnable({ ...x, enable: val ? 1 : 0 }))
        }
      ></Switch>
    ),
  },
  {
    key: "tag",
    header: "标签",
    render: (x) => (
      <>
        {x.ruleTagList.map((v, i) => {
          return (
            <Tag key={i}>
              {v.tagName} {TAGOPERATORENUM_LABEL[v.tagOperator]} {v.tagValue}
            </Tag>
          );
        })}
      </>
    ),
  },
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
