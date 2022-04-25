import GridPageDuck, { Filter as BaseFilter } from '@src/common/ducks/GridPage';
import { OperationItem } from './types';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { FilterTime } from '@src/common/types';
import { fetchLogs } from './model';

interface Filter extends BaseFilter {
  filterTime: FilterTime;
}

export default class OperationDuck extends GridPageDuck {
  Filter: Filter;
  Item: OperationItem;

  get baseUrl() {
    return '/operation-log';
  }

  get quickTypes() {
    enum Types {
      SET_FILTER_TIME,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get initialFetch() {
    return false;
  }

  get watchTypes() {
    const { types } = this;
    return [...super.watchTypes, types.SET_FILTER_TIME];
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      filterTime: reduceFromPayload(types.SET_FILTER_TIME, {} as FilterTime),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      setFilterTime: createToPayload<FilterTime>(types.SET_FILTER_TIME),
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
        filterTime: state.filterTime,
      }),
    };
  }

  async getData(params: this['Filter']) {
    const { page, count, filterTime } = params;
    const res = await fetchLogs({
      pageNo: page,
      pageSize: count,
      startTime: filterTime.startTime,
      endTime: filterTime.endTime,
    });
    return res;
  }
}
