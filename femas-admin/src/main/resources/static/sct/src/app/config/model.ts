import { apiRequest, APIRequestOption } from '../../common/util/apiRequest';
import { ConfigItem, VersionItem } from './types';

export async function configRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: 'dcfg',
  });
  return res;
}

interface Params {
  namespaceId: string;
  pageNo?: number;
  pageSize?: number;
  searchWord?: string;
}

export async function describeConfigs(params: Params) {
  const res = await configRequest<{
    count: number;
    data: Array<ConfigItem>;
  }>({
    action: 'fetchConfigs',
    data: params,
  });

  return {
    list: res?.data || [],
    totalCount: res?.count || 0,
  };
}

export async function fetchConfigById(params) {
  return configRequest({ action: 'fetchConfigById', data: params });
}

export async function fetchConfigVersions(params) {
  const res = await configRequest<{
    count: number;
    data: Array<VersionItem>;
  }>({
    action: 'fetchConfigVersions',
    data: params,
  });

  return {
    list: res?.data || [],
    totalCount: res?.count || 0,
  };
}

export async function deleteConfigs(params) {
  return configRequest({ action: 'deleteConfigs', data: params });
}

export async function configureConfig(params) {
  return configRequest({ action: 'configureConfig', data: params });
}

export async function operateConfigVersion(params) {
  return configRequest({ action: 'operateConfigVersion', data: params });
}

export async function deleteConfigVersions(params) {
  return configRequest({ action: 'deleteConfigVersions', data: params });
}
