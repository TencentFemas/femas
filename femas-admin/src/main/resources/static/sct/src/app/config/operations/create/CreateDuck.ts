import FormDialog from '@src/common/ducks/FormDialog';
import Form from '@src/common/ducks/Form';
import { CONFIG_TYPE, ConfigItem } from '../../types';
import { put, select } from 'redux-saga/effects';
import { resolvePromise } from 'saga-duck/build/helper';
import { configureConfig, operateConfigVersion } from '../../model';
import { nameTipMessage } from '@src/common/types';

export enum EDIT_TYPE {
  create = 'create_new',
  generate = 'create',
  release = 'release',
  rollback = 'rollback',
}

export const EDIT_TYPE_NAME = {
  [EDIT_TYPE.create]: '新建配置',
  [EDIT_TYPE.generate]: '生成新版本',
  [EDIT_TYPE.release]: '配置发布',
  [EDIT_TYPE.rollback]: '配置回滚',
};

export const EDIT_TYPE_BTN_NAME = {
  [EDIT_TYPE.create]: '提交',
  [EDIT_TYPE.generate]: '提交',
  [EDIT_TYPE.release]: '发布',
  [EDIT_TYPE.rollback]: '回滚',
};

export interface DialogOptions {
  configId?: string;
  namespaceId?: string;
  editType: EDIT_TYPE;
  configVersion?: number;
  configVersionId?: string;
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
    const { namespaceId, editType, configVersionId } = selectors.options(yield select());
    const { configName, configType, configValue, tags, serviceName, configDesc, configId } = form.selectors.values(
      yield select(),
    );
    try {
      if (editType === EDIT_TYPE.create) {
        const sysTag = {};
        tags
          .filter(d => d.key && d.value)
          .forEach(d => {
            sysTag[d.key] = d.value;
          });
        const res = yield* resolvePromise(
          configureConfig({
            namespaceId,
            configName,
            configType,
            configValue,
            systemTag: JSON.stringify(sysTag),
            serviceName,
            configDesc,
          }),
        );
        return res;
      } else {
        // 操作
        const res = yield* resolvePromise(
          operateConfigVersion({
            command: editType,
            configVersionId,
            configId,
            configValue,
            namespaceId,
          }),
        );
        return res;
      }
    } catch (error) {
      if (error.data === 'formatError') {
        yield put(form.creators.markInvalid('configValue', error.message));
        yield put(form.creators.setAllTouched());
      }
      throw error;
    }
  }

  *onShow() {
    yield* super.onShow();
    const {
      selectors,
      ducks: { form },
    } = this;
    const options = selectors.options(yield select());
    const data = selectors.data(yield select());
    yield put(form.creators.setMeta(options));
    const tag = data.systemTag ? JSON.parse(data.systemTag) : null;
    yield put(
      form.creators.setValues({
        ...data,
        configType: data.configType || CONFIG_TYPE.yaml,
        tags: tag
          ? Object.keys(tag).map(key => ({ key, value: tag[key] }))
          : [
              {
                key: '',
                value: '',
              },
            ],
      }),
    );
    // TODO 表单弹窗逻辑，在弹窗关闭后自动cancel
  }
}

interface Tag {
  key: string;
  value: string;
}

export interface Values extends ConfigItem {
  tags?: Array<Tag>;
}

class CreateForm extends Form {
  Values: Values;
  Meta: {};

  validate(v: this['Values'], meta: this['Meta']) {
    return validator(v, meta);
  }
}

const tagTipMessage = '不能为空，最长60个字符，只能包含字母、数字及分隔符("_"、"-"、“.”)，且不能以分隔符开头或结尾';

const validator = CreateForm.combineValidators<Values, {}>({
  configName(v) {
    if (!v) {
      return '请输入配置名称，' + nameTipMessage;
    }
    if (v?.length > 60 || !/^[a-z0-9]([-_a-z0-9]*[a-z0-9])?$/.test(v)) {
      return nameTipMessage;
    }
  },
  configValue(v) {
    if (!v) {
      return '请输入配置内容';
    }
  },
  tags: [
    {
      key(v) {
        if (!v || v?.length > 60 || !/^[a-zA-Z0-9]([-_.a-zA-Z0-9]*[a-zA-Z0-9])?$/.test(v)) {
          return tagTipMessage;
        }
      },
      value(v) {
        if (!v || v?.length > 60 || !/^[a-zA-Z0-9]([-_.a-zA-Z0-9]*[a-zA-Z0-9])?$/.test(v)) {
          return tagTipMessage;
        }
      },
    },
  ],
});
