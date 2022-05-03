import SearchSelect from './SearchSelect';
import * as React from 'react';
import { SearchParams } from './TagTable';
import { purify } from 'saga-duck/purify';
import { DuckCmpProps } from 'saga-duck';
import Duck from './SelectDuck';
import { Bubble, Icon, InputAdornment, Select } from 'tea-component';

interface Props {
  params: SearchParams;
  valueList: any;
  onChange: any;
  tooltip: JSX.Element;
}

//获取 当前服务所在的命名空间
export function getValueNamespace(valueList: Array<string>) {
  if (!valueList || !valueList.length) return '';
  const item = valueList[0].split('/');
  if (item.length > 1) return item[0];
  return '';
}

// eslint-disable-next-line prettier/prettier
export default purify(function (props: DuckCmpProps<Duck> & Props) {
  const { duck, dispatch, onChange, store, valueList, params, tooltip } = props;
  const { selectors } = duck;
  const namespaceList = selectors.namespaceList(store);
  const serviceList = selectors.serviceNamespaceList(store);
  const serviceListLoading = selectors.serviceListLoading(store);
  const [namespaceId, setNamespaceId] = React.useState(getValueNamespace(valueList) || params?.namespaceId || '');

  const selectNs = namespaceId => {
    setNamespaceId(namespaceId);
    onChange(getValueListByNs(namespaceId, valueList));
    dispatch(
      duck.creators.loadServiceNamespaceList({
        namespaceId: namespaceId,
      }),
    );
  };
  const getValueListByNs = (nsId: string, valueList: Array<string>) => {
    if (!valueList || !valueList.length) return [];
    valueList = valueList.map(item => {
      if (item.indexOf('/') < 0) return `${nsId}/${item}`;
      return item;
    });
    const isCurNamespace = valueList.every(item => item.split('/').indexOf(nsId) > -1);
    if (isCurNamespace) {
      return valueList;
    }
    return [];
  };
  return namespaceList ? (
    <Bubble content={tooltip}>
      <InputAdornment
        appearence={'pure'}
        after={
          serviceListLoading ? (
            <Icon type={'loading'} />
          ) : (
            <SearchSelect {...props} style={{ width: '150px' }} options={serviceList} />
          )
        }
      >
        <Select
          style={{ width: '150px', maxWidth: '150px', minWidth: '150px' }}
          placeholder='请选择命名空间'
          searchable
          boxSizeSync
          size='m'
          type='simulate'
          appearance='button'
          options={namespaceList}
          value={namespaceId}
          onChange={value => selectNs(value)}
        />
      </InputAdornment>
    </Bubble>
  ) : (
    <Icon type={'loading'} />
  );
});
