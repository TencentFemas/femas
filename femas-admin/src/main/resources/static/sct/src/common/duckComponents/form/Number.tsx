import * as React from 'react';
import { FieldAPI } from '../../ducks/Form';
import { Input, InputProps } from 'tea-component';

interface Props extends Omit<InputProps, 'value' | 'onChange'> {
  field: FieldAPI<number>;
}

/**
 * 与InputNumber的区别： 基于Input，仅type为number，没有+/-等操作
 */
function Number(props: Props) {
  const { field, onBlur, ...rest } = props;

  const value = field.getValue();
  const stringValue = typeof value === 'number' ? String(value) : '';
  return (
    <Input
      type='number'
      value={stringValue}
      onChange={value => field.setValue(+value || 0)}
      onBlur={e => {
        field.setTouched();
        if (onBlur) {
          return onBlur(e);
        }
      }}
      {...rest}
    />
  );
}

Number['defaultLabelAlign'] = 'middle';
export default Number;
