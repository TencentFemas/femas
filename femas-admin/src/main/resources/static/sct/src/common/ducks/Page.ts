import { all, call, cancel, fork, put, select, take } from 'redux-saga/effects';
import { delay, Effect } from 'redux-saga';
import { createToPayload, DuckMap, reduceFromPayload } from 'saga-duck';
import { runAndTakeLatest, takeEvery, takeLatest } from 'redux-saga-catch';
import RouteDuck, { Param } from '../ducks/Route';
import { createElement, ReactElement } from 'react';
import { Alert } from 'tea-component';

type SELECTOR<T> = (globalState: any) => T;
type CREATOR<T> = (value: T) => any;

export interface PageParam<T = any> extends Param<T> {
  /**
   * 参数变更时，通过什么ActionType通知外界；
   * 以及外界什么情况下参数会变更要同步到url上；
   * 可替代selector与creator，但仅限最传统的 {payload:value} 格式的action
   */
  type?: string;
  /**
   * 如何获取参数对应的值
   * 如果为string，表示通过当前页面的selectors.xxx来创建
   */
  selector?: string | SELECTOR<T>;
  /**
   * 如何创建Action通知参数变更。
   * 如果不传（默认），则创建 { type, payload } 的action
   * 如果为string，表示通过当前页面的creators.xxx来创建
   */
  creator?: string | CREATOR<T>;
}

enum Types {
  READY,
  /** Page 已经根据路由初始CHANGE动作完成初始化 */
  ROUTE_INITIALIZED,
  SET_PRE_ERROR,
  SET_TITLE,
  ROUTE_CHANGED,
}

/**
 * 页面公共逻辑提取：
 * url、route
 */
export default abstract class PageDuck extends DuckMap {
  /**
   * 前置任务(preEffects)throw数据类型
   */
  PreError: any;
  private _watchTypes: string[];

  constructor() {
    // eslint-disable-next-line prefer-rest-params
    super(...arguments);
    // TODO 延后初始化，有点hack，后面可考虑怎么优化
    if (this.routeEnabled) {
      this.ducks.routes.setParams(this.params);
    }
  }

  private _routeCreators: { [key: string]: (payload: any) => any };

  /** 路由变动时，怎么通知外界 */
  get routeCreators() {
    if (this._routeCreators) {
      return this._routeCreators;
    }
    const creators = this.creators;
    return (this._routeCreators = this.params.reduce((map, param) => {
      let creator: (payload: any) => any;
      if (param.creator) {
        creator = creators[param.creator as string] || param.creator;
      } else {
        creator = creators[param.key] || (payload => ({ type: param.type, payload }));
      }
      map[param.key] = creator;
      return map;
    }, {}));
  }

  private _routesSelector: (globalState: any) => { [key: string]: any };

  /** 路由参数选择器 */
  get routesSelector() {
    if (this._routesSelector) {
      return this._routesSelector;
    }
    const { selector, selectors } = this;
    return (this._routesSelector = state =>
      this.params.reduce((map, param) => {
        let routeSelector;
        if (param.selector) {
          routeSelector = selectors[param.selector as string] || param.selector;
        } else {
          routeSelector = selectors[param.key] || (state => selector(state)[param.key]);
        }
        map[param.key] = routeSelector(state);
        return map;
      }, {}));
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
      /**
       * 前置任务(preEffects)有任意任务返回false或有throw，都会被认为拦截页面主逻辑
       *
       * 前置任务执行前此属性为null，被拦截后为false，执行完无拦截为true
       */
      ready: reduceFromPayload<boolean>(types.READY, null),
      /**
       * 如果前置任务(preEffects)有throw，则throw的值会被存放于此属性上
       *
       * 建议与 selectors.interceptor 配合使用，方便将多页面共享前置逻辑集中实现
       */
      preError: reduceFromPayload<this['PreError']>(types.SET_PRE_ERROR, null),
      /**
       * url => Route => PageDuck 属性是否已初始化同步完成。**此时Page根据params完成了初始值设置**
       */
      routeInitialized: (state = false, action) => {
        switch (action.type) {
          case types.ROUTE_INITIALIZED:
            return true;
          default:
            return state;
        }
      },

      /**
       * 动态页面标题
       */
      title: reduceFromPayload<string>(types.SET_TITLE, this.title),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      ready: (state: State) => state.ready,
      /**
       * 与 preError 组合使用，直接返回ReactChild
       */
      interceptor: ({ ready, preError }: State): ReactElement => {
        // 前置任务未完成
        if (ready === null) {
          return createElement('noscript');
        }
        // 前置任务未满足
        if (ready === false) {
          // 如果有异常
          if (preError) {
            return this.getInterceptorByPreError(preError);
          }
          return createElement('noscript');
        }
      },
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      routeChanged: () => ({ type: types.ROUTE_CHANGED }),
      /** 设置页面标题 */
      setTitle: createToPayload<string>(types.SET_TITLE),
    };
  }

  /**
   * 页面默认动态标题，注意它可以被 creators.setTitle 动作覆盖
   *
   * 如需静态标题，建议等tea-app更新后，使用app.routes中的title配置（目前存在不会自动添加“ - 控制台”统一后缀的问题）
   */
  get title() {
    return null;
  }

  /** 页面路由路径 */
  abstract get baseUrl(): string;

  /** 页面路由参数定义，可扩展 */
  get params(): PageParam[] {
    return [];
  }

  /** 页面路由参数类型引用  */
  get Params(): PageParam[] {
    return null;
  }

  get rawDucks() {
    return {
      ...super.rawDucks,
      routes: new RouteDuck({
        ...this.getSubDuckOptions('routes'),
        baseUrl: this.baseUrl,
        // 可能死循环，要延后
        // params: this.params
      }),
    };
  }

  /**
   * 前置逻辑，返回saga effect数组，它们将会并行处理，全部完成后才会执行页面的saga主逻辑
   * 如果需中断，请在任务中throw。任何一个前置任务中止，都会中止剩余前置任务并阻塞页面逻辑
   * 示例：
   * ```javascript
   get preSagas(){
  return [
    ...super.preSagas,
    call(function*(){}),
    call([this, this.bar])
  ]
}
   * ```
   */
  get preEffects(): Effect[] {
    return [call([this, this.ready], this)];
  }

  /** preEffects类型定义 */
  get PreEffects(): Effect[] {
    return null;
  }

  get routeEnabled(): boolean {
    return !!(this.baseUrl && this.params.length);
  }

  /**
   * params: {
   *    selector(state) // 用于获取路由参数，以便通知路由模块；也可以为字串，从当前Duck的selectors中获取
   *    type     // 用于监听路由参数变化
   *    creator(routeValue)  // （如果没有定义，则从type生成）用于生成路由变动通知动作，仅限路由 => 业务逻辑的通知；也可以为字串，从当前Duck的creators中获取
   * }
   */
  /** 哪些动作需要更新路由 */
  get watchRouteTypes(): string[] {
    if (this._watchTypes) {
      return this._watchTypes;
    }
    const types = this.types;
    return (this._watchTypes = this.params.reduce(
      (typeList, param) => typeList.concat(types[param.type] || param.type),
      [],
    ));
  }

  /** 加入页面前置逻辑 */
  *saga(): Generator<any, any, any> {
    let ready = this.selectors.ready(yield select());
    if (ready === null) {
      ready = yield call([this, this.waitPreEffects]);
      ready = ready !== false;
      yield put({ type: this.types.READY, payload: ready });
    }
    // 如果返回false，表示页面不再渲染
    if (!ready) {
      yield cancel();
      return;
    }
    // 路由先监听，防止漏过ducks.routes的初始CHANGED动作
    yield* this.sagaInitRoute();
    // 子duck逻辑执行
    yield* super.saga();
    // 建议，所有阻塞式的saga，如无必要都用fork方式引入，保持并行执行
    yield fork([this, this.sagaUseTitle]);
  }

  *sagaUseTitle() {
    const { types, selector } = this;
    yield runAndTakeLatest(types.SET_TITLE, function*() {
      const { title } = selector(yield select());
      if (!title) {
        return;
      }
      document.title = title;
      // 恢复标题？无法得知旧的标题，放弃处理
      // yield take(END)
      // app.setDocumentTitle(null)
    });
  }

  *sagaInitRoute(duck = this) {
    if (!duck.routeEnabled) {
      return;
    }
    const { types, selector, ducks, watchRouteTypes, routesSelector, routeCreators } = duck;
    // 路由变化，同步到属性上
    yield takeEvery([ducks.routes.types.CHANGE], function* routeToAttr(action) {
      const routes = action.changes;

      const actions = Object.keys(routes).reduce(
        (actions, key) => actions.concat(routeCreators[key] ? routeCreators[key](routes[key]) : []),
        [],
      );
      // 标记为路由向属性同步
      actions.forEach(action => {
        action.fromRoute = true;
      });
      const len = actions.length;
      for (let i = 0; i < len; i++) {
        yield put(actions[i]);
      }

      // 判断路由初始化
      const { routeInitialized } = selector(yield select());
      if (!routeInitialized) {
        yield put({ type: types.ROUTE_INITIALIZED });
      }
    });

    // 属性变化，同步到路由上
    const watchTypeSet = new Set([types.ROUTE_CHANGED, ...watchRouteTypes]);
    yield takeLatest(
      action => watchTypeSet.has(action.type) && !action.fromRoute,
      function* attrToRoute() {
        // 如果是路由向属性同步的，无视
        // TODO 如果一个fromRoute=false的动作后，紧接着一个fromRoute=true的动作
        // 会导致前一次的被取消，所以我们要移到外部实现
        // if (action.fromRoute) {
        //   return
        // }
        yield call(delay, 100);
        // 全量更新，因为有可能一个字段变动会同步影响其它字段有效性
        const routes = yield select(routesSelector);
        yield put(ducks.routes.creators.update(routes));
      },
    );
  }

  /**
   * 将preEffects包装为原ready接口
   */
  *waitPreEffects() {
    try {
      yield all(
        this.preEffects
          // 兼容（返回false表示中止）
          .map(effect =>
            call(function*() {
              if ((yield effect) === false) {
                throw null;
              }
            }),
          ),
      );
      return true;
    } catch (e) {
      yield put({
        type: this.types.SET_PRE_ERROR,
        payload: e,
      });
      return false;
    }
  }

  /**
   * 根据前置任务(#preEffects)错误(state.preError)，展示对应的UI
   *
   * 例如： 内测申请页、未授权提示等
   * @param preError
   */
  getInterceptorByPreError(preError): React.ReactElement {
    // 登录态失效的错误，不展示
    if (preError && preError.code === 'VERIFY_LOGIN_FAILED') {
      return createElement('noscript');
    }
    let message = JSON.stringify(preError, null, '    ');
    if (preError instanceof Error) {
      message = preError.message;
    }
    return createElement(
      Alert,
      // 显示为 error 过于暴力，用户以为是代码执行出错了
      {},
      createElement(
        'pre',
        {
          style: {
            whiteSpace: 'pre-wrap',
            wordWrap: 'break-word',
          },
        },
        message,
      ),
    );
  }

  /**
   * **已废弃，请使用 preEffects 替代**
   *
   * 前置逻辑，完成后才开始真正的业务逻辑
   * @deprecated 请使用 {@link preEffects} 替代
   * @param {*} duck
   * @return {SagaEffect<ready>}
   */
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  ready(duck): any {
    return true;
  }

  /**
   * 等待 url => PageDuck 同步完成
   * ```ts
   * yield* this.routeInitialized()
   * ```
   */
  *routeInitialized(): IterableIterator<any> {
    if (!this.routeEnabled) {
      return;
    }
    const { routeInitialized } = this.selector(yield select());
    if (!routeInitialized) {
      yield take(this.types.ROUTE_INITIALIZED);
    }
  }
}
