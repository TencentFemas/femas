import * as React from 'react';
import { purify } from 'saga-duck';
import { Copy, Text } from 'tea-component';

export interface CopyableTextProps {
  text: string;
  emptyTip?: string;
  isHoverIcon?: boolean;
  style?: React.CSSProperties;
  copyText?: string;
  component?: string | JSX.Element | JSX.Element[];
  splitWidth?: number;
}

class CopyableText extends React.Component<CopyableTextProps, Readonly<{}>> {
  render() {
    const { text, emptyTip = '-', isHoverIcon = true, style, copyText, component, splitWidth = 16 } = this.props;
    return (
      <>
        <Text overflow tooltip={text} style={{ maxWidth: `calc(100% - ${splitWidth}px)`, ...style }}>
          {component || text || emptyTip}
        </Text>
        {text && (
          <Text className={isHoverIcon ? 'hover-icon' : ''}>
            <Copy text={copyText || text} />
          </Text>
        )}
      </>
    );
  }
}

export default purify(CopyableText);
