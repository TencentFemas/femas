import * as React from 'react';
import { DuckCmpProps } from 'saga-duck';
import Duck from './PageDuck';
import { Button, Switch, Text } from 'tea-component';
import formatDate from '@src/common/util/formatDate';
import { EnableStatus, FuseRuleItem, ISOLATION_MAP } from './types';
import router from '@src/common/util/router';

export default (props: DuckCmpProps<Duck>, showRuleDeatil: (rule: FuseRuleItem) => void) => {
  const { duck, dispatch } = props;
  return [
    {
      key: 'targetServiceName',
      header: '下游服务',
      render: (x: FuseRuleItem) => (
        <Text overflow tooltip={x.targetServiceName}>
          {x.targetServiceName}
        </Text>
      ),
    },
    {
      key: 'targetNamespaceId',
      header: '所属命名空间',
      render: (x: FuseRuleItem) => (
        <Text overflow tooltip={x.targetNamespaceId}>
          {x.targetNamespaceId}
        </Text>
      ),
    },
    {
      key: 'isolationLevel',
      header: '隔离级别',
      render: (x: FuseRuleItem) => (
        <Text overflow tooltip={ISOLATION_MAP[x.isolationLevel]}>
          {ISOLATION_MAP[x.isolationLevel]}
        </Text>
      ),
    },
    {
      key: 'status',
      header: '启用',
      render: (x: FuseRuleItem) => {
        return (
          <Text tooltip={x.isEnable === EnableStatus.Enable ? '已启用' : '已关闭'}>
            <Switch
              value={x.isEnable === EnableStatus.Enable}
              onChange={enable =>
                dispatch(
                  duck.creators.changeStatus({
                    ...x,
                    isEnable: enable,
                  }),
                )
              }
            />
          </Text>
        );
      },
    },
    {
      key: 'updateTime',
      header: '更新时间',
      render: (x: FuseRuleItem) => formatDate(x.updateTime),
    },
    {
      key: 'operation',
      header: '操作',
      render: (x: FuseRuleItem) => {
        return (
          <>
            <Button
              type='link'
              onClick={() => {
                showRuleDeatil(x);
              }}
            >
              查看规则
            </Button>
            <Button
              type='link'
              onClick={() => {
                router.navigate(
                  `/service/fuse-create?registryId=${x.registryId}&namespaceId=${x.namespaceId}&service=${x.serviceName}&ruleId=${x.ruleId}`,
                );
              }}
            >
              编辑
            </Button>
            <Button
              type='link'
              onClick={() => {
                dispatch(duck.creators.remove(x));
              }}
            >
              删除
            </Button>
          </>
        );
      },
    },
  ];
};
