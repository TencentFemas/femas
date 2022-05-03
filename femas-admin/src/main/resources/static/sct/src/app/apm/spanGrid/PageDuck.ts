import { createToPayload, reduceFromPayload } from 'saga-duck';
import { takeLatest } from 'redux-saga-catch';
import BaseGridPage, { Filter as BaseFilter, Result } from '@src/common/ducks/GridPage';
import SpanDetailDuck from '../requestDetail/detail/SpanDetailDuck';
import { Instance, Tag } from '../requestDetail/types';
import { describeSpans } from '@src/app/trace/model';
import moment from 'moment';
import { FilterTime } from '@src/common/types';

enum Types {
  INSPECT,
  SET_SPAN_DETAIL,
  SET_SERACH_PARAMS,
}

interface Filter extends BaseFilter {
  searchParams: SearchParams;
}

interface SearchParams {
  filterTime: FilterTime;
  tags: Array<Tag>;
  notNeedFilterTag?: boolean;
}

export default abstract class PageDuck extends BaseGridPage {
  Filter: Filter;
  Item: Instance;
  baseUrl = null;

  get quickTypes() {
    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get watchTypes() {
    return [...super.watchTypes, this.types.SET_SERACH_PARAMS];
  }

  get initialFetch() {
    return false;
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      searchParams: reduceFromPayload(types.SET_SERACH_PARAMS, {} as SearchParams),
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
      searchParams: (state: State) => state.searchParams,
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      inspect: payload => ({ type: types.INSPECT, payload }),
      showSpanDetail: createToPayload(types.SET_SPAN_DETAIL),
      setSearchParams: createToPayload(types.SET_SERACH_PARAMS),
    };
  }

  *saga() {
    yield* super.saga();
    const { types } = this;
    yield takeLatest(types.SET_SPAN_DETAIL, function*() {
      // const { traceId, spanId } = action.payload;
      const instance = {
        // traceId,
        // spanId,
        data: [],
        title: '请求详情',
      };
      yield SpanDetailDuck.show(instance);
    });
  }

  async getData(filter: Filter) {
    const { page, count, searchParams } = filter;
    const { filterTime, tags, notNeedFilterTag, ...rest } = searchParams;
    const { startTime, endTime } = filterTime;
    const result = await describeSpans({
      offset: (page - 1) * count,
      limit: count,
      ...filterTime,
      tags: tags.filter(item => notNeedFilterTag || item.value),
      ...rest,
    });
    const { totalCount, list } = result;

    return {
      totalCount,
      list: list.map((x, i) => ({
        ...x,
        id: x.spanId + i,
        startTimestamp: +moment(startTime),
        endTimestamp: +moment(endTime),
      })),
    } as Result<Instance>;
  }
}
