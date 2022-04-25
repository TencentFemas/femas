import { put, select, takeLatest } from 'redux-saga/effects';
import BaseForm from '@src/common/ducks/Form';
import { Tag } from '@src/app/apm/requestDetail/types';
import { isValidIp } from '@src/common/util/check';
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
  clientInstanceIp(v) {
    if (v && !isValidIp(v)) {
      return '请输入正确的IP地址，如 255.0.0.0';
    }
  },
  serverInstanceIp(v) {
    if (v && !isValidIp(v)) {
      return '请输入正确的IP地址，如 255.0.0.0';
    }
  },
});

export interface Values {
  startTime: string; //开始时间
  endTime: string; //结束时间
  client: string; // 客户端
  server: string; // 服务端
  callStatus: boolean; // 返回状态
  clientApi: string; // 客户端接口
  serverApi: string; // 服务端接口
  callType: string; // 调用类型
  statusCode: string;
  minDuration: string;
  maxDuration: string;
  tags: Array<Tag>; // 自定义标签
  showHigh: boolean;
  kind?: string;
  clientInstanceIp: string; // 客户端IP
  serverInstanceIp: string; // 服务端IP
  traceId: string;
  spanId: string;
  namespaceId: string;
}

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
      serverService: ServiceSelectDuck,
      clientService: ServiceSelectDuck,
    };
  }

  validate(v) {
    return validator(v || {});
  }

  _handleData(data: Values) {
    data.client = data.client || '';
    data.server = data.server || '';
    data.callType = data.callType || '';
    data.tags = data.tags?.length ? data.tags : [{ key: '', value: '' }];
    return data;
  }

  *initData(data) {
    const {
      creators,
      ducks: { serverService, clientService },
    } = this;
    yield put(creators.setAllTouched(false));
    yield put(creators.setValues(this._handleData(data)));
    yield put(serverService.creators.select(data.server || ''));
    yield put(clientService.creators.select(data.client || ''));
    yield put(creators.loadService());
  }

  *saga() {
    yield* super.saga();
    const {
      types,
      selectors,
      ducks: { serverService, clientService },
    } = this;
    yield takeLatest(types.LOAD_SERVICE, function*() {
      const values = selectors.values(yield select());
      const { namespaceId } = values;
      // 加载service
      yield put(
        serverService.creators.load({
          needEmptyItem: true,
          namespaceId,
        }),
      );
      yield put(
        clientService.creators.load({
          needEmptyItem: true,
          namespaceId,
        }),
      );
    });
  }
}
