import axios, { AxiosRequestConfig } from 'axios';
import { notification } from 'tea-component';
import { apiEndpoint, AuthenticationKey } from '../../config';
import { changeLetterLowCase } from './common';
import tips from './tips';
import resolvePath from './resolvePath';

export interface PageParam {
  pageNo: number;
  pageSize: number;
}

export interface APIRequestOption {
  serviceType?: string;
  action: string;
  data?: any;
  opts?: AxiosRequestConfig;
}

export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
}

export async function apiRequest<T>(options: APIRequestOption) {
  const { serviceType, action, data = {}, opts } = options;
  try {
    tips.showLoading({});
    const token = window.sessionStorage.getItem(AuthenticationKey);
    const res = await axios.post<ApiResponse<T>>(`${apiEndpoint}/${serviceType}/${action}`, data, {
      ...opts,
      headers: {
        ...(token
          ? {
              token,
            }
          : {}),
      },
    });
    if (res.data.code !== '200') {
      throw res.data;
    }
    return changeLetterLowCase(res.data.data);
  } catch (e) {
    handerErr(e);
  } finally {
    tips.hideLoading();
  }
}

function handerErr(error: ApiResponse<any>) {
  switch (error.code) {
    case '401':
      window.location.href = resolvePath(window['FEMAS_BASE_PATH'], 'femas/login?backurl=' + window.location.href);
  }
  notification.error({ description: error.message });
  throw error;
}

export async function authRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: 'auth',
  });
  return res;
}

/**
 * 获取所有的列表
 * @param fetchFun 模板函数需要支持pageNo,pageSize参数
 * @param listKey 返回结果中列表的键名称 默认list
 */
export function getAllList<TParam extends { pageNo?: number; pageSize?: number }, TResult>(
  fetchFun: (params?: TParam) => Promise<{ [list: string]: Array<TResult> } | { totalCount: number }>,
  listKey = 'list',
) {
  // eslint-disable-next-line prettier/prettier
  return async function (params: TParam) {
    let allList = [],
      pageNo = 1;
    const pageSize = 50;
    while (true) {
      // 每次获取获取50条
      params = { ...params, pageNo, pageSize };

      const result = await fetchFun({
        ...params,
        pageNo,
        pageSize,
      } as TParam);

      allList = allList.concat(result[listKey]);

      if (allList.length >= result.totalCount) {
        // 返回
        break;
      } else {
        pageNo++;
      }
    }
    return {
      list: allList,
      totalCount: allList.length,
    };
  };
}
