import React, { useEffect } from 'react';
import SearchableTeaSelect from '@src/common/duckComponents/SearchableTeaSelect';
import { useDuck } from 'saga-duck';
import ApiSelectDuck from './ApiSelectDuck';
import useControlled from '@src/common/hooks/useControlled';

interface Props {
  namespaceId: string;
  serviceName: string;
  value: string;
  onChange: (value: string) => void;
}

export default function ApiSelect(props: Props) {
  const { duck, store, dispatch } = useDuck(ApiSelectDuck);
  const { namespaceId, serviceName, value, onChange } = props;
  useControlled(value, onChange, duck.selectors.id(store), v => dispatch(duck.creators.select(v)));
  // eslint-disable-next-line prettier/prettier
  useEffect(function () {
    dispatch(
      duck.creators.load({
        namespaceId,
        serviceName,
      }),
    );
  }, []);
  return (
    <SearchableTeaSelect
      duck={duck}
      dispatch={dispatch}
      store={store}
      searchable={false}
      toOption={o => {
        return {
          text: `${o.path}(${o.method})`,
          tooltip: `${o.path}(${o.method})`,
          value: o.id,
        };
      }}
      boxSizeSync
    />
  );
}
