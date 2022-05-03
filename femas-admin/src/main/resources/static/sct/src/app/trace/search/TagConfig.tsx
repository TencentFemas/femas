import * as React from 'react';
import { Bubble, FormControl, FormItem, FormText, Icon, Text } from 'tea-component';
import Input from '@src/common/duckComponents/form/Input';
import { DuckCmpProps } from 'saga-duck';
import Duck from './SearchFormDuck';
import DetailDuck from '../detail/SearchFormDuck';

const DefaultMes =
  'Tag中包含系统和业务自定义的标签，通过标识span以实现标识查询、过滤与聚合，支持精确匹配。如果您需要查询非入口业务标签，请在上方选择Span查询。';

const DefaultTitle = '入口业务标签（Tag）';

export default class TagConfig extends React.Component<
  DuckCmpProps<Duck | DetailDuck> & { message?: string; title?: string }
> {
  render() {
    const { duck, dispatch, store, message = DefaultMes, title = DefaultTitle } = this.props;
    const formApi = duck.getAPI(store, dispatch);
    const { tags } = formApi.getFields(['tags']);

    //添加
    const addEnvInfo = () => {
      tags.setValue([
        ...tags.getValue(),
        {
          key: '',
          value: '',
        },
      ]);
    };

    //删除
    const deleteEnvInfo = index => {
      const newValue = tags.getValue();
      newValue.splice(index, 1);
      tags.setValue([...newValue]);
    };

    const renderEnvInfoList = () => {
      const tagsListField = tags.asArray();
      return (
        <section style={{ width: 444 }}>
          {[...tagsListField].map((field, index) => {
            const { key, value } = field.getFields(['key', 'value']);
            const validateResult = key.getTouched() && key.getError();
            const validateValueResult = value.getTouched() && value.getError();
            return (
              <section key={index} style={{ marginTop: index !== 0 ? 10 : 0 }}>
                <FormControl
                  status={validateResult ? 'error' : null}
                  showStatusIcon={false}
                  style={{ display: 'inline' }}
                >
                  <Input field={key} placeholder={'请输入Tag Key'} style={{ width: 128 }} />
                </FormControl>

                <FormText
                  style={{
                    display: 'inline-block',
                    marginRight: 24,
                    marginTop: 6,
                  }}
                >
                  :
                </FormText>
                <FormControl
                  status={validateValueResult ? 'error' : null}
                  showStatusIcon={false}
                  style={{ display: 'inline' }}
                >
                  <Input field={value} placeholder={'请输入Tag Value'} />
                </FormControl>
                {tags.getValue()?.length > 1 ? (
                  <Icon type='not' size='s' onClick={() => deleteEnvInfo(index)} style={{ marginTop: 7 }} />
                ) : (
                  <Text style={{ width: 16, display: 'inline-block' }} />
                )}
                {index === tags.getValue()?.length - 1 ? (
                  <Icon type='and' onClick={() => addEnvInfo()} style={{ marginTop: 7, marginLeft: 7 }} />
                ) : null}
                {(validateResult || validateValueResult) && (
                  <Text reset parent='p' theme='danger' style={{ marginTop: 10 }}>
                    {validateResult || validateValueResult}
                  </Text>
                )}
              </section>
            );
          })}
        </section>
      );
    };

    return (
      <>
        <FormItem
          label={
            <Text>
              {title}
              <Bubble placement='right' content={message}>
                <Icon type='info' />
              </Bubble>
            </Text>
          }
          align='middle'
        >
          {renderEnvInfoList()}
        </FormItem>
      </>
    );
  }
}
