import { reduceFromPayload } from 'saga-duck';
import { call, put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import SearchFormDuck, { Values } from './SearchFormDuck';
import BaseGridPage from '@src/app/apm/spanGrid/PageDuck';
import { HIGH_SPAN_LS, SPAN_LS_KEY, SpanParams, TRACE_LS_KEY } from '../types';
import { CallTypeMap, TagMap } from '@src/app/apm/types';
import { removeLsItem } from '@src/app/apm/utils';
import { delay } from 'redux-saga';
import moment from 'moment';

enum Types {
  LOAD,
  SET_SEARCH,
  SET_SEARCHED,
  EXEC_SEARCH,
  SET_INITED,
  INSPECT_DONE,
}

export default class PageDuck extends BaseGridPage {
  get quickTypes() {
    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      form: SearchFormDuck,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      search: reduceFromPayload(types.SET_SEARCH, {} as Values),
      searched: reduceFromPayload(types.SET_SEARCHED, false),
      inited: reduceFromPayload(types.SET_INITED, false),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      execSearch: () => ({ type: types.EXEC_SEARCH }),
      load: composedId => ({
        type: types.LOAD,
        payload: { composedId },
      }),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      search: (state: State) => state.search,
      searched: (state: State) => state.searched,
      inited: (state: State) => state.inited,
    };
  }

  *saga() {
    yield* super.saga();
    const {
      types,
      selectors,
      ducks: { grid, form },
    } = this;
    const duck = this;
    yield takeLatest(types.LOAD, function*(action) {
      const {
        composedId: { namespaceId, needLS, tabActive },
      } = action.payload;
      const search = yield select(selectors.search);
      yield put({ type: types.SET_SEARCHED, payload: false });

      let showHigh = false;
      //只取一次就清空
      if (+needLS === 1) {
        for (const key in SPAN_LS_KEY) {
          const itemValue = localStorage.getItem(SPAN_LS_KEY[key]);
          search[key] = itemValue === null ? search[key] || '' : itemValue;
          if (SPAN_LS_KEY[key] === SPAN_LS_KEY.tags && itemValue) {
            search.tags = JSON.parse(itemValue) || [];
          }
          if (SPAN_LS_KEY[key] === SPAN_LS_KEY.kind && itemValue) {
            search.tags = (search.tags?.length ? search.tags : []).concat([
              {
                key: TagMap.kind,
                value: search.kind,
              },
            ]);
          }
        }
        removeLsItem(SPAN_LS_KEY);
      }
      Object.keys(search).forEach(key => {
        if (search[key] && HIGH_SPAN_LS.indexOf(SPAN_LS_KEY[key]) > -1) {
          showHigh = true;
        }
      });

      // 初始化searchForm数据
      yield form.initData({
        ...search,
        showHigh,
        namespaceId,
      });
      yield put({ type: types.SET_INITED, payload: true });
      yield delay(100);
      if (tabActive) {
        yield put({ type: types.EXEC_SEARCH, payload: search });
      }
    });
    yield takeLatest(types.EXEC_SEARCH, function*() {
      const firstInvalid = yield select(form.selectors.firstInvalid);

      if (firstInvalid) {
        yield put(form.creators.setAllTouched());
        throw firstInvalid;
      }
      const search = form.selectors.values(yield select());
      if (!search) return;

      // 处理查询条件
      yield put({
        type: types.SET_SEARCH,
        payload: search,
      });
      yield call([duck, duck.handlerSearchParams], search);
    });
    yield takeLatest([grid.types.FETCH_DONE, grid.types.FETCH_FAIL], function*() {
      yield put({ type: types.SET_SEARCHED, payload: true });
    });
    yield takeLatest(types.INSPECT, function*(action) {
      const item = action.payload;
      const search = form.selectors.values(yield select());
      const { startTime, endTime } = search;
      yield put({ type: types.INSPECT_DONE });
      localStorage.setItem(TRACE_LS_KEY.traceId, item.traceId);
      localStorage.setItem(TRACE_LS_KEY.startTime, startTime);
      localStorage.setItem(TRACE_LS_KEY.endTime, endTime);
    });
  }

  *handlerSearchParams(search: Values) {
    const { types } = this;
    const {
      startTime,
      endTime,
      client,
      server,
      callStatus,
      minDuration,
      maxDuration,
      clientApi,
      serverApi,
      callType,
      statusCode,
      tags,
      clientInstanceIp,
      serverInstanceIp,
      traceId,
      spanId,
    } = search;

    const params = {
      filterTime: {
        startTime: moment(startTime).valueOf(),
        endTime: moment(endTime).valueOf(),
      },
      clientService: client,
      serverService: server,
      clientOperation: clientApi,
      serverOperation: serverApi,
      clientInstanceIp,
      serverInstanceIp,
      traceId,
      spanId,
      notNeedFilterTag: true,
    } as SpanParams;

    if (+minDuration) {
      params.durationMin = +minDuration;
    }

    if (+maxDuration) {
      params.durationMax = +maxDuration;
    }

    params.tags = [
      ...[
        { key: TagMap.type, value: callType },
        {
          key: callType === CallTypeMap.http ? TagMap.httpStatus : TagMap.rpcStatus,
          value: statusCode,
        },
        { key: TagMap.status, value: callStatus ? 'false' : '' },
      ].filter(item => item.value),
      // tags这里保证key存在就可
      ...tags.filter(item => item.key),
    ];

    // 获取数据
    yield put({
      type: types.SET_SERACH_PARAMS,
      payload: params,
    });
  }
}
