import { showDialog } from "@src/common/helpers/showDialog";
import CreateDuck, { DialogOptions } from "./CreateDuck";
import Create from "./Create";
import { LaneItem } from "../../types";

export default function create(instance: LaneItem, options: DialogOptions) {
  return new Promise((resolve) => {
    showDialog(Create, CreateDuck, function*(duck: CreateDuck) {
      try {
        resolve(yield* duck.execute(instance, options));
      } finally {
        resolve(false);
      }
    });
  });
}
