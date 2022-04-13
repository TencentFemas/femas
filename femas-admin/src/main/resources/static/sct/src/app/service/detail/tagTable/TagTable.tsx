import * as React from 'react';
import ServiceSelect from './ServiceSelect';
import NamespaceServiceSelect, { getValueNamespace } from './NamespaceServiceSelect';
import {
  KEY_OPERATOR_MAP,
  OPERATOR,
  OPERATOR_ALL,
  OPERATOR_MAP,
  SYSTEM_TAG,
  SYSTEM_TAG_OPTIONS,
  TAG_TYPE,
  TAG_TYPE_OPTIONS,
} from './types';
import { validateKey, validateVal } from './validateUtil';
import { DuckCmpProps } from 'saga-duck';
import TagTableDuck from './TagTableDuck';
import { Button, FormControl, Input, Select, Table, Text } from 'tea-component';
import insertCSS from '@src/common/helpers/insertCSS';
import { genIdForNode } from '@src/common/util/common';

insertCSS(
  'tc-15-table-box',
  `
.fullwidth{
  width: 100% !important;
}
.service-tagtable .search-input-wrap .btn-rm-txt {
  right: 0 !important;
}
`,
);
// 不同标签类型对应不同的下拉框
const SELECT_MAP = {
  [SYSTEM_TAG.TSF_LOCAL_SERVICE]: ServiceSelect,
  [SYSTEM_TAG.TSF_LOCAL_NAMESPACE_SERVICE]: NamespaceServiceSelect,
};

interface Condition {
  tagType: string; // 标签类型
  tagField: string; // 标签key
  tagOperator: string; // 逻辑关系
  tagValue: string; // 值, 多个值用,分隔
}

export interface SearchParams {
  namespaceId: string;
  serviceName: string;
}

export enum CONDITION_KEY {
  TYPE = 'tagType',
  KEY = 'tagField',
  VALUE = 'tagValue',
  OPERATOR = 'tagOperator',
  DUCKID = 'duckId',
}

interface TagTableProps {
  list?: Array<Condition>;
  onChange?: (list: Array<Condition>) => void;
  // 是否允许规则为空，是则规则剩一条的时候可删除，否则隐藏删除按钮
  isAllowEmpty?: boolean;
  // 接口查询参数
  params: SearchParams;
  // 所支持的系统标签keys
  systemTags?: Array<SYSTEM_TAG | string>;
}

export default class TagTable extends React.Component<DuckCmpProps<TagTableDuck> & TagTableProps, any> {
  private systemTagOptions: any;

  constructor(props) {
    super(props);
    const { isAllowEmpty = true, systemTags } = props;
    let { list = [] } = props;
    this.systemTagOptions = this.getSystemTagOptions(systemTags);

    if (!isAllowEmpty && !list.length) {
      // 新增一项, 默认系统标签
      list.push(this.getNewTag());
    } else if (list.length) {
      //如果列表中有项，加载对应的列表
      list = list.map(item => {
        const randomIds = genIdForNode().split('-');
        const duckId = randomIds[randomIds.length - 1];
        this.getNewDuck(duckId, item[CONDITION_KEY.KEY], this.splitStr(item[CONDITION_KEY.VALUE]));
        item[CONDITION_KEY.DUCKID] = duckId;
        return item;
      });
    }
    this.state = {
      list: [...list],
      systemTagOptions: this.systemTagOptions,
    };

    this.onChange = this.onChange.bind(this);
    this.onDelete = this.onDelete.bind(this);
    this.onAdd = this.onAdd.bind(this);
    this.isValid = this.isValid.bind(this);
    this.checkKeyValid = this.checkKeyValid.bind(this);
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    const { list = [], isAllowEmpty = true } = nextProps;
    if (!isAllowEmpty && !list.length) {
      // 新增一项, 默认系统标签
      list.push(this.getNewTag());
    }

    this.systemTagOptions = this.getSystemTagOptions(nextProps.systemTags);
    // 重新设置state里的值
    this.setState({
      list: [...list],
      systemTagOptions: this.systemTagOptions,
    });
  }

  getSystemTagOptions(systemTags) {
    return systemTags && systemTags.length
      ? SYSTEM_TAG_OPTIONS.filter(tag => systemTags.includes(tag.value))
      : SYSTEM_TAG_OPTIONS;
  }

  getNewTag() {
    const randomIds = genIdForNode().split('-');
    const duckId = randomIds[randomIds.length - 1];
    const defaultKey = this.systemTagOptions[0].value;
    this.getNewDuck(duckId, defaultKey);
    return {
      [CONDITION_KEY.TYPE]: TAG_TYPE.SYSTEM,
      [CONDITION_KEY.KEY]: defaultKey,
      [CONDITION_KEY.OPERATOR]: KEY_OPERATOR_MAP[defaultKey][0],
      [CONDITION_KEY.VALUE]: '',
      [CONDITION_KEY.DUCKID]: duckId,
    };
  }

  getNewDuck(duckId: string, conditionKey: string, valueList?: Array<string>) {
    const { params, duck, dispatch } = this.props;
    //如果是命名空间+服务名，这里需要判断一下，namespaceId是否是props传来的namespaceId（url参数）,如果不是，则需要以value中的namespaceId为准
    const newParams = { ...params };
    if (conditionKey === SYSTEM_TAG.TSF_LOCAL_NAMESPACE_SERVICE && valueList) {
      newParams.namespaceId = getValueNamespace(valueList);
    }
    dispatch(
      duck.creators.createDuck({
        options: { params: newParams, loadType: conditionKey },
        duckId: duckId,
      }),
    );
  }

  onChange(index, key, newValue) {
    // 设置值
    const current = this.state.list[index];
    let needValidation = false;
    // 修改了type值
    if (key === CONDITION_KEY.TYPE) {
      // 改为了自定义
      if (newValue === TAG_TYPE.CUSTOM) {
        // 清空key值
        current[CONDITION_KEY.KEY] = '';
        // 重新设置operator
        current[CONDITION_KEY.OPERATOR] = OPERATOR_ALL[0];
      } else {
        // 改为了系统标签
        // 设置key选项
        current[CONDITION_KEY.KEY] = this.state.systemTagOptions[0].value;

        // 设置operator
        current[CONDITION_KEY.OPERATOR] = KEY_OPERATOR_MAP[current[CONDITION_KEY.KEY]][0];
      }
      current[key] = newValue;
      current[CONDITION_KEY.VALUE] = '';

      current.keyError = '';
      current.valueError = '';
    } else if (key === CONDITION_KEY.KEY) {
      // 修改了key值
      if (current[CONDITION_KEY.TYPE] === TAG_TYPE.SYSTEM) {
        // 重设operator
        current[CONDITION_KEY.OPERATOR] = KEY_OPERATOR_MAP[newValue][0];

        // 清空value
        current[CONDITION_KEY.VALUE] = '';
        current.valueError = '';
      }
      if (newValue === SYSTEM_TAG.TSF_LOCAL_SERVICE || newValue === SYSTEM_TAG.TSF_LOCAL_NAMESPACE_SERVICE) {
        needValidation = true;
      }
      // 设置值
      current[key] = newValue;
      // 重新加载列表
      const { params, duck, dispatch } = this.props;
      const currentDuck = duck.ducks.selectDuck.getDuck(current['duckId']);
      dispatch(currentDuck.creators.load({ params, loadType: newValue }));
      current.keyError = validateKey(newValue, this.state.list);
    } else if (key === CONDITION_KEY.OPERATOR) {
      // 修改了operator值
      // 如果输入框变为了下拉框，那么清空值，否则不清空
      const oldShowInput = this.isShowInput(current);
      const newShowInput = this.isShowInput({
        ...current,
        [key]: newValue,
      });
      if (oldShowInput !== newShowInput) {
        current[CONDITION_KEY.VALUE] = '';
        current.valueError = '';
      }
      current[key] = newValue;
    } else if (key === CONDITION_KEY.VALUE) {
      // 修改了value值
      current[key] = newValue;
      current.valueError = validateVal(current, newValue);
    }
    this.setState(
      {
        list: this.state.list.concat([]),
      },
      () => {
        if (needValidation) this.checkKeyValid();
      },
    );
    // 触发onChange钩子
    this.props.onChange && this.props.onChange(this.state.list);
  }

  onDelete(index) {
    const list = this.state.list;
    list.splice(index, 1);
    this.setState({
      list,
    });
    this.props.onChange && this.props.onChange(list);
  }

  checkKeyValid() {
    if (!this.state.list.length) {
      return false;
    }
    const list = this.state.list;
    list.forEach(item => {
      item.keyError = validateKey(item[CONDITION_KEY.KEY], list);
    });
    const isValid = !list.some(item => item.keyError);
    this.setState({
      list: list.concat([]),
    });
    return isValid;
  }

  isValid() {
    const list = this.state.list;
    list.forEach(item => {
      item.keyError = validateKey(item[CONDITION_KEY.KEY], list);
      item.valueError = validateVal(item, item[CONDITION_KEY.VALUE]);
    });

    const isValid = !list.some(item => item.keyError || item.valueError);

    if (isValid) {
      // 删除无用key
      list.forEach(item => {
        delete item.keyError;
        delete item.valueError;
      });

      this.props.onChange && this.props.onChange(this.state.list);
    }

    // 重新渲染
    this.setState({
      list: list.concat([]),
    });

    return isValid;
  }

  onAdd() {
    const list = this.state.list;
    // 新增一项, 默认系统标签

    list.push(this.getNewTag());

    this.setState(
      {
        list,
      },
      () => {
        this.checkKeyValid();
      },
    );
    this.props.onChange && this.props.onChange(list);
  }

  isMultiple(item) {
    return [OPERATOR.IN, OPERATOR.NOTIN].includes(item[CONDITION_KEY.OPERATOR]);
  }

  isShowInput(item) {
    if (item[CONDITION_KEY.TYPE] === TAG_TYPE.CUSTOM) {
      // 自定义标签类型，显示为input
      return true;
    } else {
      // 系统标签, 是某几个特定类型, 且操作符不是正则表达式的时候 是下拉框
      return !(
        Object.keys(SELECT_MAP).includes(item[CONDITION_KEY.KEY]) && item[CONDITION_KEY.OPERATOR] !== OPERATOR.REGEX
      );
    }
  }

  getOperatorByType(item) {
    const type = item[CONDITION_KEY.TYPE];
    const key = item[CONDITION_KEY.KEY];
    if (type === TAG_TYPE.CUSTOM) {
      // 自定义的情况 有全部的逻辑关系
      return OPERATOR_ALL;
    } else if (type === TAG_TYPE.SYSTEM) {
      // 系统标签的key的情况，有部分
      return KEY_OPERATOR_MAP[key] || [];
    }
  }

  splitStr(str) {
    return str.split(',').filter(item => !!item);
  }

  render() {
    const { isAllowEmpty = true } = this.props;
    const { duck, store, dispatch } = this.props;
    const { ducks } = duck;
    return (
      <Table
        verticalTop
        bordered={true}
        columns={[
          {
            key: 'tagType',
            header: '标签类型',
            render: (item, rowKey) => {
              const index = Number(rowKey.split('_')[1]);
              return (
                <Select
                  type='native'
                  size='full'
                  options={TAG_TYPE_OPTIONS.map(item => {
                    return {
                      value: item.value,
                      text: item.label,
                    };
                  })}
                  value={item[CONDITION_KEY.TYPE]}
                  onChange={value => this.onChange(index, CONDITION_KEY.TYPE, value)}
                />
              );
            },
          },
          {
            key: 'tagName',
            header: '标签名',
            width: '240px',
            render: (item: any, rowKey) => {
              const index = Number(rowKey.split('_')[1]);
              return (
                <>
                  <FormControl
                    status={item.keyError ? 'error' : null}
                    message={item.keyError ? item.keyError : ''}
                    showStatusIcon={false}
                    style={{
                      width: '240px',
                      maxWidth: '240px',
                      padding: '0px',
                    }}
                  >
                    {item[CONDITION_KEY.TYPE] === TAG_TYPE.CUSTOM && (
                      <Input
                        style={{ width: '200px' }}
                        placeholder={'请输入key值'}
                        value={item[CONDITION_KEY.KEY]}
                        size={'full'}
                        onChange={val => this.onChange(index, CONDITION_KEY.KEY, val)}
                      />
                    )}
                    {item[CONDITION_KEY.TYPE] === TAG_TYPE.SYSTEM && (
                      <Select
                        type='simulate'
                        appearance={'button'}
                        boxSizeSync
                        size='m'
                        options={this.state.systemTagOptions.map(item => {
                          return {
                            value: item.value,
                            text: item.label,
                          };
                        })}
                        value={item[CONDITION_KEY.KEY]}
                        onChange={value => this.onChange(index, CONDITION_KEY.KEY, value)}
                      />
                    )}
                  </FormControl>
                </>
              );
            },
          },
          {
            key: 'operator',
            header: '逻辑关系',
            render: (item, rowKey) => {
              const index = Number(rowKey.split('_')[1]);
              return (
                <Select
                  type='native'
                  size='full'
                  options={this.getOperatorByType(item).map(val => {
                    return {
                      value: val,
                      text: OPERATOR_MAP[val],
                    };
                  })}
                  value={item[CONDITION_KEY.OPERATOR]}
                  onChange={value => this.onChange(index, CONDITION_KEY.OPERATOR, value)}
                />
              );
            },
          },
          {
            key: 'value',
            header: '值',
            width: '320px',
            render: (item: any, rowKey) => {
              const index = Number(rowKey.split('_')[1]);
              const SelectClass = SELECT_MAP[item[CONDITION_KEY.KEY]];
              const selectDuck = ducks.selectDuck.getDuck(item[CONDITION_KEY.DUCKID]);
              if (!selectDuck) return <noscript />;
              return (
                <>
                  {this.isShowInput(item) ? (
                    <FormControl
                      status={item.valueError ? 'error' : null}
                      message={item.valueError ? item.valueError : ''}
                      showStatusIcon={false}
                      style={{
                        width: '300px',
                        maxWidth: '300px',
                        padding: '0px',
                      }}
                    >
                      <Input
                        placeholder={
                          [OPERATOR.IN, OPERATOR.NOTIN].includes(item[CONDITION_KEY.OPERATOR])
                            ? '多个值请用英文半角逗号,分隔'
                            : '请输入值'
                        }
                        style={{ width: '100%' }}
                        value={item[CONDITION_KEY.VALUE]}
                        onChange={val => this.onChange(index, CONDITION_KEY.VALUE, val)}
                      />
                    </FormControl>
                  ) : (
                    <FormControl
                      status={item.valueError ? 'error' : null}
                      message={item.valueError ? item.valueError : ''}
                      showStatusIcon={false}
                      style={{
                        width: '300px',
                        maxWidth: '300px',
                        padding: '0px',
                      }}
                    >
                      <SelectClass
                        duck={selectDuck}
                        store={store}
                        dispatch={dispatch}
                        onChange={val => this.onChange(index, CONDITION_KEY.VALUE, val.join(','))}
                        style={{ width: '300px' }}
                        multiple={this.isMultiple(item)}
                        error={item.valueError || ''}
                        valueList={this.splitStr(item[CONDITION_KEY.VALUE])}
                        params={this.props.params}
                        tooltip={
                          item[CONDITION_KEY.VALUE] &&
                          item[CONDITION_KEY.VALUE].split(',').map(item => {
                            return (
                              <Text parent={'div'} key={item}>
                                {item}
                              </Text>
                            );
                          })
                        }
                      />
                    </FormControl>
                  )}
                </>
              );
            },
          },
          {
            key: '',
            header: '',
            render: (target, rowKey) => {
              const index = Number(rowKey.split('_')[1]);
              return (
                <>
                  {!isAllowEmpty && this.state.list.length === 1 ? null : (
                    <Button icon={'close'} title={'删除'} onClick={() => this.onDelete(index)} />
                  )}
                </>
              );
            },
          },
        ]}
        records={this.state.list}
        bottomTip={
          <>
            {!this.state.list.length ? (
              <Text parent={'div'}>
                没有任何标签，您可以
                <Button type='link' style={{ marginRight: 0 }} onClick={this.onAdd}>
                  新建
                </Button>
                一个标签
              </Text>
            ) : (
              <Text parent={'div'}>
                <Button type='link' onClick={this.onAdd}>
                  新增标签
                </Button>
              </Text>
            )}
          </>
        }
      />
    );
  }
}
