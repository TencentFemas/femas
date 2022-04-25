import * as React from 'react';
import { Select, SelectOptionWithGroup, SimulateSelectProps, StatusTip } from 'tea-component';
import { DuckCmpProps } from 'saga-duck';
import Duck from '../ducks/SearchableSelect';
import { handleStatus } from './SearchableSelect';

interface MyDuck<TItem> extends Duck {
  ID: string;
  Item: TItem;
}

export interface SearchableTeaSelectProps<TItem>
  extends DuckCmpProps<MyDuck<TItem>>,
    Omit<
      SimulateSelectProps,
      | 'options'
      | 'type'
      | 'value'
      | 'onChange'
      | 'searchValue'
      | 'onSearchValueChange'
      | 'onSearch'
      | 'onScrollBottom'
      | 'tips'
      | 'bottomTips'
    > {
  toOption: (item: TItem) => SelectOptionWithGroup;
}

export default function SearchableTeaSelect<TItem>(props: SearchableTeaSelectProps<TItem>) {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { duck, store, dispatch, toOption, searchable = true, ...rest } = props;
  const searchableProps = useSearchableTeaSelect(props);
  return <Select size='m' appearance='button' type='simulate' searchable={searchable} {...searchableProps} {...rest} />;
}

/** 为了配合Table Filterable使用，将props逻辑抽离出来 */
export function useSearchableTeaSelect<TItem>(props: SearchableTeaSelectProps<TItem>) {
  const { duck, store, dispatch, toOption } = props;
  const { selector, creators } = duck;
  const { id, pendingKeyword, list, loadingMore, nomore } = selector(store);
  const handlers = React.useMemo(() => {
    return {
      select: (id: string) => dispatch(creators.select(id)),
      inputKeyword: (keyword: string) => dispatch(creators.inputKeyword(keyword)),
      clearKeyword: () => dispatch(creators.search('')),
      search: keyword => dispatch(creators.search(keyword)),
      more: () => dispatch(creators.more()),
      reload: () => dispatch(creators.reload()),
    };
  }, [duck, dispatch]);

  // 如果是加载更多的模式，loading应该展示在列表下方
  const isLoadMoreMode = loadingMore;
  const { tipProps: statusTipProps } = handleStatus(props);

  return {
    value: id,
    options: list?.map(toOption) || [],
    onChange: handlers.select,
    searchValue: pendingKeyword,
    onSearchValueChange: handlers.inputKeyword,
    onSearch: handlers.search,
    onScrollBottom: nomore ? null : handlers.more,
    filter: () => true,
    autoClearSearchValue: false,
    tips: !isLoadMoreMode && statusTipProps ? <StatusTip {...statusTipProps}></StatusTip> : null,
    bottomTips: isLoadMoreMode && statusTipProps ? <StatusTip {...statusTipProps}></StatusTip> : null,
  };
}

SearchableTeaSelect['defaultLabelAlign'] = 'middle';
