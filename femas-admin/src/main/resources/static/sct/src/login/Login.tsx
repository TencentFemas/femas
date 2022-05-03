import * as React from 'react';
import '@src/assets/css/login.css';
import { Button, Form, H1 } from 'tea-component';
import { DuckCmpProps } from 'saga-duck';
import LoginPageDuck from './LoginDuck';
import FormField from '../common/duckComponents/form/Field';
import Input from '../common/duckComponents/form/Input';

// eslint-disable-next-line prettier/prettier
export default function (props: DuckCmpProps<LoginPageDuck>) {
  const { duck, store, dispatch } = props;
  const {
    creators,
    ducks: { form },
  } = duck;
  const formApi = form.getAPI(store, dispatch);
  const { username, password } = formApi.getFields(['username', 'password']);
  const handlers = React.useMemo(
    () => ({
      login: () => dispatch(creators.login()),
    }),
    [],
  );
  return (
    <>
      <div className='tea-login'>
        <div className='tea-login__body'>
          <div className='tea-login__title'>
            <div className='tea-login__title-logos'>
              <H1> Femas</H1>
            </div>
          </div>
          <div className='tea-login__content'>
            <Form style={{ width: '100%' }}>
              <FormField label='用户名' field={username} showStatusIcon={false}>
                <Input size='full' placeholder='用户名' field={username} />
              </FormField>
              <FormField label='密码' field={password} showStatusIcon={false}>
                <Input type='password' size='full' placeholder='密码' field={password} />
              </FormField>
            </Form>
            <div className='tea-form-operate'>
              <Button type='primary' onClick={handlers.login}>
                立即登录
              </Button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
