/**
 * 组合ducks/Form与ducks/DialogPure
 *
 * 使用示例



 */
import Base from '../ducks/DialogPure';
import Form from '../ducks/Form';
import { call, put, select } from 'redux-saga/effects';
import { reduceFromPayload } from 'saga-duck';

interface FormClass {
  new (...any: any[]): Form;
}

export default abstract class FormDialog extends Base {
  Options: any;
  Data: this['FormValues'];

  /** 表单数据结构 */
  // abstract FormValues
  get FormValues(): InstanceType<this['Form']>['Values'] {
    return null;
  }

  /** 对应的表单Duck - 子类扩展实现 */
  abstract get Form(): FormClass;

  get quickTypes() {
    return {
      ...super.quickTypes,
      SET_OPTIONS: 1,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      options: reduceFromPayload<this['Options']>(types.SET_OPTIONS, null),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      options: (state: State) => state.options,
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      form: this.Form as this['Form'],
    };
  }

  /** 提交逻辑 - 子类扩展实现 */
  abstract onSubmit(values: this['FormValues'], initialValues: this['Data'], options: this['Options']);

  /**
   * 开始执行表单弹窗逻辑
   * @param data 表单初始值
   * @param options 弹窗自定义配置（TODO: 或考虑直接传入Form作为Meta使用）
   */
  *execute(data: Partial<this['Data']>, options: this['Options'] = null) {
    const duck = this;
    const { selector } = duck;
    const { options: oldOptions } = selector(yield select());
    if (options !== oldOptions) {
      yield put({
        type: duck.types.SET_OPTIONS,
        payload: options,
      });
    }
    return yield this.show(data, function*() {
      const state = selector(yield select());
      const values = state.form.values;

      yield call([duck, duck.onSubmit], values, data, state.options);
    });
  }

  // 初始化
  *beforeShow(data) {
    yield* super.beforeShow(data);
    const duck = this;
    const {
      ducks: {
        form: { creators },
      },
    } = duck;
    yield put(creators.setAllTouched(false));
    yield put(creators.setValues(data, true));
  }

  // 有错误不提交
  *beforeSubmit() {
    const {
      ducks: { form },
    } = this;
    const { creators, selectors } = form as Form;
    const invalid = selectors.firstInvalid(yield select());
    if (invalid) {
      yield put(creators.setAllTouched());
      throw invalid;
    }
  }
}
