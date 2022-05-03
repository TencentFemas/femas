// 对应ducks/Form 原生下拉框
import * as React from 'react';
import { FieldAPI } from '../../ducks/Form';
import { Select as TeaSelect, SelectOptionWithGroup, SelectProps } from 'tea-component';

export type Item = SelectOptionWithGroup;
type Props = SelectProps & {
  value?: never;
  field: FieldAPI<string | number>;
  /** 历史兼容，建议使用options替代 @deprecated */
  list?: Item[];
  beforeChange?: Function;
  onChange?: (value: string | number) => void;
};

function Select(props: Props) {
  const { field, list, type = 'native', placeholder = '请选择', beforeChange, ...rest } = props;

  const isNumber = typeof field.getValue() === 'number';

  // 有些是值为undefined, select会取第一个，想取请选择
  const isUndefOrNull = field.getValue() === undefined || field.getValue() === null;

  return (
    <TeaSelect
      type={type}
      options={list}
      value={isUndefOrNull ? '' : String(field.getValue())}
      onChange={value => {
        if (typeof beforeChange === 'function') {
          if (beforeChange(value) === false) {
            return;
          }
        }
        if (props && props.onChange) {
          props.onChange(isNumber ? +value : value);
        }
        field.setValue(isNumber ? +value : value);
        field.setTouched();
      }}
      placeholder={placeholder}
      {...rest}
    />
  );
}

Select['defaultLabelAlign'] = 'middle';
export default Select;
