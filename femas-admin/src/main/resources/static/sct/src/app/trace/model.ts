import { apiRequest, APIRequestOption } from '@src/common/util/apiRequest';
import { Instance } from '../apm/requestDetail/types';

export async function traceRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: 'trace',
  });
  return res;
}

export async function describeSpans(params) {
  const result = await traceRequest<{
    total: number;
    traces: Array<Instance>;
  }>({
    action: 'describeBasicTraces',
    data: params,
  });
  return {
    list: result?.traces || [],
    totalCount: result?.total || 0,
  };
}
