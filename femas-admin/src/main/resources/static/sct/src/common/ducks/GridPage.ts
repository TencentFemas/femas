import * as csvUtil from '../components/csv';
import { call, fork, put, select } from 'redux-saga/effects';
import GridDuck, { GridResult } from '../ducks/Grid';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import PageDuck from '../ducks/Page';
import { delay } from 'redux-saga';
import { runAndTakeLatest, takeLatest } from 'redux-saga-catch';
import { notification, TableColumn as TablePanelColumnGeneric } from 'tea-component';
import { ColumnField } from '../duckComponents/FieldManager';
import { runAndWatchLatest } from '../helpers/saga';
import { combineCompleters, Completer, getBufferUpdate, UPDATE } from './utils/completer';
import tips from '../util/tips';
// 兼容原有引用
export { Completer, UPDATE, combineCompleters };

export interface Identifiable {
  id: string | number;
}

/**
 * 单Grid页面 公共逻辑提取：
 * grid加载、搜索、翻页、过滤、额外数据加载
 */
export interface Filter {
  page?: number;
  count?: number;
  keyword?: string;
}

export type ColumnOptions = {};

export enum OperationType {
  /** 无目标操作，或目标不是列表项的操作 */
  NO_TARGET,
  /** 单目标操作，action payload为操作对象 */
  SINGLE,
  /** 批量操作，从Grid selection获取操作对象 */
  MULTI,
}

export interface Result<T = any> {
  totalCount: number;
  list: T[];
}

enum Types {
  'PAGINATE',
  'INPUT_KEYWORD',
  'SEARCH',
  'RELOAD',
  'EXPORT',
  'EXPORT_DONE',
  'EXPORT_CAM_FAIL',
  /** 设置当前能展示的完整列 */
  'SET_FULL_COLUMNS',
  /** 设置用户自定义列 */
  'SET_CUSTOM_FIELDS',
}

export interface Column<T> extends TablePanelColumnGeneric<T> {
  /** 是否支持，如果不支持，则在列配置界面上也不展示 */
  supported?: boolean;
  /** 是否强制展示，此时列配置界面无法取消勾选 */
  required?: boolean;
  /** 是否默认隐藏，此时列配置界面上仍可添加勾选 */
  hide?: boolean;
}

interface GenericGrid<Filter, Item> extends GridDuck {
  Filter: Filter;
  Item: Item;
}

interface DuckClass<Filter, Item> {
  new (...any: any[]): GenericGrid<Filter, Item>;
}

/**
 * 列表页
 *
 * 代码参考模板： http://code.qcm.oa.com/?tpl=gridpage
 */
export default abstract class GridPageDuck extends PageDuck {
  abstract Filter: Filter;
  abstract Item;
  ColumnOptions: ColumnOptions;
  Column: Column<this['Item']>;

  /**
   * 每条记录的ID属性，用于Table组件渲染时的key，以及选择功能的索引。默认为 "id"
   */
  get recordKey() {
    return 'id';
  }

  get Grid(): DuckClass<this['Filter'], this['Item']> {
    type Filter = this['Filter'];
    type Item = this['Item'];
    const duck = this;
    return class extends GridDuck {
      Filter: Filter;
      Item: Item;

      get buffer() {
        return duck.buffer;
      }

      get recordKey() {
        return duck.recordKey;
      }

      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      getData(me = this, filter: this['Filter']) {
        return duck.getData(filter);
      }
    };
  }

  get quickTypes() {
    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducers() {
    const { types, defaultPageSize, maxPageSize } = this;
    return {
      ...super.reducers,
      page: (state = 1, action): number => {
        switch (action.type) {
          case types.PAGINATE:
            return action.payload.page || state;
          default:
            return state;
        }
      },
      count: (state = Math.min(defaultPageSize, maxPageSize), action): number => {
        switch (action.type) {
          case types.PAGINATE:
            return action.payload.count || state;
          default:
            return state;
        }
      },
      pendingKeyword: (state = '', action): string => {
        switch (action.type) {
          case types.INPUT_KEYWORD:
          case types.SEARCH:
            return action.payload;
          default:
            return state;
        }
      },
      keyword: reduceFromPayload<string>(types.SEARCH, ''),
      fullColumns: reduceFromPayload<this['Column'][]>(types.SET_FULL_COLUMNS, []),
      customFields: reduceFromPayload<Identifiable[]>(types.SET_CUSTOM_FIELDS, []),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      paginate: (page: number, count?: number) => {
        const payload = { page, count };
        return {
          type: types.PAGINATE,
          payload,
        };
      },
      inputKeyword: createToPayload<string>(types.INPUT_KEYWORD),
      search: (keyword: string) => {
        // 进行前后空格去除
        if (this.autoTrimKeyword && typeof keyword === 'string') {
          keyword = keyword.trim();
        }
        return {
          type: types.SEARCH,
          payload: keyword,
        };
      },
      /** 清空搜索条件，可重写 */
      clearSearchCondition: () => this.creators.search(''),
      reload: () => ({
        type: types.RELOAD,
      }),
      /* 导出 */
      export: (columns: csvUtil.CsvColumn[], name: string) => ({
        type: types.EXPORT,
        payload: columns,
        name,
      }),
      setFullColumns: createToPayload<this['Column'][]>(types.SET_FULL_COLUMNS),
      setCustomFields: createToPayload<Identifiable[]>(types.SET_CUSTOM_FIELDS),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      filter: (state: State): this['Filter'] => ({
        // regionId: state.region.id,
        page: state.page,
        count: state.count,
        keyword: state.keyword,
      }),
      page: ({ page }: State) => page,
      count: ({ count }: State) => count,
      pendingKeyword: (state: State) => state.pendingKeyword,
      keyword: ({ keyword }: State) => keyword,
      /**
       * 搜索描述语xxx，展示在Grid的上方（搜索"xxx"，找到N条结果。返回原列表）。
       * 如果为空则默认使用filter.keyword，可重写
       */
      searchCondition: () => '',
      columnOptions: (): this['ColumnOptions'] => ({}),
      /** 供FieldsManager使用 */
      fields: (state: State): ColumnField[] => {
        type Column = this['Column'];
        const fullColumns = state.fullColumns as Column[];

        return fullColumns
          .filter(x => x.supported !== false)
          .map((c: this['Column']) => ({
            id: c.key,
            headTitle: typeof c.header === 'function' ? c.header(c) : c.header,
            required: c.required,
            defaults: c.required,
            hide: c.hide,
          }));
      },
      columns: ({ fullColumns, customFields }: State) => {
        const set = new Set<string>();

        customFields.forEach(f => {
          set.add(f.id as string);
        });

        return fullColumns.filter(c => {
          return c.supported !== false && (c.required || !customFields.length || set.has(c.key));
        });
      },
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      grid: this.Grid as this['Grid'],
    };
  }

  /** 哪些动作需要触发reload */
  get watchTypes(): string[] {
    const { types } = this;
    return [types.SEARCH, types.RELOAD];
  }

  /**
   * 是否等待路由初始同步完成，才开始监听reload，为了兼容旧有行为，默认为false。
   *
   * 如果碰到 initialFetch 置为false，但url路由初始化触发刷新的问题，请打开此选项。
   */
  get waitRouteInitialized() {
    return false;
  }

  /** 哪些动作需要重置页码，默认所有 {#watchTypes} 都会触发 */
  get resetPageTypes(): string[] {
    return this.watchTypes;
  }

  /** getData入参的类型声明 */
  get GetDataParams(): this['Filter'] {
    return null;
  }

  /** getData返回值的类型声明 */
  get GetDataResult(): Promise<GridResult<this['Item']>> {
    return null;
  }

  /** 是否需要初始加载，注意如果查询条件变更也会导致加载 */
  get initialFetch() {
    return true;
  }

  get minPageSize() {
    return 5;
  }

  get pageSizeInterval() {
    return 5;
  }

  /** 一次最大查询条数，建议根据后台接口来设置 */
  get maxPageSize() {
    return 100;
  }

  /** 默认每页条数 */
  get defaultPageSize() {
    return 20;
  }

  /** 页面参数列表 */
  get params(): this['Params'] {
    const { types, creators, maxPageSize, defaultPageSize } = this;
    return [
      ...super.params,
      //   {
      //     key: 'regionId',
      //     route: 'rid',
      //     history: true,
      //     defaults: () => appUtil.getRegionId(),
      //     parse: v => parseInt(v, 10) || 1,
      //     type: ducks.region.types.SELECT,
      //     selector: ducks.region.selectors.id,
      //     creator: ducks.region.creators.select
      //   },
      {
        key: 'page',
        defaults: 1,
        parse: v => parseInt(v, 10) || 1,
        type: types.PAGINATE,
        // selector不需要定义，有selectors.page定义了
        creator: page => creators.paginate(page),
      },
      {
        key: 'count',
        defaults: Math.min(defaultPageSize, maxPageSize),
        parse: v => parseInt(v, 10) || defaultPageSize,
        type: types.PAGINATE,
        // selector同page，不需要定义
        creator: count => creators.paginate(null, count),
      },
      {
        key: 'keyword',
        defaults: '',
        type: types.SEARCH,
        creator: 'search',
      },
    ];
  }

  /** 是否在搜索时自动对关键词进行前后空格去除，默认为true */
  get autoTrimKeyword(): boolean {
    return true;
  }

  /**
   * 留给子类自定义
   * 设置需要更新 columns 的事件
   */
  get columnsWatchTypes(): string[] {
    const { ducks } = this;
    return [ducks.grid.types.FETCH_DONE];
  }

  /**
   * 加载数据缓冲，默认100ms
   */
  get buffer() {
    return 100;
  }

  /**
   * Completer更新缓冲，默认为0关闭
   *
   * 如果列表比较大，又是单项更新，可以配置buffer减少React重绘以提升UI性能
   */
  get completerUpdateBuffer(): number {
    return 0;
  }

  /** 列表补全，因为历史兼容，ts声明无法补加 */
  get Completer(): new (filter?) => Completer<any, any, any> {
    return null;
  }

  /** 列表操作映射快速 */
  get operations(): Operation<this['Item']>[] {
    return [];
  }

  *saga() {
    yield* super.saga();
    yield* this.sagaGridPageMain();
  }

  /** Grid加载方法，会覆盖ducks.grid的getData */
  abstract getData(filter: this['GetDataParams']): Promise<GridResult<this['Item']>>;

  /** 留给子类自定义 */
  getColumns() {
    return null;
  }

  *sagaGridPageMain() {
    // GridPage的前置任务
    yield* this.waitGridPageReady();

    // 初始时，以及参数改变时，重新加载Grid
    yield* this.watchLoadData();

    // UI操作搜索、切换地域时，重置页码
    yield* this.watchResetPagination();

    // 导出
    yield* this.watchExport();

    // 监听columns变更
    yield* this.sagaColumnsWatch();

    // 监听操作映射
    yield* this.sagaOperationsWatch();
  }

  getCompleterUpdater(): UPDATE<any> {
    const {
      ducks: { grid },
    } = this;
    return getBufferUpdate(function*(list) {
      yield put(grid.creators.updateList(list));
    }, this.completerUpdateBuffer);
  }

  protected *sagaColumnsWatch() {
    const duck = this;
    const { creators, selectors } = duck;
    yield runAndWatchLatest<this['ColumnOptions']>(this.columnsWatchTypes, selectors.columnOptions, function*() {
      // getColumns 有时候需要调后端接口, 加一个yield, @author seven
      yield put(creators.setFullColumns(yield duck.getColumns()));
    });
  }

  /** 前置任务，完成后才开始GridPage逻辑，次序是： Page.ready -> ducks(包含routes) -> GridPage.ready -> GridPage.watchs */
  protected *waitGridPageReady() {
    if (this.waitRouteInitialized) {
      yield* this.routeInitialized();
    }
  }

  /**
   * 搜索条件描述从selectors中移到方法中，以便实现更灵活的实时计算
   *
   * 如果使用TagSearch，请复写成
   * ```typescript
   * return yield* this.ducks.tagSearch.getSearchCondition()
   * ```
   */
  protected *getSearchCondition(): Generator<any, string, any> {
    return this.selectors.searchCondition(yield select());
  }

  // 初始时，以及参数改变时，重新加载Grid
  protected *watchLoadData() {
    const duck = this;
    const { types, watchTypes, initialFetch } = duck;

    const helper = initialFetch ? runAndTakeLatest : takeLatest;

    yield helper([types.PAGINATE, watchTypes], function* reloadOnChange() {
      yield* duck.loadData();
    });
  }

  /** 加载数据，返回 dataFetched as boolean  */
  protected *loadData() {
    const duck = this;
    const {
      ducks: { grid },
    } = duck;
    const filter = yield call([duck, duck.getFilter], duck);
    const Completer = this.Completer;
    // 数据补全器
    const completer = Completer ? new Completer() : null;
    if (completer) {
      yield fork([completer, completer.init], filter);
    }
    // 以前是异步的，现在改为同步后，CKafka列表的自动刷新会有问题 --- 已同步修改
    const dataFetched = yield* grid.sagaManualFetch(filter, yield* this.getSearchCondition());

    if (!dataFetched) {
      return dataFetched;
    }
    // 补全回调
    if (completer) {
      const state = grid.selector(yield select());
      yield fork([completer, completer.fetched], state.list, state.totalCount, this.getCompleterUpdater());
    }

    return dataFetched;
  }

  // UI操作搜索、切换地域时，重置页码
  protected *watchResetPagination() {
    const duck = this;
    const { selectors, creators, resetPageTypes } = duck;
    yield takeLatest(resetPageTypes, function* resetPage(action) {
      // URL同步过来的无视
      if (action.fromRoute) {
        return;
      }
      const page = yield select(selectors.page);
      if (page !== 1) {
        yield put(creators.paginate(1));
      }
    });
  }

  protected *watchExport() {
    const duck = this;
    yield takeLatest([duck.types.EXPORT], function* doExport(action) {
      yield call(delay, 300);
      yield* duck.doExport(action);
    });
  }

  /** 可供扩展 */
  protected *getExportData(filter: this['Filter']): IterableIterator<any> {
    return yield call([this, this.getData], filter);
  }

  protected *sagaExportProgress(process) {
    tips.showLoading({
      text: `当前导出进度：${process}%`,
    });
  }

  protected *doExport(action) {
    const duck = this;
    const {
      types,
      getFilter,
      maxPageSize,
      options: { namespace },
    } = duck;
    if (!csvUtil.isSupportSave()) {
      notification.error({ description: '此浏览器版本过低，不支持导出功能' });
      return;
    }
    const columns = action.payload;
    try {
      let finished = false;
      const filter = yield call(getFilter, duck);
      // 导出所有
      let fullList = [];
      let page = 1;
      const pageSize = maxPageSize;
      // 有的接口有限制最大页数，这时需要多次查询
      do {
        filter.page = page;
        filter.count = pageSize;
        const { list, totalCount } = yield call([duck, duck.getExportData], filter);
        fullList = fullList.concat(list);

        const totalPage = Math.ceil(totalCount / pageSize);

        yield fork([duck, duck.sagaExportProgress], Math.round((100 * page) / totalPage));

        if (page >= totalPage) {
          finished = true;
        }
        page++;
      } while (!finished);
      // // debug
      // fullList = fullList.concat(fullList) // x2
      // fullList = fullList.concat(fullList) // x4
      // fullList = fullList.concat(fullList) // x8
      // fullList = fullList.concat(fullList) // x16
      // fullList = fullList.concat(fullList) // x32
      // fullList = fullList.concat(fullList) // x64
      // fullList = fullList.concat(fullList) // x128
      // fullList = fullList.concat(fullList) // x256

      const fileName = action.name || namespace;

      //let message = csvUtil.save(`${fileName}.csv`, content, false)
      let exceed;
      // 是否尝试导出失败
      let tried = false;
      // 目前导出的文件数
      let fileNum = 0;
      // 已成功导出数
      let exported = 0;
      // 上次导出失败的位置
      let lastTry;
      do {
        // 本次尝试范围，默认导出乘下的所有，失败时减少导出量重试
        const thisTry = lastTry ? Math.floor((lastTry - exported) / 1.5) + exported : fullList.length;
        const content = csvUtil.format(fullList.slice(exported, thisTry), columns);
        exceed = csvUtil.save(`${fileName}${tried ? '_' + (fileNum + 1) : ''}.csv`, content, false);
        if (exceed) {
          lastTry = thisTry;
        } else {
          fileNum++;
          exported = thisTry;
          lastTry = null;
        }
        tried = true;
      } while (exceed || exported < fullList.length);

      if (fileNum > 1) {
        tips.info({ text: '因文件过大，自动拆分为多个文件下载' });
      } else {
        notification.success({ description: '导出成功' });
      }
      yield put({ type: types.EXPORT_DONE });
    } catch (e) {
      notification.error({
        description: `${'导出失败'} [${e.code || 0}]${e.message}`,
      });
    }
  }

  /**
   * 获取Grid的过滤条件，可重写，用来扩展filter。
   * （selectors.filter扩展有局限性）
   * @param param0
   */
  protected *getFilter({ selectors }: this): any {
    return yield select(selectors.filter);
  }

  /** 各操作映射 */
  protected *sagaOperationsWatch() {
    type Instance = this['Item'];
    const { creators, ducks, operations } = this;
    for (const operation of operations) {
      let wappedSaga: (action?: any) => any;
      switch (operation.type) {
        // 无目标操作
        case OperationType.NO_TARGET:
          wappedSaga = operation.fn;
          break;
        // 单条操作映射
        case OperationType.SINGLE:
          wappedSaga = function*(action) {
            const { payload } = action;
            return yield operation.fn(payload, action);
          };
          break;
        // 批量操作映射
        case OperationType.MULTI:
          wappedSaga = function*(action) {
            const instance: Instance = action.payload;
            let instances: Instance[];
            if (instance) {
              instances = [instance];
            } else {
              instances = ducks.grid.selectors.selection(yield select());
            }
            return yield operation.fn(instances, action);
          };
          break;
      }
      if (!wappedSaga) {
        continue;
      }
      yield takeLatest(operation.watch, function*(action) {
        const success = yield wappedSaga(action);
        if (success && operation.reload !== false) {
          yield put(creators.reload());
        }
        if (success && operation.successTip) {
          notification.success({ description: operation.successTip });
        }
        if (success && operation.successCb) {
          yield operation.successCb();
        }
      });
    }
  }
}

// ---------------------- 列表操作快速映射(operations模式) -------------------------

interface AbstractOperation {
  /** 监听action，string时为action.type；Function时为action filter */
  watch: string | Function;
  /** 成功后是否重新加载数据，默认为true */
  reload?: boolean;
  /** 成功后tip提示内容（根据fn的结果判断是否成功） */
  successTip?: string;
  /** 成功后自定义回调 */
  successCb?: Function;
}

type BooleanResult = Promise<boolean> | Generator<any, boolean, any>;

export interface OperationNoTarget extends AbstractOperation {
  type: OperationType.NO_TARGET;
  /** 异步任务，成功返回true，否则认为任务失败 */
  fn: (action?: any) => BooleanResult;
}

export interface OperationSingle<T> extends AbstractOperation {
  type: OperationType.SINGLE;
  /** 异步任务，成功返回true，否则认为任务失败 */
  fn: (instance: T, action?: any) => BooleanResult;
}

export interface OperationMulti<T> extends AbstractOperation {
  type: OperationType.MULTI;
  /** 异步任务，成功返回true，否则认为任务失败 */
  fn: (instances: T[], action?: any) => BooleanResult;
}

export type Operation<T> = OperationNoTarget | OperationSingle<T> | OperationMulti<T>;
