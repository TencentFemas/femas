import Base from '@src/common/ducks/DetailPage';
import { put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { fetchTraceDetail } from '../model';
import SpanDetailDuck from './SpanDetailDuck';
import { createToPayload, reduceFromPayload } from 'saga-duck';

enum Types {
  SET_SPAN_DETAIL,
  SET_LOADING,
  SHOW_LOG_PANEL,
  SET_COMPOSED_ID,
  SET_LEVEL_IDS,
  TOGGLE_FOLD,
}

interface ComposedId {
  traceId: string;
  startTime: string;
  endTime: string;
}

export interface Data {
  startTime: string;
  duration: number;
  serviceNum: number;
  spanNum: number;
  depth: number;
  list: Array<any>;
}

export default class DetailGridDuck extends Base {
  baseUrl = null;
  Data: Data;
  ComposedId: ComposedId;

  get watchTypes() {
    return [...super.watchTypes, this.types.SET_COMPOSED_ID];
  }

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
      loading: reduceFromPayload(types.SET_LOADING, true),
      composedId: reduceFromPayload(types.SET_COMPOSED_ID, {} as ComposedId),
      levelIds: reduceFromPayload(types.SET_LEVEL_IDS, []),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      composedId: (state: State) => state.composedId,
      levelIds: (state: State) => state.levelIds,
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      loadData: createToPayload(types.SET_COMPOSED_ID),
      showSpanDetail: createToPayload(types.SET_SPAN_DETAIL),
      showSpanLog: createToPayload(types.SHOW_LOG_PANEL),
      toggleFold: createToPayload(types.TOGGLE_FOLD),
    };
  }

  *saga() {
    yield* super.saga();
    const { types, selectors } = this;

    yield takeLatest(types.FETCH_DONE, function*() {
      yield put({ type: types.SET_LOADING, payload: false });
    });
    yield takeLatest(types.SET_SPAN_DETAIL, function*(action) {
      const curData = { ...action.payload };
      const data = [];
      if (curData.client) {
        data.push(curData.client);
      }
      if (curData.server) {
        data.push(curData.server);
      }
      delete curData.server;
      delete curData.client;
      data.push(curData);
      yield SpanDetailDuck.show({ data, title: '请求详情' });
    });
    yield takeLatest(types.FETCH_DONE, function*() {
      const data = selectors.data(yield select());
      const levelIds = [];
      if (data?.list?.length) {
        const root = data?.list[0];
        levelIds.push(...[root.levelId, ...(root.children?.length ? root.children.map(x => x.levelId) : [])]);
      }
      yield put({ type: types.SET_LEVEL_IDS, payload: levelIds });
    });

    yield takeLatest(types.TOGGLE_FOLD, function*(action) {
      const { data, status } = action.payload;
      const levelIds = selectors.levelIds(yield select());
      const curIds = [data.levelId, ...(data.children?.length ? data.children?.map(x => x.levelId) : [])];
      if (status) {
        // 折叠
        const indexs = [];
        curIds.forEach(id => {
          const i = levelIds.indexOf(id);
          if (i < 0) return;
          indexs.push(i);
        });
        indexs.reverse().forEach(index => {
          levelIds.splice(index, 1);
        });
      } else {
        // 展开
        levelIds.push(...curIds);
      }
      yield put({ type: types.SET_LEVEL_IDS, payload: [...levelIds] });
    });
  }

  async getData(composedId) {
    // 查询接口
    const { traceId } = composedId;
    if (!traceId) {
      return null;
    }
    const result = await fetchTraceDetail({
      traceId,
    });
    return { ...result } as this['Data'];
  }
}
