import { apiRequest, APIRequestOption } from '@src/common/util/apiRequest';
import { RouteRuleItem, TolerantItem } from './types';

interface Params {
  namespaceId: string;
  serviceName: string;
}

export async function routeRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: 'route',
  });
  return res;
}

export async function fetchServiceRouteList(params: Params & { pageNo: number; pageSize: number; keyword?: string }) {
  const result = await routeRequest<{
    count: number;
    data: Array<RouteRuleItem>;
  }>({
    action: 'fetchRouteRule',
    data: params,
  });
  return {
    list: result?.data || [],
    totalCount: result?.count || 0,
  };
}

export async function deleteRouteRule(params: Partial<RouteRuleItem>) {
  return routeRequest<boolean>({
    action: 'deleteRouteRule',
    data: params,
  });
}

export async function configureRouteRule(params: Partial<RouteRuleItem>) {
  return routeRequest<boolean>({
    action: 'configureRouteRule',
    data: params,
  });
}

export async function fetchRouteRuleById(params: Partial<RouteRuleItem>) {
  return routeRequest<RouteRuleItem>({
    action: 'fetchRouteRuleById',
    data: params,
  });
}

export async function fetchTolerant(params: Partial<RouteRuleItem>) {
  return routeRequest<TolerantItem>({
    action: 'fetchTolerant',
    data: params,
  });
}

export async function configureTolerant(params: Partial<TolerantItem>) {
  return routeRequest<boolean>({
    action: 'configureTolerant',
    data: params,
  });
}
