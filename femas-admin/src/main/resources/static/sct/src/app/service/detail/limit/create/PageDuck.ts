import { reduceFromPayload } from 'saga-duck';
import { put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import Base from '@src/common/ducks/DetailPage';
import BaseForm from '@src/common/ducks/Form';
import { validator as baseValidator } from './validator';
import TagTableDuck from '../../tagTable/TagTableDuck';
import { ComposedId, TAB } from '../../types';
import { configureLimitRule, fetchLimitRuleById, fetchServiceLimitList } from '../model';
import { notification } from 'tea-component';
import { LIMIT_RANGE, LimitRuleItem, rangeKey, rulesKey, SOURCE, sourceKey, statusKey } from '../types';
import router from '@src/common/util/router';
import { RULE_STATUS } from '@src/app/service/types';

export default class Duck extends Base {
  ComposedId: ComposedId;
  Data: LimitRuleItem;

  get baseUrl() {
    return '/service/service-limit-create';
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      form: Form,
      tagTable: TagTableDuck,
    };
  }

  get params() {
    return [
      ...super.params,
      {
        key: 'namespaceId',
        route: 'namespaceId',
        defaults: '',
        type: this.types.SET_NAMESPACE_ID,
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
        type: this.types.SET_SERVICE_NAME,
      },
    ];
  }

  get quickTypes() {
    enum Types {
      SET_NAMESPACE_ID,
      SET_REGISTRY_ID,
      SUMBIT,
      CANCEL,
      SET_SERVICE_NAME,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducers() {
    return {
      ...super.reducers,
      namespaceId: reduceFromPayload(this.types.SET_NAMESPACE_ID, ''),
      serviceName: reduceFromPayload(this.types.SET_SERVICE_NAME, ''),
      registryId: reduceFromPayload(this.types.SET_REGISTRY_ID, ''),
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
      composedId: (state: State) => ({
        namespaceId: state.namespaceId,
        registryId: state.registryId,
        serviceName: state.serviceName,
        ruleId: state.id,
      }),
    };
  }

  *saga() {
    yield* super.saga();
    const { types, ducks, selectors } = this;
    const { form } = ducks;
    yield takeLatest(types.SUMBIT, function*() {
      // 校验
      const firstInvalid = yield select(form.selectors.firstInvalid);
      if (firstInvalid) {
        yield put(form.creators.setAllTouched());
        return false;
      }
      const values = yield select(form.selectors.values);

      const { namespaceId, ruleId, serviceName, registryId } = selectors.composedId(yield select());
      const { tags, duration, totalQuota, ruleName, status, type, desc } = values;

      const params = {
        namespaceId,
        serviceName,
        ruleId,
        duration,
        totalQuota,
        type,
        ruleName,
        status: status ? RULE_STATUS.OPEN : RULE_STATUS.CLOSE,
        tags,
        desc,
      };
      if (!ruleId) {
        delete params.ruleId;
      }
      try {
        yield configureLimitRule(params);

        notification.success({
          description: `${ruleId ? '编辑' : '创建'}成功`,
        });
        router.navigate(
          `/service/detail?id=${serviceName}&namespaceId=${namespaceId}&registryId=${registryId}&tab=${TAB.Limit}`,
        );
      } catch (e) {
        notification.error({ description: `${ruleId ? '编辑' : '创建'}失败` });
      }
    });

    // 设置表单初始值
    yield takeLatest(types.FETCH_DONE, function*() {
      const data = yield select(selectors.data);
      if (!data) return;
      const composedId = yield select(selectors.composedId);
      if (!composedId.ruleId) {
        yield put(
          form.creators.setValues({
            [sourceKey]: data.hasGlobalRule ? SOURCE.PART : SOURCE.ALL,
            [rangeKey]: LIMIT_RANGE.SINGLE,
            [statusKey]: 0,
            [rulesKey]: [],
          }),
        );
        return;
      }
      yield put(
        form.creators.setValues({
          ...data,
          [statusKey]: +data.status === RULE_STATUS.OPEN,
          [rangeKey]: LIMIT_RANGE.SINGLE,
        }),
      );
    });
  }

  async getData(composedId: this['ComposedId']) {
    const { ruleId, serviceName, namespaceId } = composedId;
    if (ruleId) {
      return fetchLimitRuleById({
        ruleId,
        serviceName,
        namespaceId,
      });
    }
    const res = await fetchServiceLimitList({
      pageNo: 1,
      pageSize: 1,
      namespaceId,
      serviceName,
      type: SOURCE.ALL,
    });
    return { hasGlobalRule: !!res.totalCount } as LimitRuleItem;
  }
}

const validator = BaseForm.combineValidators<{}>(baseValidator);

class Form extends BaseForm {
  Value: {};
  Meta: {
    tagTable: any;
  };

  validate(v) {
    return validator(v || {});
  }
}
