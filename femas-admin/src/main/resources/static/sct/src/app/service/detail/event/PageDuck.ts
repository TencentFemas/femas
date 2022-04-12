import { ComposedId } from '../types';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { EventItem } from './types';
import { describeServiceEvent, fetchEventType } from '../../model';
import GridPageDuck, { Filter as BaseFilter } from '@src/common/ducks/GridPage';
import { FilterTime } from '@src/common/types';
import moment from 'moment';
import { put, takeLatest } from 'redux-saga/effects';

interface EventTypeItem {
  key: string;
  value: string;
  text: string;
}

interface Filter extends BaseFilter {
  namespaceId: string;
  serviceName: string;
  filterTime: FilterTime;
  eventType: string;
}

export default class BaseInfoDuck extends GridPageDuck {
  baseUrl = null;
  Filter: Filter;
  Item: EventItem;

  get initialFetch() {
    return false;
  }

  get watchTypes() {
    const { types } = this;
    return [...super.watchTypes, types.LOAD, types.SET_FILTER_TIME, types.SET_EVENT_TYPE];
  }

  get quickTypes() {
    enum Types {
      LOAD,
      SET_FILTER_TIME,
      SET_EVENT_TYPE,
      SET_EVENT_TYPE_LIST,
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
      composedId: reduceFromPayload(types.LOAD, {} as ComposedId),
      filterTime: reduceFromPayload(types.SET_FILTER_TIME, {} as FilterTime),
      eventType: reduceFromPayload(types.SET_EVENT_TYPE, ''),
      eventTypeList: reduceFromPayload(types.SET_EVENT_TYPE_LIST, [] as Array<EventTypeItem>),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      load: createToPayload<ComposedId>(types.LOAD),
      setFilterTime: createToPayload<FilterTime>(types.SET_FILTER_TIME),
      setEventType: createToPayload<string>(types.SET_EVENT_TYPE),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      composedId: (state: State) => state.composedId,
      filter: (state: State) => ({
        page: state.page,
        count: state.count,
        keyword: state.keyword,
        namespaceId: state.composedId.namespaceId,
        serviceName: state.composedId.serviceName,
        filterTime: state.filterTime,
        eventType: state.eventType,
      }),
      filterTime: (state: State) => state.filterTime,
      eventType: (state: State) => state.eventType,
      eventTypeList: (state: State) => state.eventTypeList,
    };
  }

  *saga() {
    yield* super.saga();
    const { types } = this;
    yield takeLatest(types.LOAD, function*() {
      // 获取eventType
      const res = yield fetchEventType({});
      yield put({
        type: types.SET_EVENT_TYPE_LIST,
        payload: res.map(d => ({ ...d, text: d.key })),
      });
    });
  }

  async getData(filters: this['Filter']) {
    const { page, count, keyword, namespaceId, serviceName, filterTime, eventType } = filters;
    return describeServiceEvent({
      pageNo: page,
      pageSize: count,
      keyword,
      namespaceId,
      serviceName,
      startTime: moment(filterTime.startTime).valueOf(),
      endTime: moment(filterTime.endTime).valueOf(),
      eventType,
    });
  }
}
