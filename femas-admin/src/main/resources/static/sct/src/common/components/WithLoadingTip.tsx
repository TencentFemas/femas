import * as React from 'react';
import { LoadingTip } from 'tea-component';
import { purify } from 'saga-duck';

type WithLoadingTipProps = {
  loading: boolean;
};

class WithLoadingTip extends React.Component<WithLoadingTipProps, Readonly<{}>> {
  render() {
    const { loading, children } = this.props;
    if (loading) {
      return <LoadingTip style={{ textAlign: 'center', padding: '100px', display: 'block' }} />;
    }
    return children;
  }
}

export default purify(WithLoadingTip);
