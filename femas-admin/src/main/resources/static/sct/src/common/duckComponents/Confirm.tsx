/**
 * Confirm确认框，单实例可复用
 */
import * as React from 'react';
import Dialog from './Dialog';
import { DuckCmpProps, purify } from 'saga-duck';
import Duck from '../ducks/Confirm';

interface Props extends DuckCmpProps<Duck> {
  /** 历史兼容，@deprecated 建议直接使用 ducks/Confirm # show 方法 */
  size?: any;
}

export default purify(function Confirm({ duck, store, dispatch }: Props) {
  const {
    title = '确认',
    yesText = '确认',
    noText = '取消',
    content,
    showFooter = true,
    defaultCancel = true,
    ...rest
  } = duck.selectors.data(store);
  return (
    <Dialog
      duck={duck}
      store={store}
      dispatch={dispatch}
      title={title}
      defaultCancel={defaultCancel}
      defaultSubmitText={yesText}
      defaultCancelText={noText}
      showFooter={showFooter}
      {...rest}
    >
      {content}
    </Dialog>
  );
});
