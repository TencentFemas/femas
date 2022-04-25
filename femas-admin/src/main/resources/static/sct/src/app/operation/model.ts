import { apiRequest, APIRequestOption } from '../../common/util/apiRequest';
import { OperationItem } from './types';

export async function logRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: 'log',
  });
  return res;
}

interface Params {
  endTime: number;
  pageNo: number;
  pageSize: number;
  startTime: number;
}

export async function fetchLogs(params: Params) {
  const res = await logRequest<{
    count: number;
    data: Array<OperationItem>;
  }>({
    action: 'fetchLogs',
    data: params,
  });

  return {
    list: res?.data?.map(item => ({ ...item, id: item.logId })) || [],
    totalCount: res?.count || 0,
  };
}
