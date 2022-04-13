import Base, { Filter as BaseFilter } from '@src/common/ducks/GridPage';
import { put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { configureAuthRule, deleteAuthRule, fetchServiceAuthList } from './model';
import { ComposedId } from '../types';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { Modal, notification } from 'tea-component';
import { RULE_STATUS } from '../../types';

interface Filter extends BaseFilter {
  serviceName: string;
  namespaceId: string;
  registryId: string;
}

enum Types {
  LOAD,
  SET_STATUS,
  REMOVE,
}

export default class PageDuck extends Base {
  baseUrl = null;
  Filter: Filter;
  ComposedId: ComposedId;
  Item: any;

  get recordKey() {
    return 'ruleId';
  }

  get watchTypes() {
    return [...super.watchTypes, this.types.LOAD];
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

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      composedId: reduceFromPayload(types.LOAD, {} as ComposedId),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      load: createToPayload(types.LOAD),
      setStatus: createToPayload(types.SET_STATUS),
      remove: createToPayload(types.REMOVE),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      composedId: (state: State) => state.composedId,
      filter: (state: State) => {
        return {
          page: state.page,
          count: state.count,
          keyword: state.keyword,
          serviceName: state.composedId.serviceName,
          namespaceId: state.composedId.namespaceId,
          registryId: state.composedId.registryId,
        };
      },
    };
  }

  *saga() {
    yield* super.saga();
    const { types, selectors, creators } = this;
    // 修改状态
    yield takeLatest(types.SET_STATUS, function*(action) {
      const item = action.payload;
      const confirm = yield Modal.confirm({
        message: '提醒',
        description: `确认${item.status ? '开启' : '关闭'}鉴权规则，仅支持一条鉴权规则生效`,
      });
      if (!confirm) return;
      try {
        yield configureAuthRule({
          ...item,
          isEnabled: item.status ? RULE_STATUS.OPEN : RULE_STATUS.CLOSE,
        });

        notification.success({
          description: `${item.status ? '开启' : '关闭'}鉴权规则成功`,
        });
        // 重新加载列表
        yield put(creators.reload());
      } catch (e) {
        notification.error({
          description: `${item.status ? '开启' : '关闭'}鉴权规则失败`,
        });
      }
    });

    // 删除
    yield takeLatest(types.REMOVE, function*(action) {
      const item = action.payload;
      const { namespaceId, serviceName } = selectors.composedId(yield select());

      const confirm = yield Modal.confirm({
        message: '提醒',
        description: '确认删除当前鉴权规则',
      });

      if (!confirm) return;
      yield deleteAuthRule({
        ruleId: item.ruleId,
        namespaceId,
        serviceName,
      });
      // 重新加载列表
      yield put(creators.reload());
    });
  }

  async getData(filter: Filter) {
    const { page, count, namespaceId, serviceName, keyword, registryId } = filter;
    const result = await fetchServiceAuthList({
      pageNo: page,
      pageSize: count,
      namespaceId,
      serviceName,
      keyword,
    });
    return {
      totalCount: result.totalCount,
      list: result.list?.map(item => ({ ...item, registryId })) || [],
    };
  }
}
