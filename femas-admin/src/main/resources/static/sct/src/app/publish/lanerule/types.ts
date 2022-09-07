// 包含 "IN" 不包含 "NOT_IN" 等于 "EQUAL" 不等于 "NOT_EQUAL" 正则 "REGEX"
export enum TAGOPERATORENUM {
  IN = "IN",
  NOT_IN = "NOT_IN",
  EQUAL = "EQUAL",
  NOT_EQUAL = "NOT_EQUAL",
  REGEX = "REGEX ",
}
export const TAGOPERATORENUM_LABEL = {
  [TAGOPERATORENUM.IN]: "包含",
  [TAGOPERATORENUM.NOT_IN]: "不包含",
  [TAGOPERATORENUM.EQUAL]: "等于",
  [TAGOPERATORENUM.NOT_EQUAL]: "不等于",
  [TAGOPERATORENUM.REGEX]: "正则",
};

export interface LaneRuleTag {
  tagName: string;
  tagOperator: TAGOPERATORENUM;
  tagValue: string;
}

// 是否开启 1：开启 0：关闭
export enum ENABLE {
  OPEN = 1,
  CLOSE = 0,
}
export const ENABLE_LABEL = {
  [ENABLE.OPEN]: "开启",
  [ENABLE.CLOSE]: "关闭",
};

// 灰度类型 蓝绿：tag 金丝雀：canary
export enum GRAYTYPE {
  TAG = "TAG",
  CANARY = "CANARY",
}
export const GRAYTYPE_LABEL = {
  [GRAYTYPE.TAG]: "蓝绿",
  [GRAYTYPE.CANARY]: "金丝雀",
};

export enum RELEATION {
  RELEATION_AND = "RELEATION_AND",
  RELEATION_OR = "RELEATION_OR",
}
export const RELEATION_LABEL = {
  [RELEATION.RELEATION_OR]: "或（满足任一规则）",
  [RELEATION.RELEATION_AND]: "与（同时满足全部规则）",
};

export interface LaneRuleItem {
  ruleId: string;
  ruleName: string;
  remark: string;
  enable: ENABLE;
  grayType: GRAYTYPE;
  priority: number;
  createTime: number;
  updateTime: number;
  relativeLane: { [key: string]: string };
  ruleTagList: Array<LaneRuleTag>;
  ruleTagRelationship: RELEATION;
}
