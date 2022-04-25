import { apiRequest, APIRequestOption } from '@src/common/util/apiRequest';
import { FuseRuleItem } from './types';

export async function breakerRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: 'breaker',
  });
  return res;
}

/**
 * 获取服务熔断规则列表
 */
export async function fetchList(params: FetchParams) {
  try {
    const result = await breakerRequest<{
      count: number;
      data: Array<FuseRuleItem>;
    }>({
      action: 'fetchBreakerRule',
      data: params,
    });
    const list = result.data;
    for (const item of list) {
      item.id = item.ruleId;
    }
    return {
      totalCount: result.count,
      list,
    };
  } catch (error) {
    throw error;
  }
}

interface FetchParams {
  isolationLevel?: string;
  namespaceId: string;
  pageNo: number;
  pageSize: number;
  serviceName: string;
}

/**
 * 编辑 规则
 * @param params
 * @returns
 */
export async function configureBreakerRule(params: Partial<FuseRuleItem>) {
  try {
    const result = await breakerRequest<boolean>({
      action: 'configureBreakerRule',
      data: params,
    });

    return result;
  } catch (error) {
    throw error;
  }
}

/**
 * 获取 单条规则
 */
export async function fetchBreakerRuleById(params: { namespaceId: string; ruleId: string; serviceName: string }) {
  try {
    const result = await breakerRequest<FuseRuleItem>({
      action: 'fetchBreakerRuleById',
      data: params,
    });

    return { ...result, id: result.ruleId };
  } catch (error) {
    throw error;
  }
}

/**
 * 获取 单条规则
 */
export async function deleteBreakerRule(params: { namespaceId: string; ruleId: string; serviceName: string }) {
  try {
    const result = await breakerRequest<boolean>({
      action: 'deleteBreakerRule',
      data: params,
    });

    return result;
  } catch (error) {
    throw error;
  }
}
