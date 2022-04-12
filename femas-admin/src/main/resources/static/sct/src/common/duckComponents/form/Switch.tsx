import * as React from 'react';
import { FieldAPI } from '../../ducks/Form';
import { Switch as TeaSwitch, SwitchProps } from 'tea-component';

interface Props extends Omit<SwitchProps, 'value' | 'onChange'> {
  field: FieldAPI<string | boolean>;
  trueValue?: string | boolean;
  falseValue?: string | boolean;
  label?: string | React.ReactNode;
}

function Switch(props: Props) {
  const { field, trueValue = true, falseValue = false, label, ...rest } = props;

  return (
    <TeaSwitch
      value={field.getValue() === trueValue}
      onChange={() => {
        const value = field.getValue() === trueValue ? falseValue : trueValue;
        field.setValue(value);
      }}
      {...rest}
    >
      {label}
    </TeaSwitch>
  );
}

Switch['defaultLabelAlign'] = 'top';
export default Switch;
