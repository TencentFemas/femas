import { apiRequest, APIRequestOption } from '@src/common/util/apiRequest';
import { RegistryItem } from './types';

export async function registryRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: 'registry',
  });
  return res;
}

export async function describeRegistryClusters(data?: { registryType?: string; status?: number }) {
  const res = await registryRequest<Array<RegistryItem>>({
    action: 'describeRegistryClusters',
    data,
  });

  return {
    list: res?.map(item => ({ ...item, id: item.registryId })) || [],
    totalCount: res?.length || 0,
  };
}

export async function describeRegistryCluster(param: Pick<RegistryItem, 'registryId'>) {
  const res = await registryRequest<ClusterInfo>({
    action: 'describeRegistryCluster',
    data: param,
  });

  return res || null;
}

export interface ClusterInfo {
  clusterServers: Array<{
    clusterRole: string;
    lastRefreshTime: number;
    serverAddr: string;
    state: string;
    term: string;
  }>;
  config: RegistryItem;
}

export async function configureRegistry(params: Partial<RegistryItem>) {
  const res = await registryRequest<boolean>({
    action: 'configureRegistry',
    data: params,
  });

  return res;
}

export async function deleteRegistryCluster(params: Pick<RegistryItem, 'registryId'>) {
  const res = await registryRequest<boolean>({
    action: 'deleteRegistryCluster',
    data: params,
  });

  return res;
}

export async function checkCertificateConf(params: Partial<RegistryItem>) {
  try {
    const res = await registryRequest<boolean>({
      action: 'checkCertificateConf',
      data: params,
    });
    return {
      success: res,
      message: '',
    };
  } catch (e) {
    return {
      success: false,
      message: e.message,
    };
  }
}
