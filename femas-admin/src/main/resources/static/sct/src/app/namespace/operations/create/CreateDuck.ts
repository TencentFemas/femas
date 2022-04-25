import { RegistryItem } from './../../../registry/types';
import FormDialog from '@src/common/ducks/FormDialog';
import Form from '@src/common/ducks/Form';
import { NamespaceItem } from '../../types';
import { put, select } from 'redux-saga/effects';
import { resolvePromise } from 'saga-duck/build/helper';
import { createNamespace, modifyNamespace } from '../../model';

export interface DialogOptions {
  registryId?: string;
  registryName?: string;
  namespaceId?: string;
  addMode: boolean;
  registryList: Array<RegistryItem>;
}
export default class CreateDuck extends FormDialog {
  Options: DialogOptions;
  get Form() {
    return CreateForm;
  }
  *onSubmit() {
    const {
      selectors,
      ducks: { form },
    } = this;
    const options = selectors.options(yield select());
    const values = form.selectors.values(yield select());
    const params = {
      name: values.name,
      desc: values.desc || '',
      registryId: values.registryId ? [values.registryId] : [],
    } as any;
    if (!options.addMode) {
      params.namespaceId = options.namespaceId;
      return yield* resolvePromise(modifyNamespace(params));
    }
    return yield* resolvePromise(createNamespace(params));
  }
  *onShow() {
    yield* super.onShow();
    const {
      selectors,
      ducks: { form },
    } = this;
    const options = selectors.options(yield select());
    yield put(form.creators.setMeta(options));
    // TODO 表单弹窗逻辑，在弹窗关闭后自动cancel
  }
}
export type Values = NamespaceItem;
class CreateForm extends Form {
  Values: Values;
  Meta: {};
  validate(v: this['Values'], meta: this['Meta']) {
    return validator(v, meta);
  }
}
export const nameTipMsg = '最长60个字符，只能包含小写字母、数字及分隔符（"-")，且不能以分隔符开头或结尾';
const validator = CreateForm.combineValidators<Values, {}>({
  name(v) {
    if (!v) {
      return '请填写命名空间名称';
    }
    if (!/^[a-z0-9]([-a-z0-9]*[a-z0-9])?$/.test(v) || v.length > 60) {
      return nameTipMsg;
    }
  },
  desc(v) {
    if (v && v.length > 128) {
      return '长度不超过128个字符';
    }
  },
});
