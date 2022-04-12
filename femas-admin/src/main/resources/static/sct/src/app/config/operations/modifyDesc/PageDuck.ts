import FormDialog from '@src/common/ducks/FormDialog';
import Form from '@src/common/ducks/Form';
import { ConfigItem } from '../../types';
import { put, select } from 'redux-saga/effects';
import { resolvePromise } from 'saga-duck/build/helper';
import { configureConfig } from '../../model';

export interface DialogOptions {
  namespaceId: string;
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
    const { namespaceId } = selectors.options(yield select());
    const {
      configId,
      configName,
      systemTag,
      configType,
      configDesc,
      serviceName,
      currentReleaseVersion,
    } = form.selectors.values(yield select());
    const res = yield* resolvePromise(
      configureConfig({
        namespaceId,
        configId,
        configName,
        serviceName,
        systemTag,
        configType,
        configDesc,
        configValue: currentReleaseVersion?.configValue || '',
      }),
    );
    return res;
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
export type Values = ConfigItem;

class CreateForm extends Form {
  Values: Values;
  Meta: {};

  validate(v: this['Values'], meta: this['Meta']) {
    return validator(v, meta);
  }
}

const validator = CreateForm.combineValidators<Values, {}>({});
