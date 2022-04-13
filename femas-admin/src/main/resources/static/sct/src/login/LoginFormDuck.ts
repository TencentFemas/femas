import Form from '../common/ducks/Form';

export default class LoginFormDuck extends Form {
  Values: {
    username: string;
    password: string;
  };
  Meta: {};

  validate(values) {
    return validator(values);
  }
}

const validator = Form.combineValidators({
  username(v) {
    if (!v) {
      return '请填写用户名';
    }
  },
  password(v) {
    if (!v) {
      return '请填写密码';
    }
  },
});
