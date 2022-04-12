import { put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { BIG_ICON_MAP, BIG_ICON_NAME, NodeCalMode, SERVICE_TYPE, TraceShowMode } from './types';
import { formatLargeNumber, formatTime, generateNodeId } from './util';
import { LayoutType } from '@src/common/qcm-chart';
import PageDuck from '@src/common/ducks/Page';
import { getTopologyGraph } from './model';
import { labelColors, lineColors } from '@src/common/qcm-chart/theme/theme';
import { fetchMetricGrafanaAddress } from '../model';

enum Types {
  FETCH,
  FETCH_DONE,
  FETCH_FAIL,
  RELOAD_TOPOLOGY,
  SET_CUR_DATA,
  SET_LOADING,
  SET_SEARCH_PARAMS,
  SET_LAYOUT_TYPE,
  SET_NODE_CAL_MODE,
  SET_TRACE_SHOW_MODE,
  SET_GRAFANAURL,
}

interface SearchParams {
  namespaceId: string;
  startTime: string;
  endTime: string;
  serviceName?: string;
}

interface CurData {
  curTarget: string;
  curSource: string;
}

export default class GridPageDuck extends PageDuck {
  baseUrl = null;

  get quickTypes() {
    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      loading: (state = true, action) => {
        switch (action.type) {
          case types.FETCH_DONE:
            return false;
          case types.SET_LOADING:
            return action.payload;
          default:
            return state;
        }
      },
      data: (state = null, action) => {
        switch (action.type) {
          case types.FETCH_DONE:
            return action.payload;
          default:
            return state;
        }
      },
      curData: reduceFromPayload(types.SET_CUR_DATA, {} as CurData),
      searchParams: reduceFromPayload(types.SET_SEARCH_PARAMS, {} as SearchParams),
      layoutType: reduceFromPayload(types.SET_LAYOUT_TYPE, LayoutType.dagre),
      nodeCalMode: reduceFromPayload(types.SET_NODE_CAL_MODE, NodeCalMode.requestCount),
      traceShowMode: reduceFromPayload(types.SET_TRACE_SHOW_MODE, TraceShowMode.none),
      grafanaUrl: reduceFromPayload(types.SET_GRAFANAURL, ''),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      setSearchParams: createToPayload(types.SET_SEARCH_PARAMS),
      setLayoutType: createToPayload(types.SET_LAYOUT_TYPE),
      changeNodeCalMode: createToPayload(types.SET_NODE_CAL_MODE),
      changeTraceShowMode: createToPayload(types.SET_TRACE_SHOW_MODE),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      loading: (state: State) => state.loading,
      data: (state: State) => state.data,
      curData: (state: State) => state.curData,
      searchParams: (state: State) => state.searchParams,
      layoutType: (state: State) => state.layoutType,
      nodeCalMode: (state: State) => state.nodeCalMode,
      traceShowMode: (state: State) => state.traceShowMode,
      grafanaUrl: (state: State) => state.grafanaUrl,
    };
  }

  *saga() {
    yield* super.saga();
    const { types, selectors } = this;
    yield takeLatest(types.SET_SEARCH_PARAMS, function*() {
      // 清空数据
      yield [put({ type: types.FETCH_DONE, payload: null }), put({ type: types.RELOAD_TOPOLOGY })];
      const grafanaUrl = yield fetchMetricGrafanaAddress();
      yield put({ type: types.SET_GRAFANAURL, payload: grafanaUrl });
    });
    // 切换布局，清空curData
    yield takeLatest(types.SET_LAYOUT_TYPE, function*() {
      yield put({ type: types.SET_CUR_DATA, payload: {} });
    });
    yield takeLatest(types.RELOAD_TOPOLOGY, function*() {
      const { namespaceId, startTime, endTime, serviceName } = selectors.searchParams(yield select());
      const filter = {
        startTime,
        endTime,
        namespaceId,
        serviceName,
      };

      yield put({ type: types.SET_CUR_DATA, payload: {} });
      yield put({ type: types.SET_LOADING, payload: true });
      let data = null;
      if (namespaceId) {
        const { nodes, calls } = (yield getTopologyGraph(filter)) || {};
        if (nodes?.length) {
          data = {
            nodes:
              nodes?.map(n => {
                const type = n.type?.toLowerCase() || '';
                return {
                  ...n,
                  type,
                  id: generateNodeId(n.namespaceId, n.name),
                  disabled: n.requestVolume === null && n.errorRate === null && n.type !== BIG_ICON_NAME.EXTERNAL,
                  nodeCenterContent: d =>
                    n.type === BIG_ICON_NAME.EXTERNAL
                      ? []
                      : [`${formatLargeNumber(d.requestVolume)}次`, `Avg. ${formatTime(d.averageDuration)}`],
                  imgHref:
                    n.type === BIG_ICON_NAME.EXTERNAL
                      ? ''
                      : BIG_ICON_MAP[type] || (type !== SERVICE_TYPE ? BIG_ICON_MAP[BIG_ICON_NAME.COMMON] : ''),
                  nodeShowText: data => (type === SERVICE_TYPE || !BIG_ICON_MAP[type] ? data.name : ''),
                };
              }) || [],
            traces:
              calls?.map(d => {
                const source = nodes.find(x => x.id === d.source);
                const target = nodes.find(x => x.id === d.target);
                return {
                  ...d,
                  sourceId: generateNodeId(source.namespaceId, source.name),
                  targetId: generateNodeId(target.namespaceId, target.name),
                  linkColor: lineColors[0],
                  labelColor: labelColors[0],
                };
              }) || [],
            id: new Date().getTime(),
          };
        }
      }
      yield put({ type: types.FETCH_DONE, payload: data });
    });
  }
}
