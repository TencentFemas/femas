import SearchableTableSelect from '@src/common/duckComponents/SearchableTableSelect';
import React from 'react';
import { useDuck } from 'saga-duck';
import { Text } from 'tea-component';
import { ApiItem } from '../types';
import ApiTableSelectDuck from './ApiTableSelectDuck';

interface Props {
  namespaceId: string;
  serviceName: string;
  value: Array<ApiItem>;
  onChange: (value: Array<ApiItem>) => void;
}

export default function ApiTableSelect(props: Props) {
  const { duck, store, dispatch } = useDuck(ApiTableSelectDuck);
  const { namespaceId, serviceName, value, onChange } = props;
  React.useEffect(() => {
    dispatch(duck.creators.load({ namespaceId, serviceName }));
    dispatch(duck.creators.selectIds(value?.map(item => item.id) || []));
  }, [namespaceId, serviceName]);
  return (
    <SearchableTableSelect
      searchable={false}
      duck={duck}
      store={store}
      dispatch={dispatch}
      selectAllText-='所有 API'
      itemRenderer={item => <Text>{item.id}</Text>}
      multiple={true}
      tableWidth='400px'
      inputWidth={560}
      scrollableOptions={{
        maxHeight: '200px',
        minHeight: '200px',
      }}
      size='full'
      columns={[
        {
          key: 'value',
          header: '所有API',
          render: item => item.id,
        },
      ]}
      onSelect={(value: string[]) => {
        const selectSet = new Set(value);
        const selected = duck.selectors.list(store).list?.filter(item => selectSet.has(item.id));

        onChange(selected);
      }}
    ></SearchableTableSelect>
  );
}
