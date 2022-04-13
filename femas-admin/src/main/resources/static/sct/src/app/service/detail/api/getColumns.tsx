import * as React from 'react';
import { Column } from '../../../../common/ducks/GridPage';
import { Text } from 'tea-component';
import { InterfaceItem, STATUS_MAP, STATUS_THEME_MAP } from './types';

export default (): Column<InterfaceItem>[] => [
  {
    key: 'id',
    header: '接口名称/方法',
    render: x => (
      <>
        <p>
          <Text tooltip={x.path} overflow>
            {x.path}
          </Text>
        </p>
        <Text tooltip={x.method} overflow>
          {x.method}
        </Text>
      </>
    ),
  },
  {
    key: 'status',
    header: '状态',
    render: x => (
      <Text theme={STATUS_THEME_MAP[x.status]} tooltip={STATUS_MAP[x.status]}>
        {STATUS_MAP[x.status] || '-'}
      </Text>
    ),
  },
  {
    key: 'serviceVersion',
    header: '服务端版本',
    render: x => <Text tooltip={x.serviceVersion}>{x.serviceVersion || '-'}</Text>,
  },
];
