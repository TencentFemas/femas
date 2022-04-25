import { createToPayload, Duck } from 'saga-duck';
import { cancel, fork, put, select } from 'redux-saga/effects';
import { takeEvery, takeLatest } from 'redux-saga-catch';

enum DynamicDuckTypes {
  CREATE_DUCK,
  CLEAR,
}

const SLAVE_DUCK_INIT_TYPE = '@@INIT@@';

export default abstract class DynamicDuck extends Duck {
  private DuckPool: Record<string, InstanceType<this['ProtoDuck']>> = {};

  abstract get ProtoDuck(): new (...args: any) => Duck;

  get quickTypes() {
    return {
      ...super.quickTypes,
      ...DynamicDuckTypes,
    };
  }

  get rawTypes() {
    return {} as InstanceType<this['ProtoDuck']>['types'];
  }

  get reducer(): any {
    return (state = {}, action) => {
      if (action.type.startsWith(this.actionTypePrefix)) {
        const key = action.type.slice(this.actionTypePrefix.length).split('/')[0];
        if (key in this.DuckPool) {
          const newState = this.DuckPool[key].reducer(state[key], action);
          if (state[key] !== newState) {
            return {
              ...state,
              [key]: this.DuckPool[key].reducer(state[key], action),
            };
          }
        }
      }
      if (action.type === this.types.CLEAR) return {};
      return state;
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      createDuck: createToPayload<string>(types.CREATE_DUCK),
      clear: createToPayload<void>(types.CLEAR),
    };
  }

  *saga() {
    yield* super.saga();
    type ProtoDuck = this['ProtoDuck'];
    const duck = this;
    let namespace = '';
    if (duck.actionTypePrefix.endsWith('/')) {
      namespace = duck.actionTypePrefix.slice(0, -1);
    }

    const target = new duck.ProtoDuck({
      route: '',
      namespace: namespace,
    }) as InstanceType<ProtoDuck>;

    for (const key in target.types) {
      this.types[key] = target.types[key];
    }

    yield takeEvery(this.types.CREATE_DUCK, function* createDuck(action) {
      // 创建动态Duck
      const id = duck.generateId(action.payload);
      if (id in duck.DuckPool) return;
      const parentSelector = duck.selector;
      const target = new duck.ProtoDuck({
        route: id,
        namespace,
        selector: state => {
          return parentSelector(state)[id] || {};
        },
      }) as InstanceType<ProtoDuck>;
      duck.DuckPool[id] = target;
      yield put({ type: `${namespace}/${id}/${SLAVE_DUCK_INIT_TYPE}` });
      duck.DuckPool[id]['_sagaHandler'] = yield fork([target, target.saga]);
    });

    // yield takeLatest(
    //   (action: Action<any>) =>
    //     action.type.startsWith(namespace) &&
    //     Object.values(duck.types).indexOf(action.type) < 0,
    //   function* (action: Action<any>) {
    //     // 向上汇聚
    //     const actionType = action.type.slice(namespace.length + 1);
    //     const slaveId = actionType.split("/")[0];
    //     if (!(slaveId in duck.DuckPool)) return;
    //     const subType = actionType.slice(slaveId.length + 1);
    //     if (subType === SLAVE_DUCK_INIT_TYPE) return;
    //     yield put({
    //       type: `${namespace}/${subType}`,
    //       payload: action.payload,
    //       slaveId,
    //     });
    //   }
    // );
    yield takeLatest(this.types.CLEAR, function*() {
      for (const duckId in duck.DuckPool) {
        yield cancel(duck.DuckPool[duckId]['_sagaHandler']);
      }
      delete duck.DuckPool;
      duck.DuckPool = {};
    });
  }

  generateId(seed) {
    return `Dynamic-${seed}`;
  }

  /**
   * 仅仅从selectors中那取
   * @param key
   */
  *getValueMap(key: string) {
    const res = {};
    const state = yield select();
    for (const id in this.DuckPool) {
      if (this.DuckPool[id].selectors[key]) res[id] = this.DuckPool[id].selectors[key](state);
    }
    return res;
  }

  getDucks() {
    return this.DuckPool;
  }

  getDuck(seed: string): InstanceType<this['ProtoDuck']> {
    const id = this.generateId(seed);
    return this.DuckPool[id];
  }
}

// 测试/示例代码
// if (process.env.NEVER_RUN) {
//   class DuckA extends Duck {}
//   class TestDuck extends DynamicDuck {
//     get ProtoDuck() {
//       return DuckA
//     }
//   }

//   const foo = new TestDuck()
//   foo.creators.createDuck('1')
//   const duck1 = foo.getDuck('1')

//   foo.creators.createDuck('2')
//   const duck2 = foo.getDuck('2')

//   duck1 !== duck2 //true

//   const DuckMap = foo.getDucks()
//   DuckMap['1'] === duck1 //true
//   DuckMap['2'] === duck2 //true
// }
