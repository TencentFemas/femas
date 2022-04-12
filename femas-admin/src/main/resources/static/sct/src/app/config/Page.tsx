import BasicLayout from '@src/common/components/BaseLayout';
import React from 'react';
import { DuckCmpProps } from 'saga-duck';
import ConfigPageDuck from './PageDuck';
import { Menu as MenuConfig } from '@src/config';
import { Button, Card, Justify, SearchBox, Table, Text } from 'tea-component';
import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import getColumns from './getColumns';
import SearchableTeaSelect from '@src/common/duckComponents/SearchableTeaSelect';

export default function ConfigPage(props: DuckCmpProps<ConfigPageDuck>) {
  const { duck, store, dispatch } = props;
  const {
    creators,
    ducks: { namespace, grid },
    selectors,
  } = duck;
  const handlers = React.useMemo(
    () => ({
      reload: () => dispatch(creators.reload()),
      search: keyword => dispatch(creators.search(keyword)),
      inputKeyword: keyword => dispatch(creators.inputKeyword(keyword)),
      clearKeyword: () => dispatch(creators.search('')),
      delete: v => dispatch(creators.delete(v)),
      add: () => dispatch(creators.add()),
    }),
    [],
  );
  const columns = React.useMemo(() => getColumns(props), []);
  const selection = grid.selectors.selection(store);
  return (
    <BasicLayout
      title={MenuConfig.config.title}
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
          left={
            <>
              <Button type='primary' onClick={() => handlers.add()}>
                新建配置
              </Button>
              <Button disabled={!selection.length} onClick={() => handlers.delete(selection)}>
                删除
              </Button>
            </>
          }
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
        <GridPageGrid duck={duck} dispatch={dispatch} store={store} columns={columns} needChecker />
        <GridPagePagination duck={duck} dispatch={dispatch} store={store} />
      </Card>
    </BasicLayout>
  );
}
