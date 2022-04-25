import React from 'react';
import { DuckCmpProps, purify } from 'saga-duck';
import { Menu as MenuConfig } from '../../config';
import NamespacePageDuck from './PageDuck';
import { Button, Card, Justify, SearchBox, Table } from 'tea-component';
import BasicLayout from '../../common/components/BaseLayout';
import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import { filterable } from 'tea-component/lib/table/addons';
import getColumns from './getColumns';

export default purify(function NamespacePage(props: DuckCmpProps<NamespacePageDuck>) {
  const { duck, dispatch, store } = props;
  const { creators, selectors } = duck;

  const handlers = React.useMemo(
    () => ({
      search: keyword => dispatch(creators.search(keyword)),
      inputKeyword: keyword => dispatch(creators.inputKeyword(keyword)),
      clearKeyword: () => dispatch(creators.search('')),
      create: () => dispatch(creators.create()),
      reload: () => dispatch(creators.reload()),
      setRegistryId: v => dispatch(creators.setRegistryId(v)),
    }),
    [],
  );
  const columns = getColumns(props);
  const registryId = selectors.registryId(store);
  const registryList = selectors.registryList(store);

  return (
    <BasicLayout title={MenuConfig.namespace.title} store={store} selectors={duck.selectors}>
      <Table.ActionPanel>
        <Justify
          left={
            <React.Fragment>
              <Button type='primary' onClick={handlers.create}>
                新建命名空间
              </Button>
            </React.Fragment>
          }
          right={
            <React.Fragment>
              <SearchBox
                size='m'
                value={selectors.pendingKeyword(store)}
                placeholder='请输入关键字'
                onSearch={handlers.search}
                onChange={handlers.inputKeyword}
                onClear={handlers.clearKeyword}
              />
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
              all: {
                text: '所有',
                value: '',
              },
              type: 'single',
              column: 'registry',
              value: registryId,
              onChange: value => handlers.setRegistryId(value),
              options: registryList,
            }),
          ]}
        />
        <GridPagePagination duck={duck} dispatch={dispatch} store={store} />
      </Card>
    </BasicLayout>
  );
});
