import * as React from 'react';
import { put, select } from 'redux-saga/effects';
import { takeLatest } from 'redux-saga-catch';
import GridPage, { Filter as BaseFilter } from '@src/common/ducks/GridPage';
import { configureRouteRule, configureTolerant, deleteRouteRule, fetchServiceRouteList, fetchTolerant } from './model';
import { ComposedId } from '../types';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { RouteRuleItem } from './types';
import { Modal, notification, Text } from 'tea-component';
import { pingRuleFullContent } from '../utils';
import { RULE_STATUS } from '../../types';
import { FilterTime } from '@src/common/types';
import moment from 'moment';
import { caculateTimeDiff } from '@src/common/qcm-chart/core/lib/helper';
import formatDate from '@src/common/util/formatDate';
import { fetchRouteMetric } from '../../model';
import { MonitorData } from '@src/app/apm/types';

interface Filter extends BaseFilter {
  serviceName: string;
  namespaceId: string;
  registryId: string;
}

enum Types {
  LOAD,
  //更改服务路由开启/关闭状态
  CHANGE_STATUS,
  REMOVE, //删除,
  //试图改变路由保护策略
  TRY_SET_FB_ROUTE,
  SET_FALLBACK_ROUTE,
  SET_FALLBACK_ROUTE_LOADING,
  SET_FILTER_TIME,
  SET_MONITOR_LOADING,
  SET_MONITOR_DATA,
}

export default class PageDuck extends GridPage {
  baseUrl = null;
  Filter: Filter;
  Item: RouteRuleItem;

  get recordKey() {
    return 'ruleId';
  }

  get initialFetch() {
    return false;
  }

  get watchTypes() {
    return [...super.watchTypes, this.types.LOAD];
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
      composedId: reduceFromPayload(types.LOAD, {} as ComposedId),
      fallbackRoute: reduceFromPayload(types.SET_FALLBACK_ROUTE, false),
      fallbackRouteLoading: reduceFromPayload(types.SET_FALLBACK_ROUTE_LOADING, false),
      filterTime: reduceFromPayload(types.SET_FILTER_TIME, {} as FilterTime),
      monitorLoading: reduceFromPayload(types.SET_MONITOR_LOADING, true),
      monitorData: reduceFromPayload(types.SET_MONITOR_DATA, [] as Array<MonitorData>),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      load: createToPayload(types.LOAD),
      changeStatus: createToPayload(types.CHANGE_STATUS),
      remove: createToPayload(types.REMOVE),
      setFbRouteStatus: createToPayload(types.TRY_SET_FB_ROUTE),
      setFilterTime: createToPayload(types.SET_FILTER_TIME),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      filter: (state: State) => ({
        serviceName: state.composedId.serviceName,
        namespaceId: state.composedId.namespaceId,
        registryId: state.composedId.registryId,
        page: state.page,
        count: state.count,
        keyword: state.keyword,
      }),
      composedId: (state: State) => state.composedId,
      list: (state: State) => state.grid.list,
      fallbackRoute: (state: State) => state.fallbackRoute,
      fallbackRouteLoading: (state: State) => state.fallbackRouteLoading,
      filterTime: (state: State) => state.filterTime,
      monitorLoading: (state: State) => state.monitorLoading,
      monitorData: (state: State) => state.monitorData,
    };
  }

  *saga() {
    yield* super.saga();
    const duck = this;
    const { types, creators, selectors } = duck;
    // 查询微服务路由保护策略启停状态
    yield takeLatest(types.LOAD, function*() {
      const { serviceName, namespaceId } = selectors.composedId(yield select());
      const fallback = yield fetchTolerant({
        serviceName,
        namespaceId,
      });
      const { isTolerant } = fallback;
      yield put({
        type: types.SET_FALLBACK_ROUTE,
        payload: +isTolerant === RULE_STATUS.OPEN,
      });
    });
    //更改规则的启动状态
    yield takeLatest(types.CHANGE_STATUS, function*(action) {
      const confirm = yield Modal.confirm({
        message: '确定开启/关闭当前服务路由规则？',
        description: '同一个服务下，只能同时启用一条路由规则，其他路由规则将被自动关闭',
      });
      if (!confirm) return;

      const item = action.payload as RouteRuleItem;
      const { checked } = item;
      const curStatus = checked ? RULE_STATUS.OPEN : RULE_STATUS.CLOSE;
      try {
        yield configureRouteRule({
          ...item,
          status: curStatus,
        });
        yield put(creators.reload());
      } catch (e) {
        notification.error({
          description: '开启/关闭服务路由规则失败',
        });
      }
    });

    //删除模板
    yield takeLatest(types.REMOVE, function*(action) {
      const { serviceName, namespaceId } = selectors.composedId(yield select());
      const confirm = yield Modal.confirm({
        message: '确定删除当前服务路由规则？',
        description: '删除后，该服务路由规则将不再生效',
      });
      if (!confirm) return;
      yield deleteRouteRule({
        serviceName,
        namespaceId,
        ruleId: action.payload,
      });
      yield put(creators.reload());
    });

    yield takeLatest(types.TRY_SET_FB_ROUTE, function*(action) {
      const { serviceName, namespaceId } = selectors.composedId(yield select());
      yield put({
        type: types.SET_FALLBACK_ROUTE_LOADING,
        payload: true,
      });
      const isTolerant = action.payload ? RULE_STATUS.OPEN : RULE_STATUS.CLOSE;
      try {
        yield configureTolerant({
          serviceName,
          namespaceId,
          isTolerant,
        });
        yield put({
          type: types.SET_FALLBACK_ROUTE,
          payload: isTolerant === RULE_STATUS.OPEN,
        });
      } catch (e) {
        notification.error({ description: '切换失败' });
      } finally {
        yield put({
          type: types.SET_FALLBACK_ROUTE_LOADING,
          payload: false,
        });
      }
    });
    yield takeLatest(types.SET_FILTER_TIME, function*(action) {
      // 获取请求数据
      const { namespaceId, serviceName } = selectors.composedId(yield select());
      const { startTime, endTime } = action.payload;
      // 获取请求数据
      yield put({ type: types.SET_MONITOR_LOADING, payload: true });
      const step = caculateTimeDiff(startTime, endTime) > 86400 ? 300 : 60;
      const { flowMetric } = yield fetchRouteMetric({
        namespaceId,
        serviceName,
        startTime: +moment(startTime),
        endTime: +moment(endTime),
        step,
      });
      const monitorData = [];
      Object.keys(flowMetric)?.forEach(name => {
        monitorData.push({
          name,
          unit: '次',
          value: flowMetric[name]?.map(d => ({
            label: formatDate(d.time),
            value: d.value,
          })),
          additionalTip: ['统计方式：sum', `统计粒度：${step / 60}分钟`],
        });
      });

      yield put({ type: types.SET_MONITOR_DATA, payload: monitorData });
      yield put({ type: types.SET_MONITOR_LOADING, payload: false });
    });
  }

  async getData(filter: Filter) {
    const { page, count, namespaceId, serviceName, keyword, registryId } = filter;
    const result = await fetchServiceRouteList({
      pageNo: page,
      pageSize: count,
      namespaceId,
      serviceName,
      keyword,
    });
    //拼接规则的内容
    for (let i = 0; i < result.list.length; i++) {
      const ruleList = result.list[i].routeTag;

      const routeDesc = [];
      for (let index = 0; index < ruleList?.length; index++) {
        const ruleJsx = await pingRuleFullContent(ruleList[index]);
        routeDesc.push(
          <Text key={`routeDescTxt${index}`}>
            规则【{index + 1}】：{ruleJsx}&nbsp;&nbsp;&nbsp;&nbsp;
          </Text>,
        );
      }
      result.list[i].routeDesc = routeDesc;
    }
    return {
      totalCount: result.totalCount,
      list: result.list?.map(item => ({ ...item, registryId })) || [],
    };
  }
}
