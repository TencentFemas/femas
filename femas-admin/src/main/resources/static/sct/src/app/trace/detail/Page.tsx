import * as React from 'react';
import { DuckCmpProps } from 'saga-duck';
import Duck from './PageDuck';
import SearchForm from './SearchForm';
import { Card } from 'tea-component';
import SpanGrid from '@src/app/apm/spanGrid/Page';

export default class Page extends React.Component<DuckCmpProps<Duck>> {
  render() {
    const { duck, store, dispatch } = this.props;
    const { selectors } = duck;
    const inited = selectors.inited(store);
    const searched = selectors.searched(store);

    return (
      <>
        <Card>
          <Card.Body>{inited && <SearchForm duck={duck} store={store} dispatch={dispatch} />}</Card.Body>
        </Card>
        {searched && <SpanGrid duck={duck} store={store} dispatch={dispatch} />}
      </>
    );
  }
}
