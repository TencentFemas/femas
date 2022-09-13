import { ServiceItem } from "@src/app/service/types";
import {
  apiRequest,
  APIRequestOption,
  getAllList,
} from "@src/common/util/apiRequest";

export async function serviceRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: "service",
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
    action: "describeRegisterService",
    data: params,
  });

  return {
    list:
      res?.serviceBriefInfos?.map((item) => ({
        ...item,
        id: item.serviceName,
        namespaceId: params.namespaceId,
        registryId: res.registryId,
      })) || [],
    totalCount: res?.count || 0,
  };
}

export async function fetchAllServiceList(params) {
  return getAllList(describeRegisterService)(params);
}

export async function describeServiceInstanceByNsId(params) {
  return serviceRequest<{ id: string }>({
    action: "describeServiceInstanceByNsId",
    data: params,
  });
}
