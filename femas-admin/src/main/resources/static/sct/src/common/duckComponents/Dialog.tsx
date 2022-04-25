import * as React from 'react';
import { Button, Icon, Modal, ModalProps } from 'tea-component';
import { DuckCmpProps, memorize, purify } from 'saga-duck';
import Duck from '../ducks/DialogPure';

export interface DialogProps {
  size?: ModalProps['size'];
  title?: string | JSX.Element;
  className?: string;
  children?: React.ReactChild | React.ReactChild[];
  buttons?: JSX.Element | JSX.Element[] | string | ButtonsCreator;
  disableEscape?: boolean;
  disableCloseIcon?: boolean;
  defaultSubmit?: boolean;
  defaultSubmitText?: string;
  onSubmit?: (e?) => void;
  onClose?: (e?) => void;
  defaultCancel?: boolean;
  defaultCancelText?: string;
  showFooter?: boolean;
  submitTooltip?: string | React.ReactNode;
}

interface ButtonProps {
  className?: string;
  text?: string;
  tooltip?: string | React.ReactNode;
}

type ButtonCreator = (props?: ButtonProps) => JSX.Element;
type ButtonsCreator = (submitCreator?: ButtonCreator, cancelCreator?: ButtonCreator) => JSX.Element[];
const getOptions = memorize(({ creators }, dispatch) => ({
  onClose: () => {
    dispatch(creators.hide());
  },
  onSubmit: () => {
    dispatch(creators.submit());
  },
}));

function noop() {}

export default purify(function DuckDialog({
  duck: { selectors },
  store,
  size,
  title,
  children,
  buttons,
  className,
  disableEscape = true,
  disableCloseIcon = false,
  defaultSubmit = true,
  defaultSubmitText = '提交',
  onSubmit,
  onClose,
  defaultCancel = true,
  defaultCancelText = '关闭',
  showFooter = true,
  submitTooltip,
}: DuckCmpProps<Duck> & DialogProps) {
  const loading = selectors.loading(store);
  const canSubmit = selectors.canSubmit(store);
  // eslint-disable-next-line prefer-rest-params
  const options = getOptions(arguments[0]);
  const bodyStyle = showFooter ? {} : { margin: 0 };

  if (!selectors.visible(store)) {
    return <noscript />;
  }

  const submitCreator: ButtonCreator = (props = {}) => {
    const { text = defaultSubmitText, className = '', tooltip = submitTooltip } = props;
    return (
      <Button
        key='default-submit'
        type='primary'
        className={className}
        disabled={!canSubmit}
        tooltip={!canSubmit && tooltip}
        onClick={canSubmit ? onSubmit || options.onSubmit : noop}
      >
        {loading && <Icon type='loading' />}
        <span>{text}</span>
      </Button>
    );
  };
  const cancelCreator: ButtonCreator = (props = {}) => {
    const { text = defaultCancelText } = props;
    return (
      <Button key='default-cancel' type='weak' disabled={loading} onClick={loading ? noop : onClose || options.onClose}>
        <span>{text}</span>
      </Button>
    );
  };

  return (
    <Modal
      className={className}
      caption={title}
      size={size}
      disableEscape={disableEscape}
      visible={true}
      onClose={loading ? noop : onClose || options.onClose}
      disableCloseIcon={disableCloseIcon}
    >
      <Modal.Body style={bodyStyle}>{children}</Modal.Body>
      {showFooter && (
        <Modal.Footer>
          {typeof buttons === 'function' ? buttons(submitCreator, cancelCreator) : buttons}
          {defaultSubmit && submitCreator()}
          {defaultCancel && cancelCreator()}
        </Modal.Footer>
      )}
    </Modal>
  );
});
