import * as React from 'react';
import { DuckCmpProps } from 'saga-duck';
import Duck from './PageDuck';
import { Card } from 'tea-component';
import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import getColumns from './getColumns';

export interface Props extends DuckCmpProps<Duck> {
  hideApiHref?: boolean;
  bordered?: boolean;
}

export default class Page extends React.Component<Props> {
  render() {
    const { duck, store, dispatch, bordered } = this.props;
    return (
      <Card bordered={bordered}>
        <GridPageGrid duck={duck} dispatch={dispatch} store={store} columns={getColumns(this.props)} />
        <GridPagePagination style={{ padding: 10 }} duck={duck} store={store} dispatch={dispatch} />
      </Card>
    );
  }
}
