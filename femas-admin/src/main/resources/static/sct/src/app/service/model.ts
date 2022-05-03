import { apiRequest, APIRequestOption, getAllList } from '../../common/util/apiRequest';
import { metricRequest } from '../apm/model';
import { InterfaceItem } from './detail/api/types';
import { EventItem } from './detail/event/types';
import { InstanceItem, ServiceItem } from './types';

export async function serviceRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: 'service',
  });
  return res;
}

interface Params {
  namespaceId: string;
  pageNo?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
  serviceVersion?: string;
}

export async function describeRegisterService(params: Params) {
  const res = await serviceRequest<{
    count: number;
    serviceBriefInfos: Array<ServiceItem>;
    registryId: string;
  }>({
    action: 'describeRegisterService',
    data: params,
  });

  return {
    list:
      res?.serviceBriefInfos?.map(item => ({
        ...item,
        id: item.serviceName,
        namespaceId: params.namespaceId,
        registryId: res.registryId,
      })) || [],
    totalCount: res?.count || 0,
  };
}

export async function describeServiceOverview(params) {
  return serviceRequest({ action: 'describeServiceOverview', data: params });
}

export async function fetchAllServiceList(params) {
  return getAllList(describeRegisterService)(params);
}

interface InstanceParams extends Params {
  serviceName: string;
}

export async function describeServiceInstance(params: InstanceParams) {
  const res = await serviceRequest<{
    count: number;
    data: Array<InstanceItem>;
  }>({
    action: 'describeServiceInstance',
    data: params,
  });

  return {
    list:
      res?.data?.map((item, i) => ({
        ...item,
        id: `${item.id}-${i}`,
        namespaceId: params.namespaceId,
        serviceName: params.serviceName,
      })) || [],
    totalCount: res?.count || 0,
  };
}

export async function describeServiceApi(params: InstanceParams) {
  const res = await serviceRequest<{
    count: number;
    data: Array<InterfaceItem>;
  }>({
    action: 'describeServiceApi',
    data: params,
  });

  return {
    list:
      res?.data?.map(item => ({
        ...item,
        id: `${item.path}-${item.method}-${item.serviceVersion}`,
        namespaceId: params.namespaceId,
        serviceName: params.serviceName,
      })) || [],
    totalCount: res?.count || 0,
  };
}

export async function fetchAllInterfaceList(params) {
  return getAllList(describeServiceApi)(params);
}

export async function describeServiceEvent(
  params: InstanceParams & {
    startTime?: number;
    endTime?: number;
    eventType?: string;
  },
) {
  const res = await serviceRequest<{
    count: number;
    data: Array<EventItem>;
  }>({
    action: 'describeServiceEvent',
    data: params,
  });

  return {
    list:
      res?.data?.map(item => ({
        ...item,
        namespaceId: params.namespaceId,
        serviceName: params.serviceName,
      })) || [],
    totalCount: res?.count || 0,
  };
}

export async function fetchEventType(params) {
  return serviceRequest({ action: 'fetchEventType', data: params });
}

export async function fetchRateLimitMetric(params) {
  return metricRequest({ action: 'fetchRateLimitMetric', data: params });
}

export async function fetchRouteMetric(params) {
  return metricRequest({ action: 'fetchRouteMetric', data: params });
}
