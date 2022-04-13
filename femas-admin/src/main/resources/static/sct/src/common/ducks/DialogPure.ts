/**
 * 与DialogDuck相比，DialogPure将保存、提交逻辑移到外部实现
 */
import { DuckMap } from 'saga-duck';
import { call, cancel, fork, put, take } from 'redux-saga/effects';
import { notification } from 'tea-component';

enum Types {
  'SHOW',
  'UPDATE',
  'SET_DISABLED',
  'SUBMIT',
  'SUBMIT_DONE',
  'SUBMIT_FAIL',
  'HIDE',
}

export default abstract class DialogPure extends DuckMap {
  /** 弹窗数据类型定义 */
  abstract Data;
  /** SUBMIT payload 类型定义 */
  SubmitData;

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
      data: (state: this['Data'] = {}, action): this['Data'] => {
        switch (action.type) {
          case types.SHOW:
            return action.payload;
          case types.UPDATE:
            return Object.assign({}, state, action.payload);
          default:
            return state;
        }
      },
      error: (state = null, action): any => {
        switch (action.type) {
          case types.SUBMIT:
            return null;
          case types.SUBMIT_FAIL:
            return action.payload || new Error('提交失败');
          default:
            return state;
        }
      },
      visible: (state = false, action): boolean => {
        switch (action.type) {
          case types.SHOW:
            return true;
          case types.HIDE:
            return false;
          default:
            return state;
        }
      },
      submitting: (state = false, action): boolean => {
        switch (action.type) {
          case types.SUBMIT:
            return true;
          case types.SUBMIT_DONE:
          case types.SUBMIT_FAIL:
            return false;
          default:
            return state;
        }
      },
      disabled: (state = false, action): boolean => {
        switch (action.type) {
          case types.SET_DISABLED:
            return action.payload;
          case types.SHOW:
            return false;
          default:
            return state;
        }
      },
      // remove: dialogReducer(types.REMOVE),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      show: payload => ({ type: types.SHOW, payload }),
      update: payload => ({ type: types.UPDATE, payload }),
      submit: payload => ({ type: types.SUBMIT, payload }),
      hide: () => ({ type: types.HIDE }),
      setDisabled: payload => ({ type: types.SET_DISABLED, payload }),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      /** 是否可见 */
      visible: (state: State) => state.visible,
      /** 是否加载中 */
      loading: (state: State) => state.submitting,
      /** 能否提交 */
      canSubmit: (state: State) => !state.submitting && !state.disabled,
      /** 入参 */
      data: (state: State): this['Data'] => state.data,
    };
  }

  /** 在提交出错时，是否使用tips.error展示错误，默认为false */
  get tipError(): boolean {
    return false;
  }

  /**
   * 展示对话框
   * @param {*} data 传入数据
   * @param {*} doSave 保存流程，可以是async或saga
   * @return SagaEffect<finished>
   */
  *show(data: Partial<this['Data']>, doSave: (submitData: this['SubmitData'], data: this['Data']) => any) {
    return yield* this.withDialog(this, data, doSave);
  }

  /**
   * 展示前处理的任务，执行完后再触发SHOW action
   */
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  *beforeShow(data: Partial<this['Data']>): IterableIterator<any> {}

  /**
   * 展示中的时候处理的任务，隐藏时自动取消
   */
  *onShow(): IterableIterator<any> {}

  *withDialog(
    duck: this,
    data: Partial<this['Data']>,
    doSave: (submitData: this['SubmitData'], data: this['Data']) => any,
  ) {
    const { types, creators, tipError, beforeSubmit } = duck;
    yield* this.beforeShow(data);
    // 显示
    yield put(creators.show(data));
    const showingTask = yield fork([duck, duck.onShow]);
    // 等待提交，除非用户中止或被其它人抢占，否则这里永远不结束
    try {
      while (1) {
        const action = yield take([types.SUBMIT, types.SHOW, types.HIDE]);
        // 被抢占或用户取消
        if (action.type !== types.SUBMIT) {
          return false;
        }
        try {
          // 如果前置逻辑未通过，则不继续
          yield call([this, beforeSubmit]);
          yield call(doSave, action.payload, data);
          yield put({ type: types.SUBMIT_DONE });
          // 默认完成后都关闭
          yield put(creators.hide());
          // 成功
          return true;
        } catch (e) {
          // 提示错误
          if (tipError) {
            const message = (e.code ? '[' + e.code + '] ' : '') + e.message;
            // 有时直接 throw '' 用于阻止窗口关闭（CMQ发送消息时用到了）
            // 这时不展示，避免上报一次tips.error
            if (message) {
              notification.error({ description: message });
            }
          }
          yield put({ type: types.SUBMIT_FAIL, payload: e });
        }
      }
    } finally {
      yield cancel(showingTask);
    }
  }

  /** 供子类扩展，可抛出错误拦截提交操作 */
  beforeSubmit(): any {
    return null;
  }
}
