import { apiRequest, APIRequestOption } from "@src/common/util/apiRequest";
import { LaneItem } from "./types";

export async function configRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: "lane",
  });
  return res;
}

interface Params {
  pageNo?: number;
  pageSize?: number;
  laneId?: string;
  laneName?: string;
  remark?: string;
}

export async function configureLane(params) {
  return configRequest({ action: "configureLane", data: params });
}

export async function fetchLaneById(params) {
  return configRequest<LaneItem>({ action: "fetchLaneById", data: params });
}

export async function fetchLaneInfoPages(params: Params) {
  const res = await configRequest<{
    count: number;
    data: Array<LaneItem>;
  }>({
    action: "fetchLaneInfoPages",
    data: params,
  });

  return {
    list: res?.data || [],
    totalCount: res?.count || 0,
  };
}

export async function deleteLane(params) {
  return configRequest({ action: "delete", data: params });
}
