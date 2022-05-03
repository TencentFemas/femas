import * as React from 'react';
import { DuckCmpProps, purify } from 'saga-duck';
import Duck from './PageDuck';
import SearchForm from './SearchForm';
import { Card } from 'tea-component';
import Request from '@src/app/apm/requestDetail/Page';

// eslint-disable-next-line prettier/prettier
export default purify(function (props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const { selectors } = duck;
  const inited = selectors.inited(store);
  const searched = selectors.searched(store);

  return (
    <>
      <Card>
        <Card.Body>{inited && <SearchForm duck={duck} store={store} dispatch={dispatch} />}</Card.Body>
      </Card>
      {searched && (
        <Card>
          <Request duck={duck} dispatch={dispatch} store={store} />
        </Card>
      )}
    </>
  );
});
