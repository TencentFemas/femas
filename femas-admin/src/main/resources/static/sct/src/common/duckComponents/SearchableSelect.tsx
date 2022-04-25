import * as React from 'react';
import { DuckCmpProps, memorize, purify } from 'saga-duck';
import Duck from '../ducks/SearchableSelect';
import {
  Dropdown,
  DropdownProps,
  List,
  ListItemProps,
  ListProps,
  SearchBox,
  SearchBoxProps,
  StatusTip,
  StatusTipProps,
} from 'tea-component';

interface MyDuck<T> extends Duck {
  Item: T;
}

export interface Props<T = any>
  extends DuckCmpProps<MyDuck<T>>,
    Omit<DropdownProps, 'button' | 'children' | 'clickClose'> {
  placeholder?: React.ReactChild;
  searchPlaceholder?: string;
  showEmptyItem?: boolean;
  emptyItem?: T;
  onChange?: Function;
  searchable?: boolean;

  /** 判断选项是否被禁用 */
  itemDisabled?(item: T): boolean;

  /** 选项如何被渲染，如果不指定，则直接展示id（duck.getId） */
  itemRenderer?(item: T): React.ReactChild;

  /** 选项展示什么tip */
  itemTip?(item: T): React.ReactChild;
}

const getHandlers = memorize(({ creators }: Duck, dispatch) => ({
  inputKeyword: keyword => dispatch(creators.inputKeyword(keyword)),
  search: keyword => dispatch(creators.search(keyword)),
  clearKeyword: () => dispatch(creators.search('')),
  select: selected => dispatch(creators.select(selected)),
  more: () => dispatch(creators.more()),
  reload: () => dispatch(creators.reload()),
}));

const SearchalbeSelect = purify(function Cmp<T>(props: Props<T>) {
  const handlers = getHandlers(props);
  const {
    duck,
    store,
    dispatch,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    itemDisabled = o => false,
    itemRenderer = o => duck.getId(o),
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    itemTip = o => undefined,
    placeholder = '请选择...',
    searchPlaceholder = '输入关键字搜索',
    size = 'm',
    showEmptyItem,
    emptyItem,
    onChange,
    searchable,
    ...rest
  } = props;
  const { selector, selectors } = duck;
  const { id: selectedId, totalCount } = selector(store);

  const selected = selectors.selected(store);
  const selectedItem = selected ? itemRenderer(selected) : totalCount === 0 && !showEmptyItem ? '暂无数据' : null;

  return (
    <Dropdown
      appearence='button'
      button={selectedItem || selectedId || placeholder}
      clickClose={false}
      size={size}
      {...rest}
    >
      {close => (
        <div>
          {searchable && (
            <SearchableSearchBox duck={duck} store={store} dispatch={dispatch} placeholder={searchPlaceholder} />
          )}
          <SearchableList
            duck={duck}
            store={store}
            dispatch={dispatch}
            type='option'
            showEmptyItem={showEmptyItem}
            emptyItem={emptyItem}
            getListItemContent={itemRenderer}
            getListItemProps={o => {
              const id = duck.getId(o);
              return {
                current: id === selectedId,
                disabled: itemDisabled(o),
                tooltip: itemTip(o),
                onClick: () => {
                  handlers.select(id);
                  onChange && onChange(id);
                  close();
                },
              };
            }}
          />
        </div>
      )}
    </Dropdown>
  );
});

SearchalbeSelect['defaultLabelAlign'] = 'middle';
export default SearchalbeSelect;

// 子组件，可以自由组装成其它形式
export interface SearchableSearchBoxProps
  extends DuckCmpProps<MyDuck<any>>,
    Omit<SearchBoxProps, 'value' | 'onChange' | 'onSearch' | 'onClear'> {}

/**
 * 搜索框
 */
export function SearchableSearchBox(props: SearchableSearchBoxProps) {
  const handlers = getHandlers(props);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { duck, store, dispatch, placeholder = '输入关键字搜索', ...rest } = props;
  const { selector } = duck;
  const { pendingKeyword } = selector(store);

  return (
    <SearchBox
      value={pendingKeyword}
      onChange={handlers.inputKeyword}
      onSearch={handlers.search}
      onClear={handlers.clearKeyword}
      placeholder={placeholder}
      {...rest}
    />
  );
}

export interface SearchableListProps<T> extends DuckCmpProps<MyDuck<T>>, Omit<ListProps, 'onScrollBottom'> {
  addons?: Addon[];
  showEmptyItem?: boolean;
  emptyItem?: T;

  getListItemProps?(item: T): Partial<ListItemProps>;

  getListItemContent(item: T): React.ReactChild;
}

interface Addon {
  onInjectTip?: Middleware<(opts: SearchableListStatusTipProps) => React.ReactNode>;
}

/**
 * 列表
 */
export function SearchableList<T>(props: SearchableListProps<T>) {
  const handlers = getHandlers(props);
  const {
    duck,
    store,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    dispatch,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    getListItemProps = o => {},
    getListItemContent,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    addons,
    showEmptyItem,
    emptyItem,
    ...rest
  } = props;
  const { selector } = duck;
  const { id: selectedId, list, loadingMore, nomore } = selector(store);

  // 如果是加载更多的模式，loading应该展示在列表下方
  const isLoadMoreMode = loadingMore;
  const { tipProps, emptyStatus } = handleStatus(props);
  const statusTip = <StatusTip {...tipProps} />;
  const showEmpty = emptyStatus && showEmptyItem && emptyItem;

  return (
    <List onScrollBottom={nomore ? undefined : handlers.more} {...rest}>
      {!showEmpty && !isLoadMoreMode && tipProps && <List.StatusTip>{statusTip}</List.StatusTip>}
      {showEmpty && (
        <List.Item
          key={duck.getId(emptyItem)}
          current={duck.getId(emptyItem) === selectedId}
          {...getListItemProps(emptyItem)}
        >
          {getListItemContent(emptyItem)}
        </List.Item>
      )}
      {(list || []).map(o => {
        const id = duck.getId(o);
        const props = getListItemProps(o);
        return (
          <List.Item
            key={id}
            current={id === selectedId}
            onClick={() => {
              handlers.select(id);
            }}
            {...props}
          >
            {getListItemContent(o)}
          </List.Item>
        );
      })}
      {isLoadMoreMode && statusTip}
    </List>
  );
}

/**
 * 状态Tip
 */
export interface SearchableListStatusTipProps
  extends DuckCmpProps<MyDuck<any>>,
    Omit<StatusTipProps, 'status' | 'onRetry' | 'onClear'> {
  wrap?: (content: React.ReactChild) => React.ReactElement;
}

export const handleStatus = (props: SearchableListStatusTipProps) => {
  const handlers = getHandlers(props);
  const { duck, store } = props;
  const { selector } = duck;
  const {
    keyword,
    list,
    totalCount,
    fetcher: { loading, error },
  } = selector(store);

  let tipProps: StatusTipProps = null;
  let emptyStatus = true;

  if (loading) {
    tipProps = {
      status: 'loading',
    };
    emptyStatus = false;
  } else if (error) {
    tipProps = {
      status: 'error',
      onRetry: handlers.reload,
    };
    emptyStatus = false;
  } else if (keyword && list) {
    tipProps = {
      status: 'found',
      foundText: list.length > 0 ? `找到${totalCount}条结果` : `搜索"${keyword}"暂无数据`,
      onClear: handlers.clearKeyword,
    };
    emptyStatus = false;
  } else if (list && list.length <= 0) {
    tipProps = {
      status: 'empty',
    };
  }
  return { tipProps, emptyStatus };
};

interface Middleware<T> {
  (next: T): T;
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function useMiddleware<K extends keyof Addon>(addons: Addon[], name: K) {
  // infer value type
  type Value<T> = T extends Middleware<infer V> ? V : never;
  type ValueType = Value<Addon[typeof name]>;

  if (!addons || !addons.length) {
    return (initialValue: ValueType) => initialValue;
  }

  return (initialValue: ValueType) =>
    addons
      .map(x => x[name])
      .filter(Boolean)
      .reduce<ValueType>((previous, middleware: Middleware<any>) => middleware(previous), initialValue);
}
