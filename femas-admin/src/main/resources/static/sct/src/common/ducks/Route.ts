/**
 * 路由功能
 */
import { BaseDuck, DuckOptions, END, INIT } from 'saga-duck';
import { call, fork, put, select, take } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import { buffers, Channel, eventChannel } from 'redux-saga';

export type GETTER<T> = () => T;
export type OPT<T> = T | GETTER<T>;

export interface Param<T = any> {
  /** 对应路由数据对象中的属性 */
  key: string;
  /** 对应URL中的参数名，不传则与key相同 */
  route?: string;
  /** 在URL中的排序，越小越靠前 */
  order?: number;
  /** 是否需要生成新的历史访问记录（即pushState与replaceState的区别），默认不生成 */
  history?: boolean;
  /** 默认值 */
  defaults?: OPT<T>;
  /** 当与默认值相等时是否展示，默认为false */
  alwaysVisible?: boolean;
  /** 解析函数，url串到最终属性 */
  parse?: (value: string) => T;
  /** 序列化函数，属性到url串 */
  stringify?: (value: T) => string;
}

export class QSParser {
  params: Param[];

  constructor(params: Param[]) {
    // 统一化params
    this.params = params
      .map(param => {
        return {
          order: param.order || 0,
          key: param.key,
          route: param.route || param.key,
          history: param.history === true,
          defaults: typeof param.defaults === 'function' ? param.defaults : () => param.defaults,
          alwaysVisible: param.alwaysVisible,
          parse: param.parse || (v => v),
          stringify: param.stringify || (v => '' + v),
        };
      })
      .sort((a, b) => a.order - b.order);
  }

  formula(values) {
    const o = {};
    this.params.forEach(({ key, parse }) => {
      let value;
      if (key in values) {
        value = parse(values[key]);
        o[key] = value;
      }
    });
    return o;
  }

  /**
   * 将url解析为routes
   * @param {*} url
   */
  parse(url) {
    const qs = url.split('?')[1] || '';
    const values = {};
    qs.split('&').forEach(kv => {
      const [k, v] = kv.split('=');
      values[decodeURIComponent(k)] = decodeURIComponent(v);
    });
    const routes = {};

    this.params.forEach(({ key, route, parse, defaults }) => {
      let value;
      if (route in values) {
        value = values[route];
      } else {
        value = defaults();
      }
      routes[key] = parse(value);
    });

    return routes;
  }

  /**
   * 将routes对象转换为url
   * @param {*} routes
   */
  stringify(routes) {
    const qs = this.params
      .map(({ key, route, stringify, defaults, alwaysVisible }) => {
        const value = routes[key];
        if (value === undefined || (value === defaults() && !alwaysVisible)) {
          return;
        }
        return encodeURIComponent(route) + '=' + encodeURIComponent(stringify(value));
      })
      .filter(v => v)
      .join('&');

    return qs ? '?' + qs : '';
  }

  /**
   * 判断routes改后，是否只需要replace（即只替换，不生成新的历史记录）
   * @param {*} a
   * @param {*} b
   */
  shouldReplace(a, b) {
    return !this.params.some(({ key, history }) => {
      if (a[key] === b[key]) {
        return;
      }
      if (history) {
        return true;
      }
    });
  }
}

enum Types {
  UPDATE,
  SET,
  CHANGE,
  READY,
}

interface Options extends DuckOptions {
  /** url参数定义 */
  params?: any[];
  /** 基本路由 */
  baseUrl?: string;
}

export default class RouteDuck extends BaseDuck {
  protected options: Options;
  protected initialState: any;
  private parser: QSParser;

  constructor(options: Options) {
    super(options);
    // 提供延后初始化的能力，防止别人没有初始化完时，params计算会死循环（死锁）的问题
    const params = this.options.params;
    if (params && params.length) {
      this.setParams(params);
    }
  }

  get quickTypes() {
    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducer() {
    const { types, parser } = this;
    return (state = {}, action): any => {
      switch (action.type) {
        case types.UPDATE:
          return {
            ...state,
            ...parser.formula(action.payload),
          };
        case types.SET:
          return parser.formula(action.payload);
        default:
          return state;
      }
    };
  }

  get params(): Param[] {
    return this.options.params || [];
  }

  get creators() {
    const { types, parser } = this;
    return {
      ...super.creators,
      /** 用于业务主动更新route */
      update: routes => ({
        type: types.UPDATE,
        payload: routes,
      }),
      /** 用于nmc跳转响应 */
      set: routes => ({
        type: types.SET,
        payload: parser.formula(routes),
      }),
    };
  }

  *saga() {
    yield* super.saga();
    yield fork([this, this.sagaFromRouteWatch]);
    yield fork([this, this.sagaToRouteWatch]);
  }

  // 监听路由 nmcRouter => RouteDuck
  *sagaFromRouteWatch(duck = this) {
    const { options, creators, watch } = duck;
    // 如果没有定义baseUrl，不启用
    if (!options.baseUrl) {
      return;
    }
    // yield take(INIT);
    const chan: Channel<string> = yield call(watch, duck);
    yield fork(function*() {
      try {
        while (1) {
          // TODO 要判断是不是router自己设置的 --- 好像一样无解，只能判断相同时不触发联动来规避
          const url = yield take(chan);
          if (duck.parser) {
            const routes = duck.parser.parse(url);
            yield put(creators.set(routes));
          }
        }
      } finally {
        chan.close();
      }
    });
    //
    yield take(END);
    chan.close();
  }

  // 当路由变化时，通过action通知外界，同时尝试更新router
  // RouteDuck => CHANGED action & nmcRouter
  *sagaToRouteWatch(duck = this) {
    const { types, selector, options, perform } = duck;
    // 如果没有定义baseUrl，不启用
    if (!options.baseUrl) {
      return;
    }
    // 第一次强制更新
    let lastRoutes = {}; //yield select(selector)
    yield takeLatest([INIT, types.SET, types.UPDATE], function*(action) {
      // yield call(delay, 100)
      if (!duck.parser) {
        return;
      }
      const curRoutes = yield select(selector);

      const changes = {};
      // 原有属性有变更的（删、改）
      Object.keys(lastRoutes).forEach(key => {
        const value = curRoutes[key];
        if (
          value !== lastRoutes[key] &&
          // NaN、数组或对象的对比会有问题，进行更深的判断
          JSON.stringify(value) !== JSON.stringify(lastRoutes[key])
        ) {
          changes[key] = value;
        }
      });
      // 新增的属性
      Object.keys(curRoutes)
        .filter(key => !(key in lastRoutes))
        .forEach(key => {
          changes[key] = curRoutes[key];
        });
      // 如果有变动，则更新之
      if (Object.keys(changes).length > 0) {
        // 如果是业务逻辑自己局部更新的，不触发change？
        if (action.type !== types.UPDATE) {
          yield put({
            type: types.CHANGE,
            payload: curRoutes,
            changes,
          });
        }

        // 同时更新url
        yield call(perform, duck, duck.parser.stringify(curRoutes), duck.parser.shouldReplace(lastRoutes, curRoutes));
      }

      lastRoutes = curRoutes;
    });
  }

  setParams(params: Param[]): void {
    this.parser = new QSParser(params);
    const initialState = {};
    this.parser.params.forEach(({ key, defaults }) => {
      initialState[key] = defaults();
    });
    this.initialState = initialState;
  }

  /**
   * 监听URL变化
   * @param param
   */
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  protected watch({ options }: this): Channel<string> {
    return eventChannel(emitter => {
      const handler = () => {
        emitter(window.location.search);
      };
      if (window.history && history.pushState && window.addEventListener) {
        window.addEventListener('popstate', handler);
      }
      emitter(window.location.search);

      return () => {
        window.removeEventListener && window.removeEventListener('popstate', handler);
      };
    }, buffers.sliding(1));
  }

  /**
   * 实施URL变化
   */
  protected perform(_this, params: string, replace: boolean) {
    if (window.history && history.pushState) {
      // 如果完全一样，可能是popState，这时应该什么操作都不做
      if (params === document.location.search) {
        return;
      }
      if (replace) {
        history.replaceState(null, document.title, document.location.pathname + params);
      } else {
        history.pushState(null, document.title, document.location.pathname + params);
      }
    }
  }
}
