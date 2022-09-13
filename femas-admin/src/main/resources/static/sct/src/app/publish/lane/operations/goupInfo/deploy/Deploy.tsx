import React, { useState } from "react";
import { DuckCmpProps } from "saga-duck";
import { Switch, Tag } from "tea-component";
import {
  Transfer,
  Table,
  SearchBox,
  Select,
  TagSearchBox,
} from "tea-component";
import DeployDuck from "./DeployDuck";
import { ServiceInfo } from "./type";
import SearchableTeaSelect from "@src/common/duckComponents/SearchableTeaSelect";
import { useEffect } from "react";

const { selectable, removeable, scrollable } = Table.addons;

const sourceColumns = [
  {
    key: "serviceName",
    header: "ID/实例名",
    render: (cvm) => (
      <>
        <p style={{ display: "flex", alignItems: "center" }}>
          <a>{cvm.serviceName}</a>
          <span style={{ marginLeft: "6px" }}>{cvm.version}</span>
        </p>
        <Tag>命名空间：{cvm.namespaceName}</Tag>
      </>
    ),
  },
];

const getTargetColumns = (props) => [
  {
    key: "serviceName",
    header: "部署组",
    render: (cvm) => (
      <>
        <p style={{ display: "flex", alignItems: "center" }}>
          <a>{cvm.serviceName}</a>
          <span style={{ marginLeft: "6px" }}>{cvm.version}</span>
        </p>
        <Tag>命名空间：{cvm.namespaceName}</Tag>
      </>
    ),
  },
  {
    key: "entrance",
    header: "泳道入口",
    width: 100,
    render: (cvm, rowKey, recordIndex) => {
      return (
        <Switch
          value={cvm.entrance}
          onChange={(entrance) => {
            props.onEntranceChange({ ...cvm, entrance }, recordIndex);
          }}
          tooltip="禁用"
        ></Switch>
      );
    },
  },
];

function SourceTable({ dataSource, targetKeys, onChange }) {
  return (
    <Table
      hideHeader={true}
      records={dataSource}
      recordKey={(v) => v.serviceName + v.version}
      columns={sourceColumns}
      addons={[
        scrollable({
          maxHeight: 310,
          onScrollBottom: () => console.log("到达底部"),
        }),
        selectable({
          value: targetKeys,
          onChange,
          rowSelect: true,
        }),
      ]}
    />
  );
}

function TargetTable({ dataSource, onRemove, onEntranceChange }) {
  const columns = React.useMemo(
    () => getTargetColumns({ onEntranceChange }),
    []
  );
  return (
    <Table
      records={dataSource}
      recordKey={(v) => v.serviceName + v.version}
      columns={columns}
      addons={[removeable({ onRemove })]}
    />
  );
}

interface Props {
  value?: Array<ServiceInfo>;
  onChange?(list: Array<ServiceInfo>): void;
}
export default function Deploy(props: DuckCmpProps<DeployDuck> & Props) {
  const { duck, store, dispatch, onChange, value = [] } = props;
  const [targetData, setTargetData] = useState<Array<ServiceInfo>>(value);
  const {
    selectors,
    ducks: { namespace },
  } = duck;

  // const namespaceId = namespace.selectors.id(store);
  // const { list } = namespace.selector(store);
  const serviceList = selectors.serviceList(store);
  useEffect(() => {
    onChange(targetData);
  }, [targetData]);

  return (
    <Transfer
      leftCell={
        <Transfer.Cell
          scrollable={false}
          title="请选择部署组"
          tip="支持按住 shift 键进行多选"
          header={
            <>
              <SearchableTeaSelect
                size="full"
                duck={namespace}
                dispatch={dispatch}
                store={store}
                searchable={false}
                toOption={(o) => {
                  return {
                    text: o.name,
                    tooltip: o.name,
                    value: o.namespaceId,
                  };
                }}
                boxSizeSync
              />
            </>
          }
        >
          <SourceTable
            dataSource={serviceList}
            targetKeys={targetData.map((v) => v.serviceName + v.version)}
            onChange={(keys, { selectedRecords }) => {
              setTargetData((data) => {
                const alredyData = selectedRecords.filter((v) => {
                  return !data.find(
                    (j) =>
                      j.serviceName + j.version === v.serviceName + v.version
                  );
                });
                return [...data, ...alredyData];
              });
            }}
          />
        </Transfer.Cell>
      }
      rightCell={
        <Transfer.Cell title={`已选择 (${targetData.length})`}>
          <TargetTable
            dataSource={targetData}
            onRemove={(key) => {
              console.log(key);
              setTargetData((data) =>
                data.filter((i) => i.serviceName + i.version !== key)
              );
            }}
            onEntranceChange={(val, recordIndex) => {
              setTargetData((data) => {
                const tmpData = [...data];
                tmpData[recordIndex] = val;
                return tmpData;
              });
            }}
          />
        </Transfer.Cell>
      }
    />
  );
}
