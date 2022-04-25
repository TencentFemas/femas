import React from 'react';
import { DuckCmpProps, purify } from 'saga-duck';
import Duck, { nameTipMsg } from './CreateDuck';
import Dialog from '../../../../common/duckComponents/Dialog';
import { Form } from 'tea-component';
import FormField from '@src/common/duckComponents/form/Field';
import Input from '@src/common/duckComponents/form/Input';
import AutoComplete from '@src/common/components/AutoComplete';

export default function Create(props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const { selectors } = duck;
  const visible = selectors.visible(store);
  if (!visible) {
    return <noscript />;
  }
  return (
    <Dialog duck={duck} store={store} dispatch={dispatch} size='m' title={'新增命名空间'}>
      <CreateForm duck={duck} store={store} dispatch={dispatch} />
    </Dialog>
  );
}

const CreateForm = purify(function CreateForm(props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const {
    ducks: { form },
  } = duck;

  const formApi = form.getAPI(store, dispatch);
  const { name, desc, registryId } = formApi.getFields(['name', 'desc', 'registryId']);
  const options = duck.selectors.options(store);

  return (
    <>
      <Form>
        <FormField field={name} label={'命名空间名称'} required message={nameTipMsg}>
          <Input field={name} maxLength={60} placeholder={'请输入命名空间名称'} />
        </FormField>
        <FormField field={registryId} label='关联注册中心' align={options.registryId ? 'auto' : 'middle'}>
          {options.registryId ? (
            <Form.Text>
              {options.registryName}({options.registryId})
            </Form.Text>
          ) : (
            <AutoComplete
              value={registryId.getValue()}
              dataSource={options.registryList}
              onItemSelect={item => registryId.setValue(item?.value || '')}
            />
          )}
        </FormField>
        <FormField field={desc} label={'命名空间描述'} message='选填，不超过128个字符'>
          <Input field={desc} multiline />
        </FormField>
      </Form>
    </>
  );
});
