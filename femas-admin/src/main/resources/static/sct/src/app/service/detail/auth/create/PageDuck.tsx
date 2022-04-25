import { reduceFromPayload } from 'saga-duck';
import { put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { delay } from 'redux-saga';
import FormDuck from '@src/common/ducks/Form';
import BasePage from '@src/common/ducks/DetailPage';
import { configureAuthRule, getAuthRule } from '../model';
import { TAB } from '../../types';
import TagTableDuck from '../../tagTable/TagTableDuck';
import { notification } from 'tea-component';
import { AUTH_TYPE, AuthItem, TARGET } from '../types';
import router from '@src/common/util/router';
import { RULE_STATUS } from '@src/app/service/types';
import { nameTipMessage } from '@src/common/types';

enum Types {
  SET_CONDITION_LIST,
  SET_NAMESPACE_ID,
  SET_REGISTRY_ID,
  SET_SERVICE_NAME,
  SUBMIT,
  SET_CAN_SUBMIT,
  CANCEL,
  SUBMIT_FAIL,
}

interface ComposedId {
  namespaceId: string;
  serviceName: string;
  ruleId: string;
}

export default class CreatePage extends BasePage {
  ComposedId: ComposedId;
  Data: AuthItem;

  get baseUrl() {
    return '/service/service-auth-create';
  }

  get params() {
    return [
      ...super.params,
      {
        key: 'namespaceId',
        type: this.types.SET_NAMESPACE_ID,
        defaults: '',
      },
      {
        key: 'registryId',
        type: this.types.SET_REGISTRY_ID,
        defaults: '',
      },
      {
        key: 'serviceName',
        type: this.types.SET_SERVICE_NAME,
        defaults: '',
      },
    ];
  }

  get quickTypes() {
    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      form: MyForm,
      tagTable: TagTableDuck,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      conditionList: (state = [], action) => {
        if (action.type === types.SET_CONDITION_LIST) {
          return action.payload;
        }
        if (action.type === types.FETCH_DONE) {
          return action.payload?.tags || [];
        }
        return state;
      },
      namespaceId: reduceFromPayload(types.SET_NAMESPACE_ID, ''),
      registryId: reduceFromPayload(types.SET_REGISTRY_ID, ''),
      serviceName: reduceFromPayload(types.SET_SERVICE_NAME, ''),
      canSubmit: (state = true, action) => {
        if (action.type === types.SUBMIT) {
          return false;
        }
        if (action.type === types.SET_CAN_SUBMIT) {
          return !!action.payload;
        }
        if (action.type === types.SUBMIT_FAIL) {
          return true;
        }
        return state;
      },
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      conditionList: (state: State) => state.conditionList,
      composedId: (state: State) => {
        return {
          namespaceId: state.namespaceId,
          serviceName: state.serviceName,
          registryId: state.registryId,
          ruleId: state.id || '',
        };
      },
      canSubmit: (state: State) => state.canSubmit,
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      setList: payload => ({
        type: types.SET_CONDITION_LIST,
        payload,
      }),
      submit: () => ({
        type: types.SUBMIT,
      }),
      cancel: () => ({
        type: types.CANCEL,
      }),
    };
  }

  *saga() {
    yield* super.saga();
    const duck = this;
    const {
      ducks: { form },
      selectors,
      types,
    } = duck;

    yield put(
      form.creators.setValues({
        ruleName: '',
        status: false,
        ruleType: AUTH_TYPE.close,
        target: TARGET.all,
      }),
    );

    yield takeLatest(types.SUBMIT, function*() {
      try {
        // 判断表单是否有误, 否则错误提示不出
        yield put(form.creators.setAllTouched());

        const invalid = yield select(form.selectors.firstInvalid);
        if (invalid) return;

        // 向后台提交表单
        const { values } = yield select(form.selector);
        const conditionList = yield select(selectors.conditionList);
        const { ruleId, serviceName, namespaceId, registryId } = selectors.composedId(yield select());
        const { status } = values;

        const requestParams = {
          ...values,
          isEnabled: status ? RULE_STATUS.OPEN : RULE_STATUS.CLOSE,
          tags: conditionList,
          serviceName,
          namespaceId,
        };

        if (!ruleId) {
          delete requestParams.ruleId;
        }

        try {
          yield configureAuthRule(requestParams);
          notification.success({
            description: `${ruleId ? '更新' : '创建'}鉴权规则成功`,
          });

          yield delay(2000);
          // 跳转
          router.navigate(
            `/service/detail?id=${serviceName}&namespaceId=${namespaceId}&registryId=${registryId}&tab=${TAB.Auth}`,
          );
        } catch (e) {
          notification.error({
            description: `${ruleId ? '更新' : '创建'}鉴权规则失败, 请重试`,
          });
          yield put({ type: types.SUBMIT_FAIL });
        }
      } finally {
        yield put({ type: types.SET_CAN_SUBMIT, payload: true });
      }
    });

    // 跳转回首页
    yield takeLatest(types.CANCEL, function*() {
      const { serviceName, namespaceId, registryId } = selectors.composedId(yield select());
      router.navigate(
        `/service/detail?id=${serviceName}&namespaceId=${namespaceId}&registryId=${registryId}&tab=${TAB.Auth}`,
      );
    });

    // 设置表单初始值
    yield takeLatest(types.FETCH_DONE, function*() {
      const data = yield select(selectors.data);
      if (!data) return;

      yield put(
        form.creators.setValues({
          ...data,
          ruleName: data.ruleName || '',
          status: +data.isEnabled === RULE_STATUS.OPEN,
          ruleType: data.ruleType || AUTH_TYPE.close,
          target: data.target || TARGET.all,
        }),
      );
    });
  }

  async getData(composedId: this['ComposedId']) {
    const { ruleId, serviceName, namespaceId } = composedId;
    if (ruleId) {
      return getAuthRule({
        ruleId,
        serviceName,
        namespaceId,
      });
    }
    return {} as AuthItem;
  }
}

enum FormTypes {
  RULE_NAME_CHANGED,
}

interface RelateDeployFormValues {
  ruleName: string;
  status: boolean;
}

const validator = FormDuck.combineValidators<RelateDeployFormValues>({
  ruleName(v) {
    if (!v) {
      return '请输入规则名，' + nameTipMessage;
    }
    if (v?.length > 60 || !/^[a-z0-9]([-_a-z0-9]*[a-z0-9])?$/.test(v)) {
      return nameTipMessage;
    }
  },
});

class MyForm extends FormDuck {
  get quickTypes() {
    return {
      ...super.quickTypes,
      ...FormTypes,
    };
  }

  get actionMapping() {
    return {
      ...super.actionMapping,
      ruleName: this.types.RULE_NAME_CHANGED,
    };
  }

  validate(values: RelateDeployFormValues) {
    return validator(values || ({} as any));
  }
}
