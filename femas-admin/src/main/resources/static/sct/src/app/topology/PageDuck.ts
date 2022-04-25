import PageDuck from '@src/common/ducks/Page';
import NamespaceSelectDuck from '../namespace/components/NamespaceSelectDuck';
import { call, put, select, takeLatest } from 'redux-saga/effects';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import TopologyDuck from '@src/app/apm/topology/PageDuck';
import { watchLatest } from '@src/common/helpers/saga';
import { delay } from 'redux-saga';
import { generateNodeId } from '../apm/topology/util';
import { Action } from '@src/common/types';

const TRACE_SELECTED = 'select_trace';

const EMPTY_SERVICE = [{ text: '全部服务', value: '' }];

const TRACE_ITEM = { text: '未选择服务', value: TRACE_SELECTED };

interface Service {
  type?: string;
  value: string;
  sourceId?: string;
  targetId?: string;
}

export default class ConfigPageDuck extends PageDuck {
  get baseUrl() {
    return '/topology';
  }

  get params() {
    return [
      ...super.params,
      this.ducks.namespace.getRegistryParam(),
      {
        key: 'startTime',
        type: this.types.SET_START_TIME,
        defaults: '',
      },
      {
        key: 'endTime',
        type: this.types.SET_END_TIME,
        defaults: '',
      },
    ];
  }

  get quickTypes() {
    enum Types {
      FETCH,
      FETCH_DONE,
      FETCH_FAIL,
      SET_FILTER_TIME,
      SET_START_TIME,
      SET_END_TIME,
      RELOAD_TOPOLOGY,
      SET_SERVICE_LIST,
      CHANGE_SERVICE,
      SET_CUR_SERVICE,
      ON_SVG_CLICK,
      SET_TOPO_CUR_DATA,
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
      topology: TopologyDuck,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      startTime: (state = null, action) => {
        switch (action.type) {
          case types.SET_FILTER_TIME:
            return action.payload.startTime;
          case types.SET_START_TIME:
            return action.payload;
          default:
            return state;
        }
      },
      endTime: (state = null, action) => {
        switch (action.type) {
          case types.SET_FILTER_TIME:
            return action.payload.endTime;
          case types.SET_END_TIME:
            return action.payload;
          default:
            return state;
        }
      },
      serviceList: reduceFromPayload(types.SET_SERVICE_LIST, EMPTY_SERVICE),
      curService: reduceFromPayload(types.SET_CUR_SERVICE, {
        value: '',
      } as Service),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      setFilterTime: createToPayload(types.SET_FILTER_TIME),
      changeService: createToPayload(types.CHANGE_SERVICE),
      onSvgClick: createToPayload(types.ON_SVG_CLICK),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      startTime: (state: State) => state.startTime,
      endTime: (state: State) => state.endTime,
      namespaceId: (state: State) => state.namespace.id,
      serviceList: (state: State) => state.serviceList,
      curService: (state: State) => state.curService,
    };
  }

  *saga() {
    yield* this.sagaInitLoad();
    yield* super.saga();
    const {
      ducks: { namespace, topology },
      types,
      selectors,
    } = this;
    // 处理命名空间list为空的情况
    yield takeLatest(namespace.types.SET_TOTAL_COUNT, function*(action: Action<number>) {
      const length = action.payload;
      if (length) return;
      yield put({ type: types.FETCH_DONE, payload: null });
    });
    yield takeLatest(types.RELOAD_TOPOLOGY, function*() {
      const namespaceId = yield select(namespace.selectors.id);
      const startTime = yield select(selectors.startTime);
      const endTime = yield select(selectors.endTime);

      // 加载依赖拓扑图
      yield put(
        topology.creators.setSearchParams({
          startTime,
          endTime,
          namespaceId,
        }),
      );
    });
    yield watchLatest(
      [types.SET_FILTER_TIME, namespace.types.SET_SELECTED],
      state => {
        return {
          namespaceId: state.namespace.id,
          startTime: state.startTime,
          endTime: state.endTime,
        };
      },
      function*() {
        yield call(delay, 200);
        const startTime = yield select(selectors.startTime);
        const endTime = yield select(selectors.endTime);
        if (!startTime || !endTime) {
          return;
        }
        yield put({ type: types.SET_START_TIME, payload: startTime });
        yield put({ type: types.SET_END_TIME, payload: endTime });

        // 获取依赖拓扑图数据
        yield put({ type: types.RELOAD_TOPOLOGY });
      },
    );
    // 根据拓扑图数据获取service列表
    yield takeLatest(topology.types.FETCH_DONE, function*() {
      const data = topology.selectors.data(yield select());
      yield put({
        type: types.SET_SERVICE_LIST,
        payload: !data
          ? EMPTY_SERVICE
          : [
              ...EMPTY_SERVICE,
              ...(data?.nodes?.map(item => ({
                ...item,
                text: item.name,
                value: generateNodeId(item.namespaceId, item.name),
              })) || []),
            ],
      });
      // 重置选择
      yield put({ type: types.SET_CUR_SERVICE, payload: { value: '' } });
      yield put({
        type: topology.types.SET_CUR_DATA,
        payload: {},
      });
    });

    // 选择service
    yield takeLatest(types.CHANGE_SERVICE, function*(action: Action<any>) {
      const curService = action.payload;
      const serviceList = selectors.serviceList(yield select());
      if (serviceList[0].value === TRACE_SELECTED) {
        serviceList.shift();
      }
      yield put({ type: types.SET_SERVICE_LIST, payload: [...serviceList] });
      yield put({ type: types.SET_CUR_SERVICE, payload: curService });
      yield put({
        type: types.SET_TOPO_CUR_DATA,
      });
    });

    // 点击 更改service
    yield takeLatest(types.ON_SVG_CLICK, function*(action: Action<any>) {
      const event = action.payload;
      // svg  image-节点 path-连线
      const nodeName = event?.target?.nodeName;
      if (!nodeName) return;

      let curService = { value: '' } as any;
      const serviceList = selectors.serviceList(yield select());
      if (serviceList[0].value === TRACE_SELECTED) {
        serviceList.shift();
      }

      if (nodeName === 'image') {
        curService = { value: event.target.id.slice(6) };
      }

      if (nodeName === 'path' || nodeName === 'textPath') {
        const [targetId, sourceId] = event.target.id.split('traceTsf-').reverse();
        serviceList.unshift(TRACE_ITEM);
        curService = {
          type: 'trace',
          value: TRACE_ITEM.value,
          targetId,
          sourceId,
        };
      }
      yield put({ type: types.SET_SERVICE_LIST, payload: [...serviceList] });
      yield put({ type: types.SET_CUR_SERVICE, payload: curService });
    });
    // 设置图高亮数据
    yield takeLatest(types.SET_TOPO_CUR_DATA, function*(action: Action<boolean>) {
      const reset = action.payload;
      const curService = selectors.curService(yield select());
      yield put({
        type: topology.types.SET_CUR_DATA,
        payload: reset
          ? {}
          : {
              curTarget: curService.type === 'trace' ? curService.targetId : curService.value,
              curSource: curService.type === 'trace' ? curService.sourceId : '',
            },
      });
    });
  }

  *sagaInitLoad() {
    const { ducks } = this;
    yield* ducks.namespace.load(null);
  }
}
