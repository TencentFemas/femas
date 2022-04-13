/**
 * 对应tea2的Form.Item，处理将FieldAPI的校验信息同步到status、message等属性上
 */

import * as React from 'react';
import { FormItemProps, FormItem, BubbleContent } from 'tea-component';
import { FieldAPI } from '../../ducks/Form';

export interface FormFieldProps<T> extends FormItemProps {
  field: FieldAPI<T>;
  /**提示类型可以为文本或者气泡，默认文本提示 */
  tipsType?: 'text' | 'bubble';
}
export default function FormField<T>(props: FormFieldProps<T>) {
  const {
    field,
    message: fixedMessage,
    children,
    label = <noscript />,
    tipsType = 'text',
    status: propStatus,
    ...rest
  } = props;
  let status: FormItemProps['status'] = null;
  let message: FormItemProps['message'] = fixedMessage;

  const error = field.getError();
  const isError = field.getTouched() && !!error;

  message = isError ? error : message;
  status = isError ? 'error' : null;
  return (
    <FormItem
      status={propStatus || status}
      message={
        tipsType === 'text'
          ? message
          : message && (
              <div style={{ position: 'absolute', zIndex: 2 }}>
                <BubbleContent placement='bottom' error>
                  {message}
                </BubbleContent>
              </div>
            )
      }
      label={label}
      {...rest}
    >
      {children}
    </FormItem>
  );
}

export function getErrorOfFields(fields: FieldAPI<any>[]) {
  let error: React.ReactChild;
  let isError: boolean;
  for (const field of fields) {
    error = field.getError();
    isError = field.getTouched() && !!error;
    if (isError) {
      break;
    }
  }
  return {
    error,
    isError,
  };
}

interface ControlledProps<T> {
  value: T;
  onChange: (T) => any;
}
export function controlledField<T>(field: FieldAPI<T>): ControlledProps<T> {
  return {
    value: field.getValue(),
    onChange: (v: T) => {
      field.setValue(v);
      field.setTouched();
    },
  };
}
