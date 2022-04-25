import { put, select, takeLatest } from 'redux-saga/effects';
import BaseForm from '@src/common/ducks/Form';
import { Tag } from '@src/app/apm/requestDetail/types';
import ServiceSelectDuck from '../../service/components/ServiceSelectDuck';

const validator = BaseForm.combineValidators<Values>({
  minDuration(v) {
    if (!v) return;
    if (v !== '0' && !/^([1-9]\d*)$/.test(v)) {
      return '请输入整数';
    }
  },
  maxDuration(v, values) {
    if (!v) return;
    if (v !== '0' && !/^([1-9]\d*)$/.test(v)) {
      return '请输入整数';
    }
    if (+v < +values.minDuration) {
      return '最大耗时不小于最小耗时';
    }
  },
  statusCode(v) {
    if (!v) return;
    if (!/^\d+(\.\d+)?$/.test(v)) {
      return '请输入正确的状态码';
    }
  },
});

export interface Values {
  startTime: string; //开始时间
  endTime: string; //结束时间
  service: string; // 途径服务
  traceId: string;
  api: string;
  callStatus: boolean; // 调用状态
  callType: string; // 调用类型
  statusCode: string;
  minDuration: string;
  maxDuration: string;
  tags: Array<Tag>; // 自定义标签
  showHigh: boolean;
  namespaceId: string;
  orderType?: string;
}

export enum OrderType {
  startTime = 'BY_START_TIME',
  duration = 'BY_DURATION',
}

export const ORDER_TYPE_NAME = {
  [OrderType.startTime]: '最新产生时间',
  [OrderType.duration]: '最长耗时排序',
};

export default class SearchFormDuck extends BaseForm {
  Values: Values;

  get quickTypes() {
    enum Types {
      LOAD_SERVICE,
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
      loadService: () => ({ type: types.LOAD_SERVICE }),
    };
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      serviceDuck: ServiceSelectDuck,
    };
  }

  validate(v) {
    return validator(v || {});
  }

  _handleData(data: Values) {
    data.service = data.service || '';
    data.callType = data.callType || '';
    data.tags = data.tags?.length ? data.tags : [{ key: '', value: '' }];
    data.orderType = data.orderType || OrderType.startTime;
    return data;
  }

  *initData(data) {
    const { creators } = this;
    yield put(creators.setAllTouched(false));
    yield put(creators.setValues(this._handleData(data)));
    yield put(creators.loadService());
  }

  *saga() {
    yield* super.saga();
    const {
      types,
      selectors,
      ducks: { serviceDuck },
    } = this;
    yield takeLatest(types.LOAD_SERVICE, function*() {
      const values = selectors.values(yield select());
      const { namespaceId } = values;
      // 加载service
      yield put(
        serviceDuck.creators.load({
          needEmptyItem: true,
          namespaceId,
          emptyItem: { serviceName: '', text: '全部', value: '' },
        }),
      );
      yield put(serviceDuck.creators.select(values.service));
    });
  }
}
