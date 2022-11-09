import React, { useState } from "react";
import { DuckCmpProps } from "saga-duck";
import { Button, Switch, Tag } from "tea-component";
import { Transfer, Table } from "tea-component";
import DeployDuck from "./DeployDuck";
import { ServiceInfo } from "./type";
import SearchableTeaSelect from "@src/common/duckComponents/SearchableTeaSelect";
import { useEffect } from "react";

const {
  selectable,
  removeable,
  scrollable,
  expandable,
  indentable,
} = Table.addons;

const SourceTable = ({
  dataSource,
  targetKeys,
  onChange,
  targetData,
  stableValue,
  onStableChange,
}) => {
  const [expandedKeys, setExpandedKeys] = useState([]);
  const [relations, setRelations] = useState({});
  type ServiceTree = {
    childs: Array<ServiceInfo>;
    serviceName: string;
    namespaceId: string;
    uniqKey: string;
    stable: string;
  };
  const [serviceTree, setServiceTree] = useState<Array<ServiceTree>>([]);
  useEffect(() => {
    const groupMap = {};
    const tmpRelations = {};
    // 树状结构
    setServiceTree(
      dataSource.reduce((a, b) => {
        const uniqKey = b.serviceName + b.namespaceId;
        tmpRelations[b.uniqKey] = uniqKey;
        if (uniqKey in groupMap) {
          a[groupMap[uniqKey]].childs.push(b);
        } else {
          groupMap[uniqKey] = a.length;
          const find = stableValue.find(
            (v) => v.serviceName + v.namespaceId === uniqKey
          );
          a.push({
            uniqKey: uniqKey,
            serviceName: b.serviceName,
            namespaceId: b.namespaceId,
            childs: [b],
            stable: find ? find.version : "",
          });
        }
        return a;
      }, [])
    );
    // 默认全部展开
    setExpandedKeys(Object.keys(groupMap));
    // 设置关联关系
    setRelations(tmpRelations);
  }, [dataSource, stableValue]);

  const renderNamespace = (record) => {
    return (
      <>
        <a>{record.serviceName}</a>
      </>
    );
  };

  const renderServer = (record) => {
    return (
      <>
        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <p style={{ display: "flex", alignItems: "center" }}>
            <a>{record.serviceName}</a>
            <span style={{ marginLeft: "6px" }}>{record.version}</span>
          </p>
        </div>
        <Tag>命名空间：{record.namespaceId}</Tag>
      </>
    );
  };

  return (
    <Table
      hideHeader={true}
      records={serviceTree}
      recordKey={"uniqKey"}
      columns={[
        {
          key: "serviceName",
          header: "ID/实例名",
          render: (record) => {
            if (record.childs) return renderNamespace(record);
            return renderServer(record);
          },
        },
      ]}
      addons={[
        scrollable({ maxHeight: 310 }),
        // 展开
        expandable({
          expandedKeys,
          expand: (record) => record.childs || null,
          onExpandedKeysChange: (keys) => setExpandedKeys(keys),
          shouldRecordExpandable: (record) => Boolean(record.childs),
        }),
        // 缩进
        indentable({ targetColumnKey: "serviceName", relations }),
        // 选择
        selectable({
          targetColumnKey: "serviceName",
          all: false,
          value: targetKeys,
          relations,
          onChange,
          render: (element, { record }) => {
            if (record.childs) return renderNamespace(record);
            // const diabled = targetData.find(
            //   (v) =>
            //     v.namespaceId === record.namespaceId &&
            //     v.version !== record.version
            // );
            // if (!diabled)
            return element;
            return (
              <label className="tea-form-check tea-form-check--table-select is-disabled">
                <input disabled type="checkbox" className="tea-checkbox" />
                <span className="tea-form-check__label">
                  <div style={{ paddingLeft: "20px" }}>
                    {renderServer(record)}
                  </div>
                </span>
              </label>
            );
          },
        }),
        removeable({
          width: 120,
          render: (element, { record }) => {
            if (record.childs)
              return (
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "flex-end",
                  }}
                >
                  稳定版本:
                  {record.stable ? (
                    <span style={{ marginLeft: "6px", color: "green" }}>
                      {record.stable}
                    </span>
                  ) : (
                    <span style={{ marginLeft: "6px" }}>未指定</span>
                  )}
                </div>
              );
            const find = stableValue.find(
              (v) =>
                v.namespaceId === record.namespaceId &&
                v.serviceName === record.serviceName &&
                v.version === record.version
            );
            return (
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "flex-end",
                }}
              >
                {find ? (
                  <Tag theme="success">stable</Tag>
                ) : (
                  <Button
                    style={{ marginLeft: "16px" }}
                    type="link"
                    title="指定为稳定版本"
                    onClick={(e) => {
                      onStableChange(record);
                      e.stopPropagation();
                    }}
                  >
                    指定
                  </Button>
                )}
              </div>
            );
          },
        }),
      ]}
    />
  );
};

const TargetTable = ({
  dataSource,
  onRemove,
  onEntranceChange,
  stableValue,
  onStableChange,
}) => {
  const getTargetColumns = (props) => [
    {
      key: "serviceName",
      header: "部署组",
      render: (cvm) => (
        <>
          <p style={{ display: "flex", alignItems: "center" }}>
            <a>{cvm.serviceName}</a>
            <span style={{ marginLeft: "6px", whiteSpace: "nowrap" }}>
              {cvm.version}
            </span>
          </p>
          <Tag>命名空间：{cvm.namespaceName}</Tag>
        </>
      ),
    },
    {
      key: "entrance",
      header: "泳道入口",
      width: 80,
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
  const columns = getTargetColumns({ onEntranceChange });

  return (
    <Table
      records={dataSource}
      recordKey={(v) => v.uniqKey}
      columns={columns}
      addons={[
        removeable({
          width: 100,
          onRemove,
          render: (element, { record }) => {
            const find = stableValue.find(
              (v) =>
                v.namespaceId === record.namespaceId &&
                v.serviceName === record.serviceName &&
                v.version === record.version
            );
            return (
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                }}
              >
                {find ? (
                  <Tag theme="success">stable</Tag>
                ) : (
                  <Button
                    type="link"
                    title="指定为稳定版本"
                    onClick={(e) => {
                      onStableChange(record);
                      e.stopPropagation();
                    }}
                  >
                    指定
                  </Button>
                )}
                {element}
              </div>
            );
          },
        }),
      ]}
    />
  );
};

interface Props {
  value?: Array<ServiceInfo>;
  stableValue?: Array<ServiceInfo>;
  onChange?(list: Array<ServiceInfo>): void;
  onStableChange?(list: Array<ServiceInfo>): void;
}
export default function Deploy(props: DuckCmpProps<DeployDuck> & Props) {
  const {
    duck,
    store,
    dispatch,
    onChange,
    onStableChange,
    value = [],
    stableValue = [],
  } = props;
  const [targetData, setTargetData] = useState<
    Array<ServiceInfo & { uniqKey: string }>
  >(
    value.map((v) => ({
      ...v,
      uniqKey: v.namespaceId + v.serviceName + v.version,
    }))
  );
  const {
    selectors,
    ducks: { namespace },
  } = duck;

  // const namespaceId = namespace.selectors.id(store);
  // const { list } = namespace.selector(store);
  const serviceList = selectors.serviceList(store).map((v) => ({
    ...v,
    uniqKey: v.namespaceId + v.serviceName + v.version,
  }));

  useEffect(() => {
    onChange(targetData);
  }, [targetData]);

  // 修改稳定版本
  const stableChangeHandle = (record) => {
    const findIndex = stableValue.findIndex(
      (v) =>
        v.serviceName === record.serviceName &&
        v.namespaceId === record.namespaceId
    );

    if (findIndex !== -1)
      return onStableChange(
        stableValue.map((v, i) => (i === findIndex ? record : v))
      );
    onStableChange([...stableValue, record]);
  };
  // target修改
  const targetChangeHandle = (keys, { recordKey, checked }) => {
    setTargetData((data) => {
      // 取消选中
      if (!checked) return data.filter((v) => v.uniqKey !== recordKey);
      // 选中
      const record = serviceList.find((v) => v.uniqKey === recordKey);
      if (!record) return data;
      return [
        ...data,
        // 默认非入口
        { entrance: false, ...record },
      ];
    });
  };

  return (
    <Transfer
      leftCell={
        <Transfer.Cell
          scrollable={false}
          title="请选择部署组"
          header={
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
          }
        >
          <SourceTable
            stableValue={stableValue}
            targetData={targetData}
            dataSource={serviceList}
            targetKeys={targetData.map((v) => v.uniqKey)}
            onChange={targetChangeHandle}
            onStableChange={stableChangeHandle}
          />
        </Transfer.Cell>
      }
      rightCell={
        <Transfer.Cell title={`已选择 (${targetData.length})`}>
          <TargetTable
            dataSource={targetData}
            onRemove={(key) => {
              setTargetData((data) => data.filter((i) => i.uniqKey !== key));
            }}
            onEntranceChange={(val, recordIndex) => {
              setTargetData((data) => {
                const tmpData = [...data];
                tmpData[recordIndex] = val;
                return tmpData;
              });
            }}
            stableValue={stableValue}
            onStableChange={stableChangeHandle}
          />
        </Transfer.Cell>
      }
    />
  );
}
