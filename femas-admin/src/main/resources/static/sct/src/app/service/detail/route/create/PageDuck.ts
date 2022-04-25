import { reduceFromPayload } from 'saga-duck';
import { put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import BasePage from '@src/common/ducks/DetailPage';
import BaseForm from '@src/common/ducks/Form';
import { validator as baseValidator } from './validator';
import TagTableDuck from '../../tagTable/TagTableDuck';
import TargetGridDuck from './TargetGridDuck';
import { ComposedId, TAB } from '../../types';
import { notification } from 'tea-component';
import { configureRouteRule, fetchRouteRuleById } from '../model';
import { RouteRuleItem } from '../types';
import router from '@src/common/util/router';
import { RULE_STATUS } from '@src/app/service/types';

enum Types {
  SET_SERVICE_NAME,
  SET_NAMESPACE_ID,
  SET_REGISTRY_ID,
  SUMBIT,
  CANCEL,
}

export default class Duck extends BasePage {
  ComposedId: ComposedId;
  Data: RouteRuleItem;

  get baseUrl() {
    return '/service/service-route-create';
  }

  get params() {
    const { types } = this;
    return [
      ...super.params,
      {
        key: 'namespaceId',
        route: 'namespaceId',
        defaults: '',
        type: types.SET_NAMESPACE_ID,
      },
      {
        key: 'registryId',
        type: this.types.SET_REGISTRY_ID,
        defaults: '',
      },
      {
        key: 'serviceName',
        route: 'serviceName',
        defaults: '',
        type: types.SET_SERVICE_NAME,
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
      form: Form,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      serviceName: reduceFromPayload(types.SET_SERVICE_NAME, ''),
      namespaceId: reduceFromPayload(types.SET_NAMESPACE_ID, ''),
      registryId: reduceFromPayload(types.SET_REGISTRY_ID, ''),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      submit: () => ({ type: types.SUMBIT }),
      cancel: () => ({ type: types.CANCEL }),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      composedId: (state: State) => {
        return {
          namespaceId: state.namespaceId,
          serviceName: state.serviceName,
          registryId: state.registryId,
          ruleId: state.id || '',
        };
      },
    };
  }

  async getData(composedId: this['ComposedId']) {
    const { ruleId, serviceName, namespaceId } = composedId;
    if (ruleId) {
      return fetchRouteRuleById({
        ruleId,
        serviceName,
        namespaceId,
      });
    }
    return {} as RouteRuleItem;
  }

  *saga() {
    yield* super.saga();
    yield* this.sagaOnSubmit();
  }

  *sagaOnSubmit() {
    const {
      types,
      ducks: { form },
      selectors,
    } = this;
    yield takeLatest(types.SUMBIT, function*() {
      const { namespaceId, serviceName, ruleId, registryId } = selectors.composedId(yield select());
      const firstInvalid = yield select(form.selectors.firstInvalid);
      const values = yield select(form.selectors.values);
      if (firstInvalid) {
        yield put(form.creators.setAllTouched());
        return false;
      }
      const { ruleName, tagRules: routeTag, status } = values;
      const params = {
        namespaceId,
        serviceName,
        ruleId,
        ruleName,
        status: status ? RULE_STATUS.OPEN : RULE_STATUS.CLOSE,
        routeTag,
      };
      if (!ruleId) {
        delete params.ruleId;
      }
      // 根据ruleID 判断是否为更新
      try {
        yield configureRouteRule(params);
        notification.success({
          description: `${ruleId ? '编辑' : '创建'}成功`,
        });
        router.navigate(
          `/service/detail?id=${serviceName}&namespaceId=${namespaceId}&registryId=${registryId}&tab=${TAB.Route}`,
        );
      } catch (e) {
        notification.error({
          description: `${ruleId ? '编辑' : '创建'}失败`,
        });
      }
    });
    // 设置表单初始值
    yield takeLatest(types.FETCH_DONE, function*() {
      const { ruleId } = selectors.composedId(yield select());
      const data = selectors.data(yield select());
      // 根据ID判断是否为编辑
      if (ruleId) {
        const { routeTag } = data;
        yield put(
          form.creators.setValues({
            ...data,
            tagRules: routeTag,
            status: +data.status === RULE_STATUS.OPEN,
          }),
        );
        return;
      }
      yield put(
        form.creators.setValues({
          tagRules: [
            {
              tags: [],
              destTag: [],
            },
          ],
        }),
      );
    });
  }
}

const validator = BaseForm.combineValidators<{}>(baseValidator);

class Form extends BaseForm {
  Value: {};
  Meta: {
    tagTable: any;
  };

  get quickDucks() {
    return {
      ...super.quickDucks,
      tagTable: TagTableDuck,
      targetGrid: TargetGridDuck,
    };
  }

  validate(v) {
    return validator(v || {});
  }
}
