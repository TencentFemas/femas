import {
  apiRequest,
  APIRequestOption,
  getAllList,
} from "@src/common/util/apiRequest";
import { fetchLaneInfoPages } from "../lane/model";
import { LaneRuleItem } from "./types";

export async function configRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: "lane/rule",
  });
  return res;
}

interface Params {
  pageNo?: number;
  pageSize?: number;
  remark?: string;
  ruleId?: string;
  ruleName?: string;
}

export async function configureLaneRule(params) {
  return configRequest({ action: "configureLaneRule", data: params });
}

export async function fetchLaneRuleById(params) {
  return configRequest<LaneRuleItem>({
    action: "fetchLaneRuleById",
    data: params,
  });
}

export async function fetchLaneRulePages(params: Params) {
  const res = await configRequest<{
    count: number;
    data: Array<LaneRuleItem>;
  }>({
    action: "fetchLaneRulePages",
    data: params,
  });

  return {
    list: res?.data || [],
    totalCount: res?.count || 0,
  };
}

export async function deleteLaneRule(params) {
  return configRequest({ action: "delete", data: params });
}

export async function fetchAllLaneList(params) {
  return getAllList(fetchLaneInfoPages)({
    ...{ laneId: "", laneName: "", remark: "" },
    ...params,
  });
}

export async function adjustLaneRulePriority(params) {
  return configRequest({ action: "adjustLaneRulePriority", data: params });
}
