/**
 * 对应tea2的Form.Control，处理将FieldAPI的校验信息同步到status、message等属性上
 */

import * as React from 'react';
import { FormControl as Base, FormControlProps as BaseProps } from 'tea-component';
import { FieldAPI } from '../../ducks/Form';
import { getErrorOfFields } from './Field';

export interface FormControlProps extends Omit<BaseProps, 'status'> {
  field: FieldAPI<any> | FieldAPI<any>[];
}

export default function FormControl(props: FormControlProps) {
  const { field, message: fixedMessage, children, ...rest } = props;
  let status: BaseProps['status'] = null;
  let message: BaseProps['message'] = fixedMessage;

  const { error, isError } = getErrorOfFields([].concat(field));

  message = isError ? error : message;
  status = isError ? 'error' : null;
  return (
    <Base status={status} message={message} {...rest}>
      {children}
    </Base>
  );
}
