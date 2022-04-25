import * as React from 'react';
import { Column } from '../../../../common/ducks/GridPage';
import { Text } from 'tea-component';
import { InstanceItem, ServiceStatusName, ServiceStatusTheme } from '../../types';
import formatDate from '@src/common/util/formatDate';

export default (): Column<InstanceItem>[] => [
  {
    key: 'id',
    header: '实例ID',
    render: x => (
      <Text tooltip={x.id} overflow>
        {x.id}
      </Text>
    ),
  },
  {
    key: 'status',
    header: '状态',
    render: x => (
      <Text theme={ServiceStatusTheme[x.status]} tooltip={ServiceStatusName[x.status]}>
        {ServiceStatusName[x.status] || '-'}
      </Text>
    ),
  },
  {
    key: 'serviceVersion',
    header: '服务版本',
    render: x => <Text tooltip={x.serviceVersion}>{x.serviceVersion || '-'}</Text>,
  },
  {
    key: 'ip',
    header: 'IP 地址',
    render: x => <Text tooltip={x.host}>{x.host || '-'}</Text>,
  },
  {
    key: 'port',
    header: '端口',
    render: x => <Text tooltip={x.port}>{x.port ?? '-'}</Text>,
  },
  {
    key: 'clientVersion',
    header: '客户端版本',
    render: x => <Text tooltip={x.clientVersion}>{x.clientVersion || '-'}</Text>,
  },
  {
    key: 'update',
    header: '上一次更新时间',
    render: x => <Text tooltip={formatDate(x.lastUpdateTime)}>{formatDate(x.lastUpdateTime) || '-'}</Text>,
  },
];
