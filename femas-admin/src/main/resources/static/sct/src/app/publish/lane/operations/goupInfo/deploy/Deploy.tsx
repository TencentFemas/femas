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
    namespaceId: string;
    stable: string;
  };
  const [serviceTree, setServiceTree] = useState<Array<ServiceTree>>([]);
  useEffect(() => {
    const groupMap = {};
    const tmpRelations = {};
    // 树状结构
    setServiceTree(
      dataSource.reduce((a, b) => {
        tmpRelations[b.uniqKey] = b.namespaceId;
        if (b.namespaceId in groupMap) {
          a[groupMap[b.namespaceId]].childs.push(b);
        } else {
          groupMap[b.namespaceId] = a.length;
          const find = stableValue.find(
            (v) =>
              v.serviceName === b.serviceName && v.namespaceId === b.namespaceId
          );
          a.push({
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

  // 指定稳定版本
  const stableClickHandle = (namespaceId, version) => {
    setServiceTree((data) => {
      const findIndex = data.findIndex((v) => v.namespaceId === namespaceId);
      if (findIndex !== -1)
        data = data.map((v, i) =>
          i === findIndex ? { ...v, stable: version } : v
        );
      // 稳定版本修改
      onStableChange(
        data.reduce((a, b) => {
          const find = b.childs.find((v) => v.version === b.stable);
          if (find) {
            const alreadyIndex = a.findIndex(
              (v) => v.namespaceId === b.namespaceId
            );
            if (alreadyIndex !== -1) {
              a = a.map((v, i) => (i === alreadyIndex ? find : v));
            } else {
              a = [...a, find];
            }
          }
          return a;
        }, stableValue)
      );
      return data;
    });
  };

  const renderNamespace = (record) => {
    return (
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <Tag>命名空间：{record.namespaceId}</Tag>
        <div>
          稳定版本:
          {record.stable ? (
            <span style={{ marginLeft: "6px", color: "green" }}>
              {record.stable}
            </span>
          ) : (
            <span style={{ marginLeft: "6px" }}>未指定</span>
          )}
        </div>
      </div>
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
          <Button
            style={{ marginLeft: "16px" }}
            type="link"
            title="指定为稳定版本"
            onClick={(e) => {
              stableClickHandle(record.namespaceId, record.version);
              e.stopPropagation();
            }}
          >
            指定
          </Button>
        </div>
      </>
    );
  };

  return (
    <Table
      hideHeader={true}
      records={serviceTree}
      recordKey={(v: ServiceTree & (ServiceInfo & { uniqKey: string })) =>
        v.childs ? v.namespaceId : v.uniqKey
      }
      columns={[
        {
          key: "namespaceId",
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
        indentable({ targetColumnKey: "namespaceId", relations }),
        // 选择
        selectable({
          targetColumnKey: "namespaceId",
          all: false,
          value: targetKeys,
          relations,
          onChange,
          render: (element, { record }) => {
            if (record.childs) return renderNamespace(record);
            const diabled = targetData.find(
              (v) =>
                v.namespaceId === record.namespaceId &&
                v.version !== record.version
            );
            if (!diabled) return element;
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
  const stableClickHandle = (record) => {
    const findIndex = stableValue.findIndex(
      (v) => v.namespaceId === record.namespaceId
    );
    if (findIndex !== -1)
      return onStableChange(
        stableValue.map((v, i) => (i === findIndex ? record : v))
      );
    onStableChange([...stableValue, record]);
  };

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
                      stableClickHandle(record);
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
            onChange={(keys, { recordKey, checked }) => {
              setTargetData((data) => {
                // 取消选中
                if (!checked)
                  return data.filter((v) => v.uniqKey !== recordKey);
                // 选中
                const record = serviceList.find((v) => v.uniqKey === recordKey);
                if (!record) return data;
                return [
                  ...data,
                  // 默认非入口
                  { entrance: false, ...record },
                ];
              });
            }}
            onStableChange={onStableChange}
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
            onStableChange={onStableChange}
          />
        </Transfer.Cell>
      }
    />
  );
}
