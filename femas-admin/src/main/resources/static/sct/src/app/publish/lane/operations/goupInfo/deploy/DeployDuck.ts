import NamespaceSelectDuck from "@src/app/namespace/components/NamespaceSelectDuck";
import { call, select, put } from "redux-saga/effects";
import { DuckMap, reduceFromPayload } from "saga-duck";
import { describeServiceInstanceByNsId } from "./model";
// import Form from "@src/common/ducks/Form";
import { takeLatest } from "redux-saga-catch";

export default class DeployDuck extends DuckMap {
  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      serviceList: reduceFromPayload(types.FETCH, []),
    };
  }

  get quickTypes() {
    enum Types {
      FETCH,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      namespace: NamespaceSelectDuck,
    };
  }

  get rawSelectors() {
    type State = this["State"];
    return {
      ...super.rawSelectors,
      serviceList: (state: State) => state.serviceList,
    };
  }

  get watchTypes(): string[] {
    const { types } = this;
    return [];
  }

  *saga() {
    const {
      ducks: { namespace },
      types,
    } = this;
    yield* namespace.load(null);
    yield* super.saga();

    yield takeLatest(namespace.types.SET_SELECTED, function*() {
      const namespaceId = namespace.selectors.id(yield select());
      if (!namespaceId) return;
      const namespaceItem = yield namespace.getSelectedItem();

      const result = yield call(describeServiceInstanceByNsId, {
        id: namespaceId,
      });

      yield put({
        type: types.FETCH,
        payload: result.map((v) => ({
          namespaceName: namespaceItem.name,
          namespaceId: v.namespaceId,
          serviceName: v.serviceName,
          version: v.version,
        })),
      });
    });
  }
}
