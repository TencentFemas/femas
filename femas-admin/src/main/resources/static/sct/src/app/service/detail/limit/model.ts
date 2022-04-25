import { apiRequest, APIRequestOption } from '@src/common/util/apiRequest';
import { pingTagRuleContent } from '../utils';
import { LimitRuleItem } from './types';

interface Params {
  namespaceId: string;
  serviceName: string;
}

export async function limitRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: 'limit',
  });
  return res;
}

export async function fetchServiceLimitList(
  params: Params & {
    pageNo: number;
    pageSize: number;
    keyword?: string;
    type?: string;
  },
) {
  const result = await limitRequest<{
    count: number;
    data: Array<LimitRuleItem>;
  }>({
    action: 'fetchLimitRule',
    data: params,
  });

  for (const item of result.data) {
    item.dimensionsDesc = await pingTagRuleContent(item.tags);
  }

  return {
    list: result?.data || [],
    totalCount: result?.count || 0,
  };
}

export async function deleteLimitRule(params: Params & { ruleId: string }) {
  return limitRequest<boolean>({
    action: 'deleteLimitRule',
    data: params,
  });
}

export async function configureLimitRule(params: Partial<LimitRuleItem>) {
  return limitRequest<boolean>({
    action: 'configureLimitRule',
    data: params,
  });
}

export async function fetchLimitRuleById(params: Partial<LimitRuleItem>) {
  const result = await limitRequest<LimitRuleItem>({
    action: 'fetchLimitRuleById',
    data: params,
  });

  result.dimensionsDesc = await pingTagRuleContent(result.tags);

  return result;
}
