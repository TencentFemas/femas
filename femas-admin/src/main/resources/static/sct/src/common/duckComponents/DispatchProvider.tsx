import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';

export interface Props {
  dispatch?: Dispatch<any>;
  children?: (dispatch: Dispatch<any>) => JSX.Element;
}

/**
 * 用于获取dispatch方法，使得部分逻辑可以移到duck中实现（例如列配置）
 * 但需要注意的是，当前的Dialog实现很暴力，可能会有影响，建议谨慎使用
 */
@connect(null, dispatch => ({ dispatch }))
export default class DispatchProvider extends React.Component<Props, {}> {
  render() {
    const props = this.props;
    return props.children(props.dispatch);
  }
}
