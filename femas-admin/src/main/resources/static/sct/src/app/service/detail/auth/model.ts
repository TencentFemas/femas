import { apiRequest, APIRequestOption } from '@src/common/util/apiRequest';
import { pingTagRuleContent } from '../utils';
import { AuthItem } from './types';

interface Params {
  namespaceId: string;
  serviceName: string;
}

export async function authRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: 'auth',
  });
  return res;
}

export async function fetchServiceAuthList(params: Params & { pageNo: number; pageSize: number; keyword?: string }) {
  const result = await authRequest<{
    count: number;
    data: Array<AuthItem>;
  }>({
    action: 'fetchAuthRule',
    data: params,
  });

  for (const item of result.data) {
    item.description = await pingTagRuleContent(item.tags);
  }

  return {
    list: result?.data || [],
    totalCount: result?.count || 0,
  };
}

export async function deleteAuthRule(params: Params & { ruleId: string }) {
  return authRequest<boolean>({
    action: 'deleteAuthRule',
    data: params,
  });
}

export async function configureAuthRule(params: Partial<AuthItem>) {
  return authRequest<boolean>({
    action: 'configureAuthRule',
    data: params,
  });
}

export async function getAuthRule(params: Partial<AuthItem>) {
  const result = await authRequest<AuthItem>({
    action: 'fetchAuthRuleById',
    data: params,
  });

  result.description = await pingTagRuleContent(result.tags);

  return result;
}
