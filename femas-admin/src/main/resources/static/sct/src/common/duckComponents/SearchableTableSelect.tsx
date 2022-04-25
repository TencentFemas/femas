import * as React from 'react';
import { DuckCmpProps, memorize, purify } from 'saga-duck';
import Duck from '@src/common/ducks/SearchableTableSelectDuck';
import {
  Bubble,
  BubbleContentProps,
  Dropdown,
  DropdownProps,
  SearchBoxProps,
  StatusTip,
  StatusTipProps,
  Table,
  TableColumn,
  TableProps,
} from 'tea-component';
import { radioable, scrollable, ScrollableAddonOptions, selectable } from 'tea-component/lib/table/addons';
import { SearchableSearchBox } from './SearchableSelect';

interface MyDuck<T> extends Duck {
  Item: T;
}

interface Props<T = any> extends DuckCmpProps<MyDuck<T>>, Omit<DropdownProps, 'button' | 'children' | 'clickClose'> {
  placeholder?: React.ReactChild;
  searchPlaceholder?: string;
  //是否多选
  multiple?: boolean;
  recordKey?: string | Function;
  columns?: TableColumn[];
  onSelect?: Function;
  tableWidth?: string;
  inputWidth?: number | string;
  scrollableOptions?: Omit<ScrollableAddonOptions, 'onScrollBottom'>;
  searchable?: boolean;
  tableSelectable?: boolean;
  dropdown?: boolean;
  tableProps?: Omit<TableProps, 'records' | 'topTip' | 'bottomTip' | 'recordKey' | 'columns' | 'addons'>;
  // 全选时dropdown显示的文本
  selectAllText?: string;
  rowSelect?: boolean;
  bubblePlacement?: BubbleContentProps['placement'];

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
  selectIds: selected => dispatch(creators.selectIds(selected)),
}));

const SearchableTableSelect = purify(function Cmp<T>(props: Props<T>) {
  const defaultColumns = [
    {
      key: 'id',
      header: 'ID',
      width: '100px',
      render: item => {
        return props.duck.getId(item);
      },
    },
  ];
  const defaultScrollableOption = {
    maxHeight: '200px',
    minHeight: '200px',
    minWidth: '400px',
  };
  const {
    duck,
    store,
    dispatch,
    itemDisabled = () => false,
    itemRenderer = o => duck.getId(o),
    placeholder = '请选择...',
    searchPlaceholder = '输入关键字搜索',
    size = 'm',
    columns,
    recordKey,
    multiple = false,
    onSelect,
    tableWidth = '600px',
    inputWidth = '100%',
    scrollableOptions = defaultScrollableOption,
    searchable = true,
    tableSelectable = true,
    dropdown = true,
    tableProps = {},
    style = { maxWidth: 600 },
    selectAllText = '',
    rowSelect = true,
    bubblePlacement = 'right',
    ...rest
  } = props;
  const { selector, selectors } = duck;
  const { id: selectedId, ids: selectedIds, list } = selector(store);

  const selected = selectors.selected(store);

  let buttonText = placeholder;
  let bubbleContent = null;
  if (multiple) {
    const renderedItems = [];
    const selectedItems = selectors.selectedItems(store) || [];
    const buttonTextArray = [];
    for (const item of selectedItems) {
      renderedItems.push(item ? <div key={duck.getId(item)}>{itemRenderer(item)}</div> : null);
      //这里用getId是因为不知道会传什么itemRenderer进来，不好都塞进DropDown里
      buttonTextArray.push(duck.getId(item));
    }
    if (selectAllText && list && list.length === selectedItems.length && selectedItems.length !== 0) {
      // selectAllText存在且选择长度于当前list长度相同时，展示selectAllText
      buttonText = selectAllText;
      bubbleContent = null;
    } else {
      buttonText = buttonTextArray.join(',') || placeholder;
      bubbleContent = selectedItems.length > 0 ? <>{renderedItems}</> : null;
    }
  } else {
    const selectedItem = selected ? itemRenderer(selected) : null;
    buttonText = selectedItem || selectedId || placeholder;
  }

  const mainBody = (
    <div>
      {searchable && (
        <SearchableSearchBox duck={duck} store={store} dispatch={dispatch} placeholder={searchPlaceholder} />
      )}
      <SearchableTable
        duck={duck}
        store={store}
        dispatch={dispatch}
        value={multiple ? selectedIds : selectedId}
        recordKey={
          recordKey ||
          (item => {
            return duck.getId(item);
          })
        }
        multiple={multiple}
        columns={columns || defaultColumns}
        rowDisabled={itemDisabled}
        onSelect={onSelect}
        tableWidth={tableWidth}
        scrollableOptions={scrollableOptions}
        tableProps={tableProps}
        rowSelect={rowSelect}
        tableSelectable={tableSelectable}
      />
    </div>
  );

  return dropdown ? (
    <Bubble content={bubbleContent} trigger='hover' placement={bubblePlacement}>
      <div style={{ overflow: 'hidden', width: inputWidth }}>
        <Dropdown appearence='button' button={buttonText} clickClose={false} size={size} style={style} {...rest}>
          {mainBody}
        </Dropdown>
      </div>
    </Bubble>
  ) : (
    mainBody
  );
});

SearchableTableSelect['defaultLabelAlign'] = 'middle';
export default SearchableTableSelect;

// 子组件，可以自由组装成其它形式
export interface SearchableSearchBoxProps
  extends DuckCmpProps<MyDuck<any>>,
    Omit<SearchBoxProps, 'value' | 'onChange' | 'onSearch' | 'onClear'> {}

export function SearchableTable(props) {
  const handlers = getHandlers(props);
  const {
    duck,
    store,
    dispatch,
    columns,
    recordKey,
    multiple,
    value,
    onSelect,
    tableWidth,
    scrollableOptions,
    tableProps,
    rowDisabled,
    rowSelect,
    tableSelectable,
  } = props;
  const { selector } = duck;
  const {
    keyword,
    filterList,
    nomore,
    fetcher: { loading },
  } = selector(store);
  const selectAddon = multiple
    ? selectable({
        value: value ? value : [],
        rowSelect: rowSelect,
        onChange(value) {
          onSelect && onSelect(value);
          dispatch(handlers.selectIds(value));
        },
      })
    : radioable({
        value: value,
        rowSelect: rowSelect,
        onChange(value) {
          onSelect && onSelect(value);
          dispatch(handlers.select(value));
        },
      });
  return (
    <div style={{ width: tableWidth }}>
      <Table
        recordKey={recordKey}
        records={filterList || []}
        columns={columns}
        rowDisabled={rowDisabled}
        addons={[
          scrollable({
            ...scrollableOptions,
            onScrollBottom: nomore ? undefined : handlers.more,
          }),
          tableSelectable ? selectAddon : '',
        ]}
        topTip={
          !loading &&
          (keyword || filterList.length === 0) && (
            <SearchableTableStatusTip duck={duck} store={store} dispatch={dispatch} />
          )
        }
        bottomTip={loading && <SearchableTableStatusTip duck={duck} store={store} dispatch={dispatch} />}
        {...tableProps}
      />
    </div>
  );
}

export function SearchableTableStatusTip(props: SearchableTableStatusTipProps) {
  const handlers = getHandlers(props);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { duck, store, dispatch, ...rest } = props;
  const { selector } = duck;
  const {
    keyword,
    filterList,
    fetcher: { loading, error },
  } = selector(store);

  let tipProps: StatusTipProps = null;

  if (loading) {
    tipProps = {
      status: 'loading',
    };
  } else if (error) {
    tipProps = {
      status: 'error',
      onRetry: handlers.reload,
    };
  } else if (keyword && filterList) {
    tipProps = {
      status: 'found',
      foundText: filterList.length > 0 ? `找到${filterList.length}条结果` : `搜索"${keyword}"暂无数据`,
      onClear: handlers.clearKeyword,
    };
  } else if (filterList && filterList.length <= 0) {
    tipProps = {
      status: 'empty',
    };
  }
  return <StatusTip {...tipProps} {...rest} />;
}

/**
 * 状态Tip
 */
export interface SearchableTableStatusTipProps
  extends DuckCmpProps<MyDuck<any>>,
    Omit<StatusTipProps, 'status' | 'onRetry' | 'onClear'> {}
