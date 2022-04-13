import * as React from 'react';
import { FieldAPI } from '../../ducks/Form';
import { Checkbox as TeaCheckbox, CheckboxProps } from 'tea-component';

interface Props extends Omit<CheckboxProps, 'value' | 'onChange'> {
  field: FieldAPI<string | boolean>;
  trueValue?: string | boolean;
  falseValue?: string | boolean;
  label?: string | React.ReactNode;
  onChange?: (value: string | boolean) => void;
}

function Checkbox(props: Props) {
  const { field, trueValue = true, falseValue = false, disabled, label, ...rest } = props;

  return (
    <TeaCheckbox
      disabled={disabled}
      value={field.getValue() === trueValue}
      onClick={() => {
        const value = field.getValue() === trueValue ? falseValue : trueValue;
        field.setValue(value);
        props.onChange && props.onChange(value);
      }}
      {...rest}
    >
      {label}
    </TeaCheckbox>
  );
}

Checkbox['defaultLabelAlign'] = 'top';
export default Checkbox;
