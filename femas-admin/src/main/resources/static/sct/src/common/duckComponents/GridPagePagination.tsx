/**
 * GridPage中的Pagination
 */
import * as React from 'react';
import { Pagination, PaginationProps } from 'tea-component';
import { DuckCmpProps, memorize, purify } from 'saga-duck';

import Duck from '../ducks/GridPage';

const getHandler = memorize((duck, dispatch) => {
  return ({ pageIndex, pageSize }) => {
    dispatch(duck.creators.paginate(pageIndex, pageSize));
  };
});

interface Props extends Omit<PaginationProps, 'pageSize' | 'onPagingChange' | 'pageIndex' | 'recordCount'> {
  minPageSize?: number;
  maxPageSize?: number;
  pageSizeInterval?: number;
}

export default purify(function DuckGridPagePagination(props: DuckCmpProps<Duck> & Props) {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { store, dispatch, duck, minPageSize, maxPageSize, pageSizeInterval, ...rest } = props;
  const {
    selectors,
    ducks: { grid },
  } = duck;

  const onPageChange = getHandler(props);

  return (
    <Pagination
      pageSize={selectors.count(store)}
      onPagingChange={onPageChange}
      pageIndex={selectors.page(store)}
      recordCount={grid.selectors.totalCount(store)}
      pageSizeOptions={getCountList(
        minPageSize || duck.minPageSize,
        maxPageSize || duck.maxPageSize,
        pageSizeInterval || duck.pageSizeInterval,
      )}
      {...rest}
    />
  );
});

function getCountList(minPageSize = 5, maxPageSize = 50, pageSizeInterval = 5) {
  const listArr = [];
  for (let pageSize = minPageSize; pageSize <= maxPageSize; pageSize += pageSizeInterval) {
    listArr.push(pageSize);
  }
  return listArr;
}
