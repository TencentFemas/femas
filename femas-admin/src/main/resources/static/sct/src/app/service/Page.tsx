import BasicLayout from '@src/common/components/BaseLayout';
import React from 'react';
import { DuckCmpProps, purify } from 'saga-duck';
import ServicePageDuck from './PageDuck';
import { Menu as MenuConfig } from '@src/config';
import { Button, Card, Justify, SearchBox, Table, Text } from 'tea-component';
import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import getColumns from './getColumns';
import SearchableTeaSelect from '@src/common/duckComponents/SearchableTeaSelect';
import { filterable } from 'tea-component/lib/table/addons';
import { SELECT_ALL, ServiceStatus, ServiceStatusName } from './types';

export default purify(function ServicePage(props: DuckCmpProps<ServicePageDuck>) {
  const { duck, store, dispatch } = props;
  const {
    creators,
    ducks: { namespace },
    selectors,
  } = duck;
  const status = selectors.status(store);
  const handlers = React.useMemo(
    () => ({
      reload: () => dispatch(creators.reload()),
      search: keyword => dispatch(creators.search(keyword)),
      inputKeyword: keyword => dispatch(creators.inputKeyword(keyword)),
      clearKeyword: () => dispatch(creators.search('')),
      setStatus: value => dispatch(creators.setStatus(value)),
    }),
    [],
  );
  const columns = getColumns(props);
  return (
    <BasicLayout
      title={MenuConfig.service.title}
      store={store}
      selectors={duck.selectors}
      header={
        <>
          <Text reset verticalAlign='middle' theme='label'>
            命名空间：
          </Text>
          <SearchableTeaSelect
            duck={namespace}
            dispatch={dispatch}
            store={store}
            searchable={false}
            toOption={o => {
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
      <Table.ActionPanel>
        <Justify
          right={
            <>
              <SearchBox
                value={selectors.pendingKeyword(store)}
                placeholder='请输入名称搜索'
                onSearch={handlers.search}
                onChange={handlers.inputKeyword}
                onClear={handlers.clearKeyword}
              />
              <Button type='icon' icon='refresh' onClick={handlers.reload}>
                刷新
              </Button>
            </>
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
              all: SELECT_ALL,
              type: 'single',
              column: 'status',
              value: status,
              onChange: value => handlers.setStatus(value),
              options: [ServiceStatus.UP, ServiceStatus.DOWN].map(value => ({
                text: ServiceStatusName[value],
                value,
              })),
            }),
          ]}
        />
        <GridPagePagination duck={duck} dispatch={dispatch} store={store} />
      </Card>
    </BasicLayout>
  );
});
