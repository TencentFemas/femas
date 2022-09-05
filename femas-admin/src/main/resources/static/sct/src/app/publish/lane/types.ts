import { ServiceInfo } from "./operations/goupInfo/deploy/type";

export interface LaneItem {
  laneId: string;
  laneName: string;
  remark: string;
  createTime: number;
  updateTime: number;
  laneServiceList: Array<ServiceInfo>;
}
