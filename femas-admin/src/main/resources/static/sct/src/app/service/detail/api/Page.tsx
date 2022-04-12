import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import React from 'react';
import { DuckCmpProps } from 'saga-duck';
import { Button, Card, Justify, SearchBox, Table } from 'tea-component';
import BaseInfoDuck from './PageDuck';
import getColumns from './getColumns';
import { filterable } from 'tea-component/lib/table/addons';
import { SELECT_ALL } from '../../types';
import { STATUS, STATUS_MAP } from './types';

export default function BaseInfo(props: DuckCmpProps<BaseInfoDuck>) {
  const { duck, store, dispatch } = props;
  const { selectors, creators } = duck;
  const columns = React.useMemo(() => getColumns(), []);
  const status = selectors.status(store);
  const serviceVersion = selectors.serviceVersion(store);
  const { versions } = selectors.composedId(store);

  const handlers = React.useMemo(
    () => ({
      reload: () => dispatch(creators.reload()),
      search: keyword => dispatch(creators.search(keyword)),
      inputKeyword: keyword => dispatch(creators.inputKeyword(keyword)),
      clearKeyword: () => dispatch(creators.search('')),
      setStatus: value => dispatch(creators.setStatus(value)),
      setVersion: value => dispatch(creators.setVersion(value)),
    }),
    [],
  );

  return (
    <>
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
              options: [STATUS.UP, STATUS.DOWN].map(value => ({
                text: STATUS_MAP[value],
                value,
              })),
            }),
            filterable({
              all: SELECT_ALL,
              type: 'single',
              column: 'serviceVersion',
              value: serviceVersion,
              onChange: value => handlers.setVersion(value),
              options: versions?.map(value => ({ text: value, value })) || [],
            }),
          ]}
        />
        <GridPagePagination duck={duck} dispatch={dispatch} store={store} />
      </Card>
    </>
  );
}
