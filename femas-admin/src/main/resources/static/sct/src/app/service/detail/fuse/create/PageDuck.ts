import NamespaceSelectDuck from '@src/app/namespace/components/NamespaceSelectDuck';
import ServiceSelectDuck from '@src/app/service/components/ServiceSelectDuck';
import Base from '@src/common/ducks/DetailPage';
import CreateForm from './FormDuck';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { watchLatest } from '@src/common/helpers/saga';
import { FuseRuleItem, ISOLATION_TYPE } from '../types';
import Fetcher from '@src/common/ducks/Fetcher';
import { configureBreakerRule, fetchBreakerRuleById } from '../model';
import router from '@src/common/util/router';
import { TAB } from '../../types';

interface ComposedId {
  registryId: string;
  namespaceId: string;
  serviceName: string;
  ruleId?: string;
}

export type Data = Pick<
  FuseRuleItem,
  'isolationLevel' | 'namespaceId' | 'serviceName' | 'targetServiceName' | 'targetNamespaceId' | 'strategy'
>;

export default class PageDuck extends Base {
  ComposedId: ComposedId;
  Data: Data;

  get baseUrl() {
    return '/service/fuce-create';
  }

  get defaultStrategy() {
    return {
      api: [],
      slidingWindowSize: 10,
      minimumNumberOfCalls: 10,
      failureRateThreshold: 50,
      slowCallDurationThreshold: 6e4,
      slowCallRateThreshold: 50,
      maxEjectionPercent: 50,
      waitDurationInOpenState: 60,
    };
  }

  get quickTypes() {
    enum Types {
      SET_REGISTRY_ID,
      SET_NAMESPACE_ID,
      SET_SERVICE,
      SET_RULE_ID,
      SUBMIT,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get params() {
    const { types } = this;
    return [
      ...super.params,
      {
        key: 'registryId',
        type: types.SET_REGISTRY_ID,
        defaults: '',
      },
      {
        key: 'namespaceId',
        type: types.SET_NAMESPACE_ID,
        defaults: '',
      },
      {
        key: 'service',
        type: types.SET_SERVICE,
        defaults: '',
      },
      {
        key: 'ruleId',
        type: types.SET_RULE_ID,
        defaults: '',
      },
    ];
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      form: CreateForm,
      namespace: MyNamespaceSelectDuck,
      service: ServiceSelectDuck,
      submit: SubmitFetcher,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      registryId: reduceFromPayload(types.SET_REGISTRY_ID, ''),
      namespaceId: reduceFromPayload(types.SET_NAMESPACE_ID, ''),
      serviceName: reduceFromPayload(types.SET_SERVICE, ''),
      ruleId: reduceFromPayload(types.SET_RULE_ID, ''),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      submit: createToPayload<void>(types.SUBMIT),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      composedId: (state: State) => ({
        registryId: state.registryId,
        namespaceId: state.namespaceId,
        serviceName: state.serviceName,
        ruleId: state.ruleId,
      }),
    };
  }

  *saga() {
    yield* super.saga();
    const { types, ducks } = this;
    const duck = this;
    yield* this.routeInitialized();
    yield* this.initForm();
    yield* this.initSelect();
    yield watchLatest(
      [types.SET_NAMESPACE_ID, ducks.namespace.types.SELECT],
      state => ducks.namespace.selector(state).id,
      function*() {
        const { id: namespaceId } = ducks.namespace.selector(yield select());
        yield* ducks.service.load({
          namespaceId,
        });
      },
    );
    yield watchLatest(
      [ducks.service.types.SELECT],
      state => ducks.service.selector(state).id,
      function*() {
        const { id: serviceName } = ducks.service.selector(yield select());
        yield put(ducks.form.creators.setValue('targetServiceName', serviceName));
      },
    );
    yield takeLatest(types.SUBMIT, function*() {
      const firstInvalid = ducks.form.selectors.firstInvalid(yield select());
      if (firstInvalid) {
        yield put(ducks.form.creators.setAllTouched());
        return;
      }
      const { namespaceId, serviceName, registryId } = duck.selectors.composedId(yield select());
      const ruleItem = ducks.form.selectors.values(yield select());
      const targetNamespaceId = ducks.namespace.selectors.id(yield select());
      if (ruleItem.isolationLevel !== ISOLATION_TYPE.API) {
        ruleItem.strategy = ruleItem.strategy.map(item => ({
          ...item,
          api: undefined,
        }));
      }
      if (ruleItem.isolationLevel !== ISOLATION_TYPE.INSTANCE) {
        ruleItem.strategy = ruleItem.strategy.map(item => ({
          ...item,
          maxEjectionPercent: undefined,
        }));
      }
      yield* ducks.submit.fetch({
        namespaceId,
        serviceName,
        targetNamespaceId,
        ...ruleItem,
      });
      router.navigate(
        `/service/detail?id=${serviceName}&namespaceId=${namespaceId}&registryId=${registryId}&tab=${TAB.Fuse}`,
      );
    });
  }

  *initSelect() {
    const { ducks, selectors } = this;

    const { registryId, namespaceId } = selectors.composedId(yield select());
    yield* ducks.namespace.load({
      registryId,
    });
    yield* ducks.service.load({
      namespaceId,
    });
  }

  *initForm() {
    const { ducks, selectors, types } = this;
    yield takeLatest(types.FETCH_DONE, function*() {
      const data = selectors.data(yield select());
      yield put(ducks.form.creators.setValues(data));
      yield put(ducks.namespace.creators.select(data.targetNamespaceId));
      yield put(ducks.service.creators.select(data.targetServiceName));
    });
  }

  async getData(params: ComposedId) {
    const { namespaceId, serviceName, ruleId } = params;
    if (!ruleId) {
      return {
        namespaceId,
        serviceName,
        targetNamespaceId: namespaceId,
        targetServiceName: serviceName,
        isolationLevel: ISOLATION_TYPE.SERVICE,
        strategy: [this.defaultStrategy],
      };
    }
    const res = await fetchBreakerRuleById({
      namespaceId,
      serviceName,
      ruleId,
    });
    res.strategy.forEach(item => {
      item.api = item.api?.map(item => ({
        id: `${item.path}(${item.method})`,
        ...item,
      }));
    });
    return res;
  }
}

class MyNamespaceSelectDuck extends NamespaceSelectDuck {
  get syncLocalStorage() {
    return false;
  }

  get lskey() {
    return null;
  }
}

class SubmitFetcher extends Fetcher {
  Data: boolean;
  Param: Data;

  async getDataAsync(param: Data) {
    return configureBreakerRule(param);
  }
}
