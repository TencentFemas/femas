import * as React from 'react';
import { FieldAPI } from '../../ducks/Form';
import { Input as TeaInput, InputProps } from 'tea-component';

interface Props extends Omit<InputProps, 'value'> {
  field: FieldAPI<string>;
}

function Input(props: Props) {
  const { field, onBlur, onChange, ...rest } = props;

  return (
    <TeaInput
      value={field.getValue() || ''}
      onChange={value => {
        field.setValue(value);
        if (onChange) {
          return onChange(value, this);
        }
      }}
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

Input['defaultLabelAlign'] = 'middle';
export default Input;
