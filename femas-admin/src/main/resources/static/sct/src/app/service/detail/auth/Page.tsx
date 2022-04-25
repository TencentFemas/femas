import * as React from 'react';
import { DuckCmpProps, purify } from 'saga-duck';
import Duck from './PageDuck';
import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import getColumns from './getColumns';
import { Button, Card, Justify, SearchBox, Table } from 'tea-component';
import { Link } from 'react-router-dom';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';

// eslint-disable-next-line prettier/prettier
export default purify(function (props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const { creators, selectors } = duck;
  const handlers = React.useMemo(
    () => ({
      reload: () => dispatch(creators.reload()),
      search: keyword => dispatch(creators.search(keyword)),
      inputKeyword: keyword => dispatch(creators.inputKeyword(keyword)),
      clearKeyword: () => dispatch(creators.search('')),
    }),
    [],
  );
  const { namespaceId, serviceName, registryId } = selectors.composedId(store);
  return (
    <>
      <Table.ActionPanel>
        <Justify
          left={
            <Link
              to={`/service/service-auth-create?namespaceId=${namespaceId}&registryId=${registryId}&serviceName=${serviceName}`}
            >
              <Button type='primary'>新建鉴权规则</Button>
            </Link>
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
        <GridPageGrid store={store} dispatch={dispatch} duck={duck} columns={getColumns(props)} />
        <GridPagePagination duck={duck} dispatch={dispatch} store={store} />
      </Card>
    </>
  );
});
