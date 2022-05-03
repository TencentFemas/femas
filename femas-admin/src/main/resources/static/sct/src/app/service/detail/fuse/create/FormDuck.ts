import BaseForm from '@src/common/ducks/Form';
import { ISOLATION_TYPE, Strategy } from '../types';
import { Data } from './PageDuck';

export default class CreateForm extends BaseForm {
  Values: Data;

  validate(v, meta) {
    return validator(v, meta);
  }
}

const validator = CreateForm.combineValidators<Data, any>({
  targetServiceName(v) {
    if (!v) {
      return '请选择下游服务';
    }
  },
  // eslint-disable-next-line prettier/prettier
  strategy: function (v, values, meta) {
    return CreateForm.combineValidators<Strategy[]>([
      {
        api(v) {
          if (values.isolationLevel !== ISOLATION_TYPE.API) return;
          return !v && '请选择API';
        },
      },
    ])(v, meta);
  },
});
