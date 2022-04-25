/**
 * 与ducks/GridPage关联的Grid
 */
import * as React from 'react';
import DuckGrid, { Props as BaseProps } from './Grid';
import { DuckCmpProps, memorize, purify } from 'saga-duck';
import { TableColumn } from 'tea-component';
import Duck from '../ducks/GridPage';

const getHandlers = memorize(({ creators }: Duck, dispatch) => ({
  onClear: () => dispatch(creators.clearSearchCondition()),
  onRetry: () => dispatch(creators.reload()),
}));

interface Props extends BaseProps {
  columns: TableColumn<any>[];
}

export default purify(function DuckGridPageGrid({
  duck: {
    ducks: { grid: gridDuck },
  },
  store,
  dispatch,
  columns,
  ...rest
}: DuckCmpProps<Duck> & Props) {
  // eslint-disable-next-line prefer-rest-params
  const handlers = getHandlers(arguments[0]);
  return (
    <DuckGrid
      duck={gridDuck}
      store={store}
      dispatch={dispatch}
      columns={columns}
      onClear={handlers.onClear}
      onRetry={handlers.onRetry}
      {...rest}
    />
  );
});
