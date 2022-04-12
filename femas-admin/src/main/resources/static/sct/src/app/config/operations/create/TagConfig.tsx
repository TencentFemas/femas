import * as React from 'react';
import { FormControl, FormText, Icon, Text } from 'tea-component';
import Input from '@src/common/duckComponents/form/Input';
import { DuckCmpProps } from 'saga-duck';
import Duck from './CreateDuck';

export default class TagConfig extends React.Component<DuckCmpProps<Duck>> {
  render() {
    const { duck, dispatch, store } = this.props;
    const {
      ducks: { form },
    } = duck;
    const formApi = form.getAPI(store, dispatch);
    const { tags } = formApi.getFields(['tags']);

    //添加
    const addTagInfo = () => {
      tags.setValue([
        ...tags.getValue(),
        {
          key: '',
          value: '',
        },
      ]);
    };

    //删除
    const deleteTagInfo = index => {
      const newValue = tags.getValue();
      newValue.splice(index, 1);
      tags.setValue([...newValue]);
    };

    const renderTagInfoList = () => {
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
                  <Input field={key} placeholder={'请输入标签名'} style={{ width: 128 }} />
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
                  <Input field={value} placeholder={'请输入标签值'} />
                </FormControl>
                {tags.getValue()?.length > 1 ? (
                  <Icon type='not' size='s' onClick={() => deleteTagInfo(index)} style={{ marginTop: 7 }} />
                ) : (
                  <Text style={{ width: 16, display: 'inline-block' }} />
                )}
                {index === tags.getValue()?.length - 1 ? (
                  <Icon type='and' onClick={() => addTagInfo()} style={{ marginTop: 7, marginLeft: 7 }} />
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

    return renderTagInfoList();
  }
}
