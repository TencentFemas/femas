import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { createLogger } from 'redux-logger';
import { call, cancel, fork, take } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import { DuckCmpProps, DuckOptions, DuckRuntime } from 'saga-duck';
import Dialog from '../ducks/DialogPure';
import waitVisible from '../helpers/visibility';
import { combineWaitFunction, makeWaitFunction } from './wait';

interface DuckClass<TDuck> {
  new (options?: DuckOptions): TDuck;
}

/**
 * 展示窗口，独立运行环境，单实例（即后面调用的会覆盖前面的）
 * 设计于比较独立的弹窗操作，使得不再需要组装Duck对应的React组件
 * 当窗口隐藏（types.HIDE）时，自动销毁
 *
 * 建议使用时捕获DuckRuntime被销毁的动作
 * ```
 * try{
 *  const result = yield* duck.show(data, doSave)
 *  resolve(result)
 * }finally{
 *  resolve(false)
 * }
 * ```
 * @param Component
 * @param Duck
 * @param saga
 */
export function showDialog<TDuck extends Dialog>(
  Component: React.ComponentClass<DuckCmpProps<TDuck>> | React.StatelessComponent<DuckCmpProps<TDuck>>,
  Duck: DuckClass<TDuck>,
  saga?: (duck: TDuck) => any,
) {
  // duck内进行dispose有可能导致递归，放在下次展示时清理
  // 因为DialogDuck本身会让对话框隐藏
  if (disposer) {
    disposer();
    disposer = null;
  }
  const middlewares = process.env.NODE_ENV === 'development' ? [createLogger({ collapsed: true })] : [];
  const duck = new Duck({
    namespace: 'OrphanDialog',
    route: '',
    selector(a) {
      return a;
    },
  });
  const duckRuntime = new DuckRuntime(duck, ...middlewares);
  const ConnectedComponent = duckRuntime.connectRoot()(Component);
  duckRuntime.addSaga([
    // 隐藏时取消saga，react组件就随意了
    function*() {
      try {
        yield fork(saga, duck);
        yield take(duck.types.HIDE);
        yield call(delay, 100);
        yield cancel();
        // 这里直接unmount会报错，相当于递归调用了，还是只清理saga逻辑吧
        // disposer && disposer()
      } finally {
        // 标记窗口已关闭
        fireVisibleChange(false);
      }
    },
  ]);
  disposer = showReactDialog(
    <Provider store={duckRuntime.store}>
      <ConnectedComponent />
    </Provider>,
  );
}

let disposer: () => void;

/**
 * 展示纯React弹窗
 * @param element
 */
export function showReactDialog(element: React.ReactElement) {
  const el = _getDialogRootDom();
  ReactDOM.render(element, el);
  fireVisibleChange(true);

  return () => {
    fireVisibleChange(false);
    // 有时unmount会报错，进行try...catch防止中止新弹窗
    try {
      ReactDOM.unmountComponentAtNode(el);
    } catch (e) {
      // 打印出来方便定位
      console.error(e);
    }
  };
}

// 唯一dom
function _getDialogRootDom() {
  const id = '_saga_duck_singleton_dialog_';
  let el = document.getElementById(id);
  if (!el) {
    el = document.createElement('div');
    el.id = id;
    document.body.appendChild(el);
  }
  return el;
}

// 窗口展示标记
let duckDialogVisible = false;
type Watcher = (visible: boolean) => any;
const duckDialogVisibleWatchers = new Set<Watcher>();

function fireVisibleChange(visible: boolean) {
  duckDialogVisible = visible;
  for (const watcher of duckDialogVisibleWatchers) {
    watcher(visible);
  }
}

function watchVisibleOnce(callback: () => any) {
  function cb() {
    duckDialogVisibleWatchers.delete(cb);
    callback();
  }

  duckDialogVisibleWatchers.add(cb);
}

/**
 * 确认当前弹窗都处于关闭状态（仅限此模块的showDialog创建的弹窗）
 * 如果没有弹窗，以true立即完成
 * 否则，等待弹窗关闭后再以false完成
 */
export const waitDialogHide = makeWaitFunction(() => !duckDialogVisible, watchVisibleOnce, 1000);

/**
 * 确认当前页面主体为可见状态
 */
export async function waitBodyVisible() {
  return combineWaitFunction([waitVisible, waitDialogHide]);
}

/**
 * 如果有第三方的弹窗，可以在这里记录状态，
 * 以保证waitBodyVisible功能正常使用
 *
 * tag-sdk实测无法使用此特性，因为只有提交时回调才会触发，没有办法得知窗口关闭或取消
 */
export function markDialogVisible() {
  fireVisibleChange(true);
  return function disposer() {
    fireVisibleChange(false);
  };
}
