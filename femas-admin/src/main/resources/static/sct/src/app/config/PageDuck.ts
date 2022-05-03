import GridPageDuck, { Filter as BaseFilter } from '@src/common/ducks/GridPage';
import { ConfigItem } from './types';
import NamespaceSelectDuck from '../namespace/components/NamespaceSelectDuck';
import { put, select, takeLatest } from 'redux-saga/effects';
import { createToPayload } from 'saga-duck';
import { Modal } from 'tea-component';
import create from './operations/create';
import { resolvePromise } from 'saga-duck/build/helper';
import { deleteConfigs, describeConfigs, fetchConfigById } from './model';
import { Action } from '@src/common/types';
import { EDIT_TYPE } from './operations/create/CreateDuck';

interface Filter extends BaseFilter {
  namespaceId: string;
}

export default class ConfigPageDuck extends GridPageDuck {
  Filter: Filter;
  Item: ConfigItem;

  get recordKey() {
    return 'configId';
  }

  get baseUrl() {
    return '/config';
  }

  get initialFetch() {
    return false;
  }

  get watchTypes() {
    return [...super.watchTypes, this.ducks.namespace.types.SELECT];
  }

  get params() {
    return [...super.params, this.ducks.namespace.getRegistryParam()];
  }

  get quickTypes() {
    enum Types {
      DELETE,
      ADD,
      CONFIGURE_VERSION,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      delete: createToPayload<Array<ConfigItem>>(types.DELETE),
      add: createToPayload<void>(types.ADD),
      configureVersion: createToPayload<string>(types.CONFIGURE_VERSION),
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      namespace: NamespaceSelectDuck,
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
        namespaceId: state.namespace.id,
      }),
    };
  }

  *saga() {
    yield* this.sagaInitLoad();
    yield* super.saga();
    const {
      ducks: { namespace },
      types,
      creators,
    } = this;
    yield takeLatest(types.DELETE, function*(action: Action<ConfigItem[]>) {
      const namespaceId = namespace.selectors.id(yield select());
      const arrs = action.payload;
      const yes = yield Modal.confirm({
        message: '确认删除当前所选配置？',
        description: '删除后，该配置将会被清空，且无法恢复。',
        okText: '删除',
        cancelText: '取消',
      });
      if (!yes) return;
      yield deleteConfigs({
        configIdList: arrs.map(d => d.configId),
        namespaceId,
      });
      yield put(creators.reload());
    });
    yield takeLatest(types.ADD, function*() {
      const namespaceId = namespace.selectors.id(yield select());
      const res = yield* resolvePromise(
        create({} as ConfigItem, {
          namespaceId,
          editType: EDIT_TYPE.create,
        }),
      );
      if (res) {
        yield put(creators.reload());
      }
    });
    yield takeLatest(types.CONFIGURE_VERSION, function*(action: Action<string>) {
      const configId = action.payload;
      const namespaceId = namespace.selectors.id(yield select());
      const config: ConfigItem = yield fetchConfigById({
        configId,
        namespaceId,
      });
      const {
        currentReleaseVersion: { configValue, configVersion, configVersionId },
      } = config;
      const res = yield* resolvePromise(
        create({ ...config, configValue } as ConfigItem, {
          namespaceId,
          configId,
          editType: EDIT_TYPE.rollback,
          configVersion,
          configVersionId,
        }),
      );
      if (res) {
        yield put(creators.reload());
      }
    });
  }

  *sagaInitLoad() {
    const { ducks } = this;
    yield* ducks.namespace.load({});
  }

  async getData(filters: this['Filter']) {
    const { page, count, keyword, namespaceId } = filters;

    if (!namespaceId) {
      return { totalCount: 0, list: [] };
    }

    const res = await describeConfigs({
      namespaceId,
      pageNo: page,
      pageSize: count,
      searchWord: keyword,
    });

    return {
      totalCount: res.totalCount,
      list: res.list as Array<ConfigItem>,
    };
  }
}
