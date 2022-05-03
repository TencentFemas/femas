import * as React from 'react';
import { Column } from '../../common/ducks/GridPage';
import { ClusterTypeMap, RegistryItem, RegistryStatus, RegistryStatusMap } from './types';
import { DuckCmpProps } from 'saga-duck';
import RegistryPageDuck from '../registry/PageDuck';
import Action from '@src/common/duckComponents/grid/Action';
import { Link } from 'react-router-dom';
import { Text } from 'tea-component';
import CopyableText from '@src/common/components/CopyableText';

export default ({ duck: { creators } }: DuckCmpProps<RegistryPageDuck>): Column<RegistryItem>[] => [
  {
    key: 'id',
    header: '注册中心名称/ID',
    render: x => (
      <React.Fragment>
        <p>
          <CopyableText text={x.registryName} />
        </p>
        <CopyableText
          text={x.registryId}
          component={<Link to={`/registry/detail?id=${x.registryId}`}>{x.registryId}</Link>}
        />
      </React.Fragment>
    ),
  },
  {
    key: 'registryType',
    header: '类型',
    render: x => <React.Fragment>{ClusterTypeMap[x.registryType]}</React.Fragment>,
  },
  {
    key: 'status',
    header: '状态',
    render: x => (
      <Text theme={x.status === RegistryStatus.Nomarl ? 'success' : 'danger'}>{RegistryStatusMap[x.status]}</Text>
    ),
  },
  {
    key: 'action',
    header: '操作',
    render: x => (
      <React.Fragment>
        <Action text='编辑' fn={dispatch => dispatch(creators.edit(x))} />
        <Action text='删除' fn={dispatch => dispatch(creators.remove(x))} />
      </React.Fragment>
    ),
  },
  /* {columns}*/
];
