import SearchSelect from './SearchSelect';
import * as React from 'react';
import { SearchParams } from './TagTable';
import { purify } from 'saga-duck/purify';
import { DuckCmpProps } from 'saga-duck';
import Duck from './SelectDuck';
import { Bubble, Icon, Text } from 'tea-component';

interface Props {
  params: SearchParams;
  tooltip: JSX.Element;
}

// eslint-disable-next-line prettier/prettier
export default purify(function (props: DuckCmpProps<Duck> & Props) {
  const { duck, store, tooltip } = props;
  const { selectors } = duck;
  const list = selectors.serviceList(store);
  if (!list) return <noscript />;

  const loading = selectors.serviceListLoading(store);
  return !loading ? (
    <Bubble content={tooltip}>
      <Text parent='div'>
        <SearchSelect options={list} {...props} />
      </Text>
    </Bubble>
  ) : (
    <Icon type={'loading'} />
  );
});
