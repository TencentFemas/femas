import React from 'react';
import { DuckCmpProps } from 'saga-duck';
import { Menu as MenuConfig } from '../../config';
import NamespacePageDuck from './PageDuck';
import { Button, Card, Justify, Table } from 'tea-component';
import GridPageGrid from '../../common/duckComponents/GridPageGrid';
import BasicLayout from '../../common/components/BaseLayout';
import getColumns from './getColumns';
import { filterable } from 'tea-component/lib/table/addons';
import { ClusterTypeMap, RegistryStatusMap } from './types';

export default function NamespacePage(props: DuckCmpProps<NamespacePageDuck>) {
  const { duck, dispatch, store } = props;
  const columns = React.useMemo(() => getColumns(props), []);
  const { registryTypeFilter, statusFilter } = duck.selector(store);
  const handlers = React.useMemo(
    () => ({
      reload: () => dispatch(duck.creators.reload()),
      create: () => dispatch(duck.creators.create()),
      setTypeFilter: (value: string) => dispatch(duck.creators.setTypeFilter(value)),
      setStatusFilter: (value: string) => dispatch(duck.creators.setStatusFilter(value)),
    }),
    [],
  );
  return (
    <BasicLayout title={MenuConfig.registry.title} store={store} selectors={duck.selectors}>
      <Table.ActionPanel>
        <Justify
          left={
            <React.Fragment>
              <Button type='primary' onClick={handlers.create}>
                配置注册中心
              </Button>
            </React.Fragment>
          }
          right={
            <React.Fragment>
              {/*{rightActions}*/}
              <Button type='icon' icon='refresh' onClick={handlers.reload}>
                刷新
              </Button>
            </React.Fragment>
          }
        />
      </Table.ActionPanel>
      <Card>
        <GridPageGrid
          duck={duck}
          dispatch={dispatch}
          store={store}
          columns={columns}
          addons={[
            filterable({
              column: 'registryType',
              type: 'single',
              value: registryTypeFilter,
              all: {
                text: '全部',
                value: '',
              },
              options: Object.keys(ClusterTypeMap).map(key => ({
                value: key,
                text: ClusterTypeMap[key],
              })),
              onChange: handlers.setTypeFilter,
            }),
            filterable({
              column: 'status',
              type: 'single',
              value: statusFilter,
              all: {
                text: '全部',
                value: '',
              },
              options: Object.keys(RegistryStatusMap).map(key => ({
                value: key,
                text: RegistryStatusMap[key],
              })),
              onChange: handlers.setStatusFilter,
            }),
          ]}
        />
      </Card>
    </BasicLayout>
  );
}
