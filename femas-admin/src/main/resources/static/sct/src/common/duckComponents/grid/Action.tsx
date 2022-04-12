import * as React from 'react';
import { purify } from 'saga-duck';
import { Dispatch } from 'redux';
import { Button } from 'tea-component';
import DispatchProvider from '../DispatchProvider';

export interface Props {
  text?: string;
  className?: string;
  disabled?: boolean;
  tip?: string;
  visible?: boolean;
  children?: React.ReactNode;

  fn(dispatch?: Dispatch<any>, e?);
}

@purify
export default class Action extends React.Component<Props, {}> {
  render() {
    const props = this.props;
    if (props.visible === false) {
      return <noscript />;
    }
    return (
      <DispatchProvider>
        {dispatch => (
          <Button
            type='link'
            disabled={props.disabled}
            key={props.text}
            onClick={e => !props.disabled && props.fn(dispatch, e)}
            tooltip={props.tip}
            className={props.className}
          >
            {props.text || props.children}
          </Button>
        )}
      </DispatchProvider>
    );
  }
}
