import { purify, DuckCmpProps } from "saga-duck";
import CreateDuck, { DialogOptions } from "../create/CreateDuck";
import { LaneItem } from "../../types";
import { showDialog } from "@src/common/helpers/showDialog";
import { GroupInfo } from "./GoupInfo";

export function create(instance: LaneItem, options: DialogOptions) {
  return new Promise((resolve) => {
    showDialog(GroupInfo, CreateDuck, function*(duck: CreateDuck) {
      try {
        resolve(yield* duck.execute(instance, options));
      } finally {
        resolve(false);
      }
    });
  });
}
