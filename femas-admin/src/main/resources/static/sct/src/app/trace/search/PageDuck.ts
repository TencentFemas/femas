import { reduceFromPayload } from 'saga-duck';
import { call, put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import SearchFormDuck, { Values } from './SearchFormDuck';
import RequestDuck, { Filter as BaseFilter } from '@src/app/apm/requestDetail/PageDuck';
import getColumns from './getColumns';
import { describeSpans } from '../model';
import { HIGH_TRACE_LS, TRACE_LS_KEY, TraceParams } from '../types';
import moment from 'moment';
import { removeLsItem } from '@src/app/apm/utils';
import { delay } from 'redux-saga';

enum Types {
  LOAD,
  SET_SEARCH,
  SET_SEARCHED,
  EXEC_SEARCH,
  SET_INITED,
}

type Filter = BaseFilter;

export default class PageDuck extends RequestDuck {
  Filter: Filter;

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
      filter: (state: State) => ({
        page: state.page,
        count: state.count,
        keyword: state.keyword,
        searchParams: state.searchParams,
      }),
      search: (state: State) => state.search,
      searched: (state: State) => state.searched,
      inited: (state: State) => state.inited,
    };
  }

  *getColumns() {
    return getColumns();
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
        composedId: { namespaceId, needLS, tabActive, clearStorage },
      } = action.payload;
      const search = yield select(selectors.search);
      let showHigh = false;
      // 需要注意切换清空途径服务的场景
      // needLs为0 或者 TRACE_LS_KEY中没有localStorage对象时
      if (tabActive && (!+needLS || Object.values(TRACE_LS_KEY).every(d => !localStorage.getItem(d)))) {
        search.service = '';
      }
      //只取一次就清空
      if (+needLS === 1) {
        for (const key in TRACE_LS_KEY) {
          const itemValue = localStorage.getItem(TRACE_LS_KEY[key]);
          search[key] = itemValue === null ? search[key] || '' : itemValue;
        }
        clearStorage && removeLsItem(TRACE_LS_KEY);
      }
      Object.keys(search).forEach(key => {
        if (search[key] && HIGH_TRACE_LS.indexOf(TRACE_LS_KEY[key]) > -1) {
          showHigh = true;
        }
      });
      // 初始化searchForm数据
      yield form.initData({
        ...search,
        namespaceId,
        showHigh,
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
  }

  *handlerSearchParams(search: Values) {
    const { types } = this;
    const {
      startTime,
      endTime,
      service,
      traceId,
      callStatus,
      minDuration,
      maxDuration,
      api,
      tags,
      orderType,
      namespaceId,
    } = search;

    const params = {
      startTimestamp: +moment(startTime),
      endTimestamp: +moment(endTime),
      serviceName: service,
      traceId,
      endpointName: api,
      traceState: callStatus ? 'ERROR' : 'ALL',
      queryOrder: orderType,
      namespaceId,
    } as TraceParams;

    if (+minDuration) {
      params.minTraceDuration = +minDuration;
    }

    if (+maxDuration) {
      params.maxTraceDuration = +maxDuration;
    }
    // tags这里保证key存在就可
    params.tags = tags.filter(item => item.key);

    // 获取数据
    yield put({
      type: types.SET_SERACH_PARAMS,
      payload: params,
    });
  }

  async getData(filter: this['Filter']) {
    const { count, page, searchParams } = filter;
    const { startTimestamp, endTimestamp, ...rest } = searchParams as TraceParams;
    const params = {
      ...rest,
      queryDuration: {
        start: startTimestamp,
        end: endTimestamp,
      },
      paging: {
        pageSize: count,
        pageNum: page,
      },
    };

    const result = await describeSpans(params);
    return {
      totalCount: result.totalCount,
      list: result.list?.map((x, i) => ({
        ...x,
        startTimestamp,
        endTimestamp,
        id: `${x.traceId}-${i}`,
      })),
    };
  }
}
