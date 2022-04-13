import { apiRequest, APIRequestOption, getAllList } from '../../common/util/apiRequest';
import { NamespaceItem } from './types';

export async function namespaceRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: 'namespace',
  });
  return res;
}

interface Params {
  registryId?: string; // registryId
  name?: string;
  pageNo: number;
  pageSize: number;
}

export async function fetchNamespaces(params: Params) {
  const res = await namespaceRequest<{
    count: number;
    data: Array<NamespaceItem>;
  }>({
    action: 'fetchNamespaces',
    data: params,
  });

  return {
    list:
      res?.data?.map(item => ({
        ...item,
        id: item.namespaceId,
        registryId: item.registry?.[0]?.registryId || '',
      })) || [],
    totalCount: res?.count || 0,
  };
}

export async function fetchAllNamespaceList(params) {
  return getAllList(fetchNamespaces)(params);
}

export async function createNamespace(params: Partial<NamespaceItem>) {
  const res = await namespaceRequest<boolean>({
    action: 'createNamespace',
    data: params,
  });

  return res;
}

export async function modifyNamespace(params: Partial<NamespaceItem>) {
  const res = await namespaceRequest<boolean>({
    action: 'modifyNamespace',
    data: params,
  });

  return res;
}

export async function deleteNamespace(params: Pick<NamespaceItem, 'namespaceId'>) {
  const res = await namespaceRequest<boolean>({
    action: 'deleteNamespace',
    data: params,
  });

  return res;
}

export async function fetchNamespaceById(params) {
  return namespaceRequest({
    action: 'fetchNamespaceById',
    data: params,
  });
}
