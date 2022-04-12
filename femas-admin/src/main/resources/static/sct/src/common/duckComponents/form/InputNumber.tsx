import * as React from 'react';
import { FieldAPI } from '../../ducks/Form';
import { InputNumber as TeaInputNumber, InputNumberProps } from 'tea-component';

interface Props extends Omit<InputNumberProps, 'value' | 'onChange'> {
  field: FieldAPI<number>;
}

function InputNumber(props: Props) {
  const { field, ...rest } = props;
  const value = field.getValue();

  return (
    <TeaInputNumber
      value={value === null ? ('' as any) : value}
      onChange={value => {
        field.setValue(+value || 0);
        field.setTouched();
      }}
      {...rest}
    />
  );
}

InputNumber['defaultLabelAlign'] = 'middle';
export default InputNumber;
