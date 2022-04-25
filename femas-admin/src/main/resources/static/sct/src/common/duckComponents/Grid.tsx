/**
 * 与ducks/Grid关联的Grid组件
 */
import * as React from 'react';
import { Table, TableProps } from 'tea-component';
import { RadioableOptions, SelectableOptions } from 'tea-component/lib/table/addons';
import { DuckCmpProps, memorize } from 'saga-duck';
import Duck from '../ducks/Grid';

const { selectable, radioable, autotip } = Table.addons;
const getHandlers = memorize((duck: Duck, dispatch) => ({
  // $disabled状态的记录过滤掉不选
  select: (list: any[]) => dispatch(duck.creators.select(list.filter(x => !x.$disabled && !x.disabled))),
}));

type HANDLER = () => void;

export interface Props extends Omit<TableProps, 'recordKey'> {
  onClear?: HANDLER;
  onRetry?: HANDLER;
  emptyTips?: string | JSX.Element;
  needChecker?: boolean;
  checkerOptions?: SelectableOptions;
  radioOptions?: RadioableOptions;
  needRadio?: boolean;
  needAutoTip?: boolean;
}

export default function DuckGrid(props: DuckCmpProps<Duck> & Props) {
  const {
    duck: { selectors, recordKey },
    store,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    dispatch,
    onClear,
    onRetry,
    emptyTips,
    columns,
    addons = [],
    needAutoTip = true,
    checkerOptions = {},
    radioOptions = {},
    ...rest
  } = props;
  const handlers = getHandlers(props);
  const selection = selectors.selection(store);
  const records = selectors.list(store);
  const loading = selectors.loading(store);
  const error = selectors.error(store);
  const showError = !loading && !!error;
  const keyword = selectors.searchCondition(store);
  const selectedKeys = selection.map(item => item[recordKey]);
  const totalCount = selectors.totalCount(store);
  // 多选框
  if (props.needChecker) {
    addons.push(
      selectable({
        value: selectedKeys,
        onChange: keys => {
          // 设置selection
          handlers.select(records.filter(item => keys.includes(item[recordKey])));
        },
        ...checkerOptions,
      }),
    );
  }

  // 单选框
  if (props.needRadio) {
    addons.push(
      radioable({
        value: selectedKeys[0],
        onChange: key => {
          handlers.select(records.filter(item => item[recordKey] === key));
        },
        ...radioOptions,
      }),
    );
  }

  if (needAutoTip) {
    const message = error?.message;
    addons.push(
      autotip({
        isLoading: loading,
        isError: showError,
        isFound: !!keyword,
        onClear: onClear,
        onRetry: onRetry,
        foundKeyword: keyword,
        foundCount: totalCount || 0,
        errorText: showError ? message || '加载失败' : null,
        emptyText: emptyTips,
      }),
    );
  }

  return (
    <Table
      recordKey={recordKey}
      columns={columns}
      records={records}
      rowDisabled={recordDisabled}
      addons={addons}
      {...rest}
    />
  );
}

// 和Bee.js的Grid保持行为一致，当为$disabled时不能选中
function recordDisabled(x) {
  return x.$disabled;
}
