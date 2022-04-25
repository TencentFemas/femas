/* eslint-disable prettier/prettier */
import * as React from 'react';
import {RadioGroup, RadioGroupProps} from 'tea-component';
import {FieldAPI} from '../../ducks/Form';

interface Props<T> extends Omit<RadioGroupProps, 'value' | 'onChange'> {
  field: FieldAPI<T>;
}

export default function (props: Props<string>) {
  const {field, ...rest} = props;

  return (
      <RadioGroup
          value={field.getValue()}
          onChange={value => {
            field.setValue(value);
            field.setTouched();
          }}
          {...rest}
      />
  );
}
