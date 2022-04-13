import * as React from 'react';
import { CheckboxGroup, CheckboxGroupProps } from 'tea-component';
import { FieldAPI } from '../../ducks/Form';

interface Props<T> extends Omit<CheckboxGroupProps, 'value' | 'onChange'> {
  field: FieldAPI<T[]>;
}

// eslint-disable-next-line prettier/prettier
export default function (props: Props<string>) {
  const { field, ...rest } = props;

  return (
    <CheckboxGroup
      value={field.getValue()}
      onChange={value => {
        field.setValue(value);
        field.setTouched();
      }}
      {...rest}
    />
  );
}
