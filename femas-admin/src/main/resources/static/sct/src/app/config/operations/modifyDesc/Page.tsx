import React from 'react';
import { DuckCmpProps, purify } from 'saga-duck';
import Duck from './PageDuck';
import Dialog from '@src/common/duckComponents/Dialog';
import { Form } from 'tea-component';
import FormField from '@src/common/duckComponents/form/Field';
import Input from '@src/common/duckComponents/form/Input';

export default function Create(props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const { selectors } = duck;
  const visible = selectors.visible(store);
  if (!visible) {
    return <noscript />;
  }
  return (
    <Dialog duck={duck} store={store} dispatch={dispatch} size={800} title={'编辑描述'}>
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
  const { configDesc } = formApi.getFields(['configDesc']);

  return (
    <>
      <Form>
        <FormField field={configDesc} label={'描述(选填)'}>
          <Input
            style={{ width: 400 }}
            multiline={true}
            field={configDesc}
            maxLength={200}
            placeholder={'不超过200个字符'}
          />
        </FormField>
      </Form>
    </>
  );
});
