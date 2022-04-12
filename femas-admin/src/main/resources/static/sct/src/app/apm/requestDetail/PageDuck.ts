import { createToPayload, reduceFromPayload } from 'saga-duck';
import { put, select, take } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import BaseGridPage, { Filter as BaseFilter } from '@src/common/ducks/GridPage';
import { Instance } from './types';
import DynamicSubGridDuck from './DynamicSubGridDuck';

export interface Filter extends BaseFilter {
  searchParams: any;
}

enum Types {
  UNFOLD,
  SET_SERACH_PARAMS,
}

interface Item extends Instance {
  id: string;
}

export default abstract class PageDuck extends BaseGridPage {
  Filter: Filter;
  baseUrl = null;
  Item: Item;

  // 不想重写全部columns时，可以使用additionalColumns添加尾部column内容
  get additionalColumns() {
    return [];
  }

  get watchTypes() {
    return [...super.watchTypes, this.types.SET_SERACH_PARAMS];
  }

  get maxPageSize() {
    return 50;
  }

  get initialFetch() {
    return false;
  }

  get quickTypes() {
    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      subGrid: DynamicSubGridDuck,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      searchParams: reduceFromPayload(types.SET_SERACH_PARAMS, {}),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      unfold: createToPayload(types.UNFOLD),
      setSearchParams: createToPayload(types.SET_SERACH_PARAMS),
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

  abstract getColumns(duck?);

  *saga() {
    yield* super.saga();
    const {
      types,
      selectors,
      ducks: { subGrid },
    } = this;
    yield takeLatest(types.UNFOLD, function*(action) {
      const searchParams = selectors.searchParams(yield select());
      const traceId = action.payload;
      const duckId = `subGrid-${traceId}`;
      yield put(subGrid.creators.createDuck(duckId));
      const subGridDuck = subGrid.getDuck(duckId);
      yield take(subGridDuck.types.READY);
      yield put(
        subGridDuck.creators.loadData({
          ...searchParams,
          traceId,
        }),
      );
    });
  }

  abstract getData(filter: Filter);
}
