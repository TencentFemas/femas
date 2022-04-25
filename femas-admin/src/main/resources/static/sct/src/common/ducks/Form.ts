/**
 * 基于redux saga-duck的通用表单实现
 *
 * 特性：
 * 1 遵循duck传统，纯逻辑层，可脱离UI运行。
 * 2 简化配置，不需要配置过多（如action、reducer等）
 * 2.1 简化的基础上，需要能实现灵活的saga逻辑开发，能自订指定表单项的对应action
 * 3 仿已有的QcForm API，使后续UI层开发切换成本降低
 *
 * 使用示例：

 export interface Values {
  name: string
  port: number
}
 const validator = Base.combineValidators<Values>({
  name(v, values) {
    if (!v) {
      return '不能为空'
    }
  }
})
 export default class Duck extends Base {
  Values: Values
  validate(v: this['Values']) {
    return validator(v)
  }
}

 */
import { createToPayload, DuckMap as Base } from 'saga-duck';
import { call, fork, put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import * as merge from 'lodash.merge';
import { createSelector } from 'reselect';
import { isValidElement, ReactChild } from 'react';
import { runAndWatchLatest, watchLatest } from '../helpers/saga';
import { warn } from '../helpers/log';
import { once } from '../helpers/cacheable';

const warnOnce = once((msg: string) => {
  warn(msg);
  return true;
});

type INVALID = ReactChild;
type CONVERT_MAP<TValues, TValue> = {
  [key in keyof TValues]?: TValues[key] extends Array<infer T>
    ? CONVERT_MAP<T, TValue>[]
    : TValues[key] extends object
    ? CONVERT_MAP<TValues[key], TValue>
    : TValue;
};
// 将值对象转换为错误对象（可递归）
type INVALIDS<T> = CONVERT_MAP<T, INVALID>;
type TOUCHES<T> = CONVERT_MAP<T, boolean>;

type VALIDATOR<TValue, TMeta = any, TValues = any> = (v: TValue, data?: TValues, meta?: TMeta) => any; //INVALID | VALIDATORS<TValue, TMeta>
type VALIDATORS<TValues, TMeta, TParentValues = TValues> =
  | VALIDATOR<TValues, TMeta, TParentValues>
  | {
      [key in keyof TValues]?: TValues[key] extends Array<infer T>
        ? [VALIDATORS<T, TMeta, TValues[key]>] | VALIDATORS<TValues[key], TMeta, TValues>
        : TValues[key] extends object
        ? VALIDATORS<TValues[key], TMeta, TValues>
        : VALIDATOR<TValues[key], TMeta, TValues>;
    };
type FIELD_KEY = string | number;

enum Types {
  SET_VALUE,
  DELETE_VALUE,
  SPLICE_VALUE,
  SET_META,
  SET_TOUCHED,
  SET_ALL_TOUCHED,
  SET_INVALID,
}

export default class Form extends Base {
  /** 表单值类型声明 */
  Values;
  /** 表单校验额外配置类型声明 */
  Meta;

  /** 表单校验结果类型 */
  get Invalids(): INVALIDS<this['Values']> {
    return null;
  }

  get quickTypes() {
    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducers() {
    const { types } = this;
    type Values = this['Values'];
    type Meta = this['Meta'];
    return {
      ...super.reducers,
      values: (state = null, action): Values => {
        switch (action.type) {
          case types.SET_VALUE:
            // 字段更新
            if (action.path) {
              return update(state, action.path, action.payload);
            } else {
              // 整体更新
              return action.payload;
            }
          case types.DELETE_VALUE:
            return deleteByPath(state, action.path);
          case types.SPLICE_VALUE:
            return spliceByPath(state, action.path, action.index, action.count, ...(action.values || []));
          default:
            return state;
        }
      },
      validateMeta: (state = {}, action): Meta => {
        switch (action.type) {
          case types.SET_META:
            return action.payload;
          default:
            return state;
        }
      },
      invalids: (state = null, action): INVALIDS<Values> => {
        switch (action.type) {
          case types.SET_INVALID:
            // 字段更新
            if (action.path) {
              return update(state, action.path, action.payload);
            } else {
              // 整体更新
              return action.payload;
            }
          case types.SET_VALUE:
            if (action.reset) {
              return null;
            }
            return state;
          case types.DELETE_VALUE:
            return deleteByPath(state, action.path);
          case types.SPLICE_VALUE:
            return spliceByPath(
              state,
              action.path,
              action.index,
              action.count,
              // eslint-disable-next-line @typescript-eslint/no-unused-vars
              ...(action.values || []).map(o => undefined),
            );
          default:
            return state;
        }
      },
      touches: (state = null, action): TOUCHES<Values> => {
        switch (action.type) {
          case types.SET_TOUCHED:
            // 字段更新
            if (action.path) {
              return update(state, action.path, action.payload);
            } else if (action.update) {
              return Object.assign({}, state, action.update);
            } else {
              // 整体更新
              return action.payload;
            }
          case types.SET_VALUE:
            if (action.reset) {
              return null;
            }
            return state;
          case types.DELETE_VALUE:
            return deleteByPath(state, action.path);
          case types.SPLICE_VALUE:
            return spliceByPath(
              state,
              action.path,
              action.index,
              action.count,
              // eslint-disable-next-line @typescript-eslint/no-unused-vars
              ...(action.values || []).map(o => undefined),
            );
          default:
            return state;
        }
      },
      allTouched: (state = false, action): boolean => {
        switch (action.type) {
          case types.SET_ALL_TOUCHED:
            return action.payload;
          case types.SET_VALUE:
            return action.reset ? false : state;
          default:
            return state;
        }
      },
    };
  }

  get creators() {
    const { types } = this;
    type Values = this['Values'];
    type Meta = this['Meta'];
    return {
      ...super.creators,
      /** 设置整个表单的值 */
      setValues(value: Values, reset = true) {
        return {
          type: types.SET_VALUE,
          payload: value,
          reset,
        };
      },
      setMeta: createToPayload<Meta>(types.SET_META),
      /** 设置某一项的值 */
      setValue(field: FIELD_KEY | FIELD_KEY[], value: any) {
        return {
          type: types.SET_VALUE,
          path: [].concat(field),
          payload: value,
        };
      },
      /** 标记整个表单的错误 */
      markInvalids: createToPayload<INVALIDS<Values>>(types.SET_INVALID),
      /** 标记某一项的错误 */
      markInvalid(field: FIELD_KEY | FIELD_KEY[], invalid: INVALID) {
        return {
          type: types.SET_INVALID,
          path: [].concat(field),
          payload: invalid,
        };
      },
      /** 设置表单已编辑完 */
      setAllTouched(touched = true) {
        return {
          type: types.SET_ALL_TOUCHED,
          payload: touched,
        };
      },
      /** 批量设置Touched，replace默认为false，即进行更新而不是替换 */
      setTouched(touches: TOUCHES<Values>, replace = false) {
        const action = { type: types.SET_TOUCHED } as any;
        if (replace) {
          action.payload = touches;
        } else {
          action.update = touches;
        }
        return action;
      },
    };
  }

  get rawSelectors() {
    type State = this['State'];
    type Values = this['Values'];
    type InvalidsSelector = (state: State) => INVALIDS<Values>;
    const invalidsSelector: InvalidsSelector = createSelector(
      (state: State) => state.invalids,
      (state: State) => this.rawSelectors.values(state),
      (state: State) => this.rawSelectors.validateMeta(state),
      (invalids, values, validateMeta) => {
        // 快速校验
        const autoInvalids = this.validate(values, validateMeta);
        // 手工标记错误
        // 合并结果
        const empty = Array.isArray(invalids) ? [] : {};
        return merge(empty, autoInvalids || empty, invalids || empty);
      },
    );
    return {
      ...super.rawSelectors,
      /** 供FieldAPI使用，可以扩展重写 */
      values: (state: State): this['Values'] => state.values,
      /** 表单校验额外信息，可以扩展重写 */
      validateMeta: (state: State): this['Meta'] => state.validateMeta,
      /** 表单错误信息，会将validate校验结果与invalids结合 */
      invalids: invalidsSelector,
      /** 表单是否包含错误 */
      firstInvalid: (state: State, fields: string[] = null) => {
        return getFirstInvalid(this.rawSelectors.invalids(state), fields);
      },
    };
  }

  /**
   * 绑定ActionType到某个属性的变更上，变化时触发，外部触发时也会同步更新到表单
   * @example```
   get actionMapping(): this['ActionMapping'] {
      return { name: 'NAME_CHANGED', section1: { property1: 'PROPERTY1_CHANGED' } }
    }
   * ```
   */
  get actionMapping(): CONVERT_MAP<this['Values'], string> {
    return null;
  }

  /** 类型声明 */
  get ActionMapping(): CONVERT_MAP<this['Values'], string> {
    return null;
  }

  /**
   * 合并各表单项的校验方法
   * @param validators
   */
  static combineValidators<TValues, TMeta = any>(
    validators: VALIDATORS<TValues, TMeta>,
  ): (v: TValues, meta?: TMeta) => INVALIDS<TValues> {
    // 数组支持，例如 combineValidators([notEmpty])(['']) => ['不能为空']
    if (Array.isArray(validators)) {
      type Item = TValues extends Array<infer T> ? T : never;
      let itemValidator: VALIDATOR<Item, TMeta, TValues>;
      const validator = validators[0];
      if (typeof validator === 'function') {
        itemValidator = validator;
      } else {
        const rootValidator = this.combineValidators(validator);
        itemValidator = (value, values, meta) => rootValidator(value, meta);
      }
      return (values, meta) => ((values as any) || []).map(item => itemValidator(item, values, meta));
    }
    // 对象递归
    const fields = Object.keys(validators);
    return (values, meta) => {
      // 值不可递归的，作为空对象处理
      values = values || ({} as TValues);
      const invalids = fields.reduce((results, field) => {
        // 校验器
        const validator = validators[field];
        // 值
        const value = values[field];
        // 如果是传统校验函数
        if (typeof validator === 'function') {
          const res = validator(value, values, meta);
          const resultValues = res ? Object.values(res) : [];
          // louis版的嵌套，返回validators
          if (resultValues.length && resultValues.every(r => typeof r === 'function')) {
            // eslint-disable-next-line @tencent/tea-i18n/no-bare-zh-in-js
            warnOnce(`[deprecated] 建议使用对象递归，而不在返回值内递归`);
            results[field] = this.combineValidators(res)(value, meta);
          } else {
            results[field] = res;
          }
        } else if (Array.isArray(validator) || typeof validator === 'object') {
          // 对象、数组递归校验
          // 如果值不可递归，跳过
          results[field] = this.combineValidators(validator)(value, meta);
        }
        return results;
      }, {});
      return invalids;
    };
  }

  static makeMapValidator<TValue = any, TMeta = any>(
    itemValidator: VALIDATOR<TValue, TMeta, Record<string, TValue>> | VALIDATORS<TValue, TMeta>,
  ): VALIDATOR<Record<string, TValue>, TMeta, any> {
    return (v: Record<string, TValue>, values: Record<string, TValue>, meta?: TMeta) => {
      let validator: VALIDATOR<TValue, TMeta, Record<string, TValue>>;
      if (typeof itemValidator === 'object') {
        const rootValidator = Form.combineValidators<TValue, TMeta>(itemValidator);
        validator = (v: TValue, values, meta) => {
          return rootValidator(v, meta);
        };
      } else {
        validator = itemValidator as VALIDATOR<TValue, TMeta, Record<string, TValue>>;
      }
      const invalids: Record<string, any> = {};
      for (const [key, value] of Object.entries(v || {})) {
        invalids[key] = validator(value, v, meta);
      }
      return invalids;
    };
  }

  *saga() {
    yield* super.saga();
    yield fork([this, this.sagaWatchSpecifiedAction]);
  }

  /**
   * 实现Action映射
   */
  *sagaWatchSpecifiedAction() {
    type Form = this;
    const duck = this;
    const { actionMapping } = duck;

    yield* walk(actionMapping);

    // 遍历actionMapping
    function* walk(
      mapping: object,
      field = duck.getAPI(null, () => null),
    ): IterableIterator<[FieldAPI<any, Form>, string]> {
      if (!mapping) {
        return;
      }
      for (const [key, value] of Object.entries(mapping)) {
        const thisField = field.getField(key as any);
        if (typeof value === 'string') {
          yield* thisField.sagaMapToAction(value);
        } else if (value) {
          yield* walk(value, thisField);
        }
      }
    }
  }

  /**
   * 校验
   */
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  validate(values: this['Values'], meta?: this['Meta']) {
    return null;
  }

  /**
   * 获取表单API，供React组件使用
   * @param store 全局store state
   * @param dispatch store.dispatch
   */
  getAPI(store: any, dispatch: (...any: any[]) => any): FieldAPI<this['Values'], this> {
    return new FieldAPI<this['Values'], this>(this, [], store, dispatch);
  }
}

const syncMark = Symbol('syncMark');

// 单一表单项操作API（供React UI组件使用）
export class FieldAPI<TValue, TForm extends Form = any> {
  constructor(
    protected duck: TForm,
    protected path: FIELD_KEY[] = [],
    protected state: any,
    protected dispatch: (...any: any[]) => any,
  ) {}

  setValue(v: TValue) {
    this.dispatch({
      type: this.duck.types.SET_VALUE,
      path: this.path,
      payload: v,
    });
  }

  getValue(): TValue {
    const values = this.duck.selectors.values(this.state);
    return pick(values, this.path);
  }

  setTouched(touched = true) {
    this.dispatch({
      type: this.duck.types.SET_TOUCHED,
      path: this.path,
      payload: touched,
    });
  }

  getTouched() {
    const { touches, allTouched } = this.duck.selector(this.state);
    return allTouched || pick(touches, this.path);
  }

  setError(error: INVALID) {
    this.dispatch({
      type: this.duck.types.SET_INVALID,
      path: this.path,
      payload: error,
    });
  }

  getError(): INVALID {
    const invalids = this.duck.selectors.invalids(this.state);
    return pick(invalids, this.path);
  }

  /**
   * @param fields 需要使用API的字段列表（目前没有很好的方案简化，先手工指定）
   */
  getFields(fields: Extract<keyof TValue, FIELD_KEY>[]): FieldAPIs<TValue, TForm> {
    const self = this;
    return fields.reduce((apis, field) => {
      Object.defineProperty(apis, field, {
        get() {
          return self.getField(field);
        },
        enumerable: true,
      });
      return apis;
    }, {}) as FieldAPIs<TValue, TForm>;
  }

  /**
   * 直接获取指定字段的API
   * @param fieldKey
   */
  getField<TKey extends Extract<keyof TValue, FIELD_KEY>>(fieldKey: TKey): FieldAPI<TValue[TKey], TForm> {
    return new FieldAPI(this.duck, this.path.concat(fieldKey), this.state, this.dispatch);
  }

  /**
   * 声明此项为数组
   */
  asArray() {
    type TItem = TValue extends Array<infer T> ? T : never;
    return new ArrayFieldAPI<TItem, TForm>(this.duck, this.path, this.state, this.dispatch);
  }

  /**
   * 声明此项为Record/Map
   */
  asMap() {
    type TRecord = TValue extends Record<string, any> ? TValue[''] : never;
    return new MapFieldAPI<TRecord, TForm>(this.duck, this.path, this.state, this.dispatch);
  }

  /**
   * 进行值映射转换，例如 FieldAPI<number> 转为 FieldAPI<string>
   */
  map<Target>(to: (v: TValue) => Target, from: (v: Target) => TValue) {
    const self = this;

    class TargetAPI extends FieldAPI<Target, TForm> {
      getValue() {
        return to(self.getValue());
      }

      setValue(v: Target) {
        return self.setValue(from(v));
      }
    }

    return new TargetAPI(self.duck, self.path, self.state, self.dispatch);
  }

  /**
   * 获取当前的值
   */
  *sagaGetValue() {
    return pick(this.duck.selector(yield select()), this.path);
  }

  /**
   * 设置当前的值
   * @param value
   */
  *sagaSetValue(value: TValue, isSync = false) {
    yield put({
      type: this.duck.types.SET_VALUE,
      path: this.path,
      payload: value,
      [syncMark]: isSync,
    });
  }

  /**
   * 进行onChange监听
   * @param sagaCb
   * @param initialCall
   */
  *sagaOnChange(
    sagaCb: (this: FieldAPI<TValue, TForm>, newValue: TValue, oldValue: TValue) => IterableIterator<any>,
    initialCall = false,
  ) {
    const { types, selectors } = this.duck;
    const helper = initialCall ? runAndWatchLatest : watchLatest;
    const self = this;
    let lastValue = null;
    yield helper(
      action => action.type === types.SET_VALUE && !action[syncMark],
      state => {
        return pick(selectors.values(state), this.path);
      },
      function*(newValue) {
        yield call([self, sagaCb], newValue, lastValue);
        lastValue = newValue;
      },
    );
  }

  /**
   * 将值变更映射为另一个action type
   * @param type
   */
  *sagaMapToAction(type: string) {
    const self = this;
    // Form -> Action
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    yield* this.sagaOnChange(function*(newValue, oldValue) {
      yield put({
        type,
        payload: newValue,
        [syncMark]: true,
      });
    });
    // Action -> Form
    yield takeLatest(
      action => action.type === type && !action[syncMark],
      function*(action) {
        yield* self.sagaSetValue(action.payload, true);
      },
    );
  }
}

/**
 * 数组类型的Field
 */
export class ArrayFieldAPI<TValue, TForm extends Form = any> extends FieldAPI<TValue[], TForm> {
  get length() {
    return this.getValue().length;
  }

  get(index: number) {
    return this.getField(index);
  }

  getValue() {
    return super.getValue() || [];
  }

  *[Symbol.iterator]() {
    const arr = this.getValue();
    for (const index of arr.keys()) {
      yield this.getField(index);
    }
  }

  *entries(): IterableIterator<[number, FieldAPI<TValue, TForm>]> {
    const arr = this.getValue();
    for (const index of arr.keys()) {
      yield [index, this.getField(index)];
    }
  }

  remove(index: number) {
    this.splice(index, 1);
  }

  push(...values: TValue[]) {
    this.splice(this.length, 0, ...values);
  }

  splice(index: number, deleteCount: number, ...add: TValue[]) {
    this.dispatch({
      type: this.duck.types.SPLICE_VALUE,
      path: this.path,
      index,
      count: deleteCount,
      values: add,
    });
  }

  insert(index: number, ...values: TValue[]) {
    this.splice(index, 0, ...values);
  }
}

/**
 * Map类型的Field
 */
export class MapFieldAPI<TValue, TForm extends Form = any> extends FieldAPI<Record<string, TValue>, TForm> {
  // ---- 实现 Map 类似的API
  get size() {
    return Object.keys(this.getValue()).length;
  }

  get(key: string) {
    return this.getField(key);
  }

  set(key: string, value: TValue) {
    this.get(key).setValue(value);
  }

  getValue() {
    return super.getValue() || ({} as Record<string, TValue>);
  }

  clear() {
    this.setValue({});
  }

  delete(key: string) {
    this.dispatch({
      type: this.duck.types.DELETE_VALUE,
      path: this.path.concat(key),
    });
    return true;
  }

  *entries(): IterableIterator<[string, FieldAPI<TValue, TForm>]> {
    for (const key of Object.keys(this.getValue())) {
      yield [key, this.getField(key)];
    }
  }

  has(key: string) {
    return key in this.getValue();
  }

  *keys() {
    for (const key of Object.keys(this.getValue())) {
      yield key;
    }
  }

  [Symbol.iterator]() {
    return this.entries();
  }
}

type FieldAPIs<V, T extends Form = any> = {
  [P in keyof V]: FieldAPI<V[P], T>;
};

// 根据路径获取值
function pick<T, TValues>(data: TValues, path: FIELD_KEY[]): T {
  return path.reduce((data, field) => {
    if (data !== null && typeof data === 'object' && field in data) {
      return data[field];
    }
    return null;
  }, data);
}

// 根据路径更新值，如果有变动，返回新的对象
function update<TValues, T = any>(data: TValues, path: FIELD_KEY[], value: T) {
  if (path.length <= 0) {
    return value;
  }
  const field = path[0];
  const isArray = typeof field === 'number';
  if (!data) {
    data = (isArray ? [] : {}) as TValues;
  }
  const oldData = data[field];
  let newData;
  if (path.length > 1) {
    newData = update(oldData, path.slice(1), value);
  } else {
    newData = value;
  }
  if (oldData !== newData) {
    // 数组也需要复制
    if (isArray) {
      // 不考虑故意传错的情况
      const arr = [].concat(data);
      arr[field] = newData;
      return arr;
    }
    // 浅复制对象
    return Object.assign({}, data, {
      [field]: newData,
    });
  }
  return data;
}

// 数组操作
function spliceByPath<V, T>(state: T, path: FIELD_KEY[], index: number, deleteCount: number, ...values: V[]) {
  const oldValue = pick<Array<V>, T>(state, path);
  // 如果本身数组未定义，并且仅插入占位元素，则不处理
  if (!oldValue && values.every(v => v === undefined)) {
    return state;
  }
  const arr = oldValue ? oldValue.slice(0) : new Array(index);
  arr.splice(index, deleteCount, ...values);
  return update(state, path, arr);
}

// 删除指定路径
function deleteByPath<T>(state: T, deletePath: FIELD_KEY[]) {
  const path = deletePath.slice(0);
  const lastKey = path.pop();
  // 要删除属性的host对象
  const oldValue: object = pick(state, path);
  // 如果本身就没有，无视
  if (!oldValue) {
    return state;
  }
  let newValue;
  if (typeof lastKey === 'number') {
    // 数组移除
    return spliceByPath(state, path, lastKey, 1);
  } else {
    // 属性移除
    // 如果本身就没有，无视
    if (!(lastKey in oldValue)) {
      return state;
    }
    newValue = {
      ...oldValue,
    };
    delete newValue[lastKey];
  }
  // 更新host对象
  return update(state, path, newValue);
}

/**
 * 判断错误对象是否包含错误
 * @param invalids
 * @param fields 指定从哪些字段中获取（目前只支持单层结构，不支持递归）
 */
function getFirstInvalid(invalids: any, fields: FIELD_KEY[] = null) {
  if (!invalids) {
    return;
  }
  // 直接返回 string或ReactElement，表明是错误
  if (typeof invalids === 'string' || isValidElement(invalids)) {
    return invalids;
  }
  // 数组形式
  if (Array.isArray(invalids)) {
    const arr = invalids as Array<any>;
    let firstInvalid;
    for (const field of fields || arr.keys()) {
      firstInvalid = getFirstInvalid(arr[field]);
      if (firstInvalid) {
        break;
      }
    }
    return firstInvalid;
  }
  // 传统对象，以及Map形式
  if (typeof invalids === 'object') {
    let firstInvalid;
    (fields || Object.keys(invalids)).some(v => (firstInvalid = getFirstInvalid(invalids[v])));
    return firstInvalid;
  }
  return invalids;
}
