import GridPage, { Filter as BaseFilter } from '@src/common/ducks/GridPage';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { ComposedId } from '../types';
import { put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { configureBreakerRule, deleteBreakerRule, fetchList } from './model';
// import DetailDuck from "./operations/DetailDuck";
import { EnableStatus, FuseRuleItem } from './types';
import Confirm from '@src/common/ducks/Confirm';

interface Filter extends BaseFilter, ComposedId {
  targetServiceName?: string;
  isolationLevel?: string;
}

export default class PageDuck extends GridPage {
  baseUrl = null;
  Filter: Filter;
  Item: FuseRuleItem;

  get maxPageSize() {
    return 10;
  }

  get recordKey() {
    return 'ruleId';
  }

  get initialFetch() {
    return false;
  }

  get watchTypes() {
    const { types } = this;
    return [...super.watchTypes, types.LOAD, types.SET_ISOLATION_LEVEL];
  }

  get quickTypes() {
    enum Types {
      LOAD,
      REMOVE,
      CHANGE_STATUS,
      SHOW_DETAIL,
      SET_ISOLATION_LEVEL,
      SET_NEED_REMIND,
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
      isolationLevel: reduceFromPayload(types.SET_ISOLATION_LEVEL, ''),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      load: createToPayload<ComposedId>(types.LOAD),
      remove: item => ({ type: types.REMOVE, payload: item }),
      setIsolationLevel: item => ({
        type: types.SET_ISOLATION_LEVEL,
        payload: item,
      }),
      changeStatus: item => ({
        type: types.CHANGE_STATUS,
        payload: item,
      }),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      filter: (state: State) => ({
        registryId: state.composedId.registryId,
        serviceName: state.composedId.serviceName,
        namespaceId: state.composedId.namespaceId,
        isolationLevel: state.isolationLevel,
        keyword: state.keyword,
        page: state.page,
        count: state.count,
      }),
      composedId: (state: State) => state.composedId,
      list: (state: State) => state.grid.list,
      isolationLevel: (state: State) => state.isolationLevel,
    };
  }

  *saga() {
    yield* super.saga();
    const { types, creators, selectors } = this;

    yield takeLatest(types.REMOVE, function*(action) {
      const { ruleId } = action.payload;
      const finished = yield Confirm.show({
        title: '确定删除当前服务熔断规则？',
        content: '删除后，该服务熔断规则将不再生效',
      });
      if (!finished) return;
      const { namespaceId, serviceName } = selectors.composedId(yield select());
      yield deleteBreakerRule({
        namespaceId,
        serviceName,
        ruleId,
      });
      yield put(creators.reload());
    });

    yield takeLatest(types.CHANGE_STATUS, function*(action) {
      const { isEnable: enabled } = action.payload as FuseRuleItem;
      const finished = yield Confirm.show({
        title: '确定开启/关闭当前服务熔断规则？',
        content: !enabled ? '关闭后，该服务熔断规则将不再生效' : '开启后，该服务规则将自动生效',
      });
      if (!finished) return;
      yield configureBreakerRule({
        ...action.payload,
        isEnable: enabled ? EnableStatus.Enable : EnableStatus.Disable,
      });
      yield put(creators.reload());
    });
  }

  async getData(filter: Filter) {
    const { registryId, serviceName, namespaceId, page, count, keyword, isolationLevel } = filter;
    const params = {
      namespaceId,
      serviceName,
      searchWord: keyword,
      isolationLevel: isolationLevel ? isolationLevel : undefined,
      pageNo: page,
      pageSize: count,
    };
    const res = await fetchList(params);
    return {
      totalCount: res.totalCount,
      list: res.list.map(item => ({ registryId, ...item })),
    };
  }
}
