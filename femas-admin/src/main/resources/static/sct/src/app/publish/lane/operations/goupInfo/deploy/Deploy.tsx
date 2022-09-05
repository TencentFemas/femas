import React, { useState } from "react";
import { DuckCmpProps } from "saga-duck";
import { Switch } from "tea-component";
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
        <p>
          <a>{cvm.serviceName}</a>
        </p>
        <p>{cvm.namespaceName}</p>
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
        <p>
          <a>{cvm.serviceName}</a>
        </p>
        <p>{cvm.namespaceName}</p>
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
      recordKey="serviceName"
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
      recordKey="serviceName"
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
            targetKeys={targetData.map((v) => v.serviceName)}
            onChange={(keys, { selectedRecords }) => {
              setTargetData(selectedRecords);
            }}
          />
        </Transfer.Cell>
      }
      rightCell={
        <Transfer.Cell title={`已选择 (${targetData.length})`}>
          <TargetTable
            dataSource={targetData}
            onRemove={(key) => {
              setTargetData(targetData.filter((i) => i.serviceName !== key));
            }}
            onEntranceChange={(val, recordIndex) => {
              const tmpData = [...targetData];
              tmpData[recordIndex] = val;
              setTargetData(tmpData);
            }}
          />
        </Transfer.Cell>
      }
    />
  );
}
