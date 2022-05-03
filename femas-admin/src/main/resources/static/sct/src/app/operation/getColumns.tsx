import * as React from 'react';
import { Column } from '../../common/ducks/GridPage';
import { OperationItem } from './types';
import formatDate from '@src/common/util/formatDate';
import { Text } from 'tea-component';

export default (): Column<OperationItem>[] => [
  {
    key: 'time',
    header: '事件时间',
    width: 150,
    render: x => formatDate(x.time),
  },
  {
    key: 'module',
    header: '模块',
    width: 200,
    render: x => x.module,
  },
  {
    key: 'type',
    header: '操作类型',
    width: 250,
    render: x => x.type,
  },
  {
    key: 'detail',
    header: '操作类型',
    render: x => x.detail,
  },
  {
    key: 'status',
    header: '状态',
    width: 150,
    render: x => <Text theme={x.status ? 'success' : 'danger'}>{x.status ? '成功' : '失败'}</Text>,
  },
  /* {columns}*/
];
