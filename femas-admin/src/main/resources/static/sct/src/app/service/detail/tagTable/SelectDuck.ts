import { DuckMap as Base, reduceFromPayload } from 'saga-duck';
import { put } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { SYSTEM_TAG } from './types';
import { describeApiList, describeNamespaceList, describeServiceList } from './cacheableModel';

export default class SelectDuck extends Base {
  baseUrl = null;

  get quickTypes() {
    enum Types {
      LOAD,
      SET_DATA,
      SET_LOADING,
      SET_FULL_SERVICE_LIST,
      SET_FULL_API_LIST,
      SET_SERVICE_LIST_LOADING,
      SET_NAMESPACE_SERVICE_LIST,
      SET_NAMESPACE_LIST,
      LOAD_FULL_SERVICE_LIST,
      LOAD_FULL_API_LIST,
      LOAD_NAMESPACE_LIST,
      LOAD_NAMESPACE_SERVICE_LIST,
      LOADING,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      data: reduceFromPayload(types.SET_DATA, {}),
      apiList: reduceFromPayload(types.SET_FULL_API_LIST, []),
      serviceList: reduceFromPayload(types.SET_FULL_SERVICE_LIST, []),
      namespaceList: reduceFromPayload(types.SET_NAMESPACE_LIST, []),
      serviceListLoading: reduceFromPayload(types.SET_SERVICE_LIST_LOADING, false),
      serviceNameSpaceList: reduceFromPayload(types.SET_NAMESPACE_SERVICE_LIST, []),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      load: payload => ({
        type: types.LOAD,
        payload: payload,
      }),
      setLoading: item => ({ type: types.SET_LOADING, payload: item }),
      loadApiList: item => ({
        type: types.LOAD_FULL_API_LIST,
        payload: item,
      }),
      loadServiceList: item => ({
        type: types.LOAD_FULL_SERVICE_LIST,
        payload: item,
      }),
      loadServiceNamespaceList: item => ({
        type: types.LOAD_NAMESPACE_SERVICE_LIST,
        payload: item,
      }),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      data: (state: State) => state.data,
      apiList: (state: State) => state.apiList,
      serviceList: (state: State) => state.serviceList,
      serviceNamespaceList: (state: State) => state.serviceNameSpaceList,
      namespaceList: (state: State) => state.namespaceList,
      serviceListLoading: (state: State) => state.serviceListLoading,
    };
  }

  *saga() {
    yield* super.saga();
    const { types, creators } = this;
    yield takeLatest(types.LOAD, function*(action) {
      const { params, loadType } = action.payload;

      const LOAD_TYPE_MAP = {
        //服务与带命名空间的服务
        [SYSTEM_TAG.TSF_LOCAL_SERVICE]: types.LOAD_FULL_SERVICE_LIST,
        [SYSTEM_TAG.TSF_LOCAL_NAMESPACE_SERVICE]: types.LOAD_NAMESPACE_LIST,
        [SYSTEM_TAG.TSF_DEST_API_PATH]: types.LOAD_FULL_API_LIST,
      };

      if (LOAD_TYPE_MAP[loadType]) yield put({ type: LOAD_TYPE_MAP[loadType], payload: params });
      else yield put({ type: types.LOAD_FULL_SERVICE_LIST, payload: params });
    });

    yield takeLatest(types.LOAD_FULL_API_LIST, function*(action) {
      const { serviceName } = action.payload;
      const res = yield describeApiList({
        serviceName,
      });
      yield put({
        type: types.SET_FULL_API_LIST,
        payload:
          res.list?.map(item => ({
            ...item,
            text: item.path,
            value: item.path,
          })) || [],
      });
    });

    yield takeLatest(types.LOAD_NAMESPACE_LIST, function*(action) {
      const { namespaceId = '', registryId } = action.payload;
      // 拉取所有列表
      const res = yield describeNamespaceList({
        registryId,
      });
      yield put({
        type: types.SET_NAMESPACE_LIST,
        payload:
          res?.list?.map(item => ({
            ...item,
            text: item.name,
            value: item.namespaceId,
            tooltip: item.name,
          })) || [],
      });
      if (namespaceId) {
        yield put(creators.loadServiceNamespaceList({ namespaceId }));
      }
    });

    yield takeLatest(types.LOAD_FULL_SERVICE_LIST, function*(action) {
      const { namespaceId } = action.payload;
      yield put({ type: types.SET_SERVICE_LIST_LOADING, payload: true });
      const res = yield describeServiceList({ namespaceId });
      const serviceList =
        res?.list?.map(item => {
          return {
            value: item.serviceName,
            text: item.serviceName,
            tooltip: item.serviceName,
          };
        }) || [];
      yield put({ type: types.SET_FULL_SERVICE_LIST, payload: serviceList });
      yield put({ type: types.SET_SERVICE_LIST_LOADING, payload: false });
    });
    yield takeLatest(types.LOAD_NAMESPACE_SERVICE_LIST, function*(action) {
      const { namespaceId } = action.payload;
      yield put({ type: types.SET_SERVICE_LIST_LOADING, payload: true });
      const res = yield describeServiceList({
        namespaceId,
      });
      const serviceNamespaceList = res?.list?.map(item => {
        return {
          value: `${namespaceId}/${item.serviceName}`,
          text: item.serviceName,
          tooltip: item.serviceName,
        };
      });
      yield put({
        type: types.SET_NAMESPACE_SERVICE_LIST,
        payload: serviceNamespaceList,
      });
      yield put({ type: types.SET_SERVICE_LIST_LOADING, payload: false });
    });
  }
}
