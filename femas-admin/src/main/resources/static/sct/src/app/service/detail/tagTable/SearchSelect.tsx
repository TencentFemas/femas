import * as React from 'react';
import { Select, SelectMultiple } from 'tea-component';

interface Option {
  label: string;
  value: string;
}

interface Props {
  onChange?: (valueList: Array<string>) => void;
  options: Array<Option>;
  valueList?: Array<string>;
  multiple?: boolean;
  placeholder?: string;
  style?: React.CSSProperties;
}

export default class SearchSelect extends React.Component<Props, any> {
  constructor(props) {
    super(props);
    this.state = {
      // checkedList: [...props.selectOptions],
      multiple: props.multiple === undefined ? true : props.multiple,
      internalValueList: [...props.valueList],
      internalError: '',
    };
    // 记录最开始的props
    this.onSubmit = this.onSubmit.bind(this);
  }

  UNSAFE_componentWillReceiveProps(props) {
    if (props.valueList !== this.props.valueList) {
      this.setState({
        valueList: [...props.valueList],
      });
    }
  }

  onSubmit(valueList) {
    // 判断，如果是单选，选了多个报错
    if (!this.props.multiple && valueList.length > 1) {
      this.setState({
        internalError: '"等于/不等于"逻辑关系时，仅支持单选',
      });
      return false;
    }

    // 点击确定
    this.props.onChange(valueList);

    this.setState({
      internalError: '',
      showList: false,
    });
  }

  render() {
    const { valueList = [], multiple = true } = this.props;
    return (
      <>
        {multiple ? (
          <SelectMultiple
            boxSizeSync
            searchable
            appearence='button'
            options={this.props.options}
            onChange={valueList => {
              this.onSubmit(valueList);
            }}
            value={valueList}
            {...this.props}
          />
        ) : (
          <Select
            boxSizeSync
            searchable
            type={'simulate'}
            appearence={'button'}
            options={this.props.options}
            value={valueList[0] || ''}
            {...this.props}
            onChange={value => this.onSubmit([value])}
          />
        )}
      </>
    );
  }
}
