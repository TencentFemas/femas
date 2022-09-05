import { showDialog } from "@src/common/helpers/showDialog";
import CreateDuck, { DialogOptions } from "./CreateDuck";
import Create from "./Create";
import { LaneRuleItem, RELEATION } from "../../types";

export default function create(instance: LaneRuleItem, options: DialogOptions) {
  return new Promise((resolve) => {
    showDialog(Create, CreateDuck, function*(duck: CreateDuck) {
      try {
        resolve(
          yield* duck.execute(
            {
              enable: 1,
              ruleTagRelationship: RELEATION.RELEATION_OR,
              ruleTagList: [{ tagName: "", tagOperator: null, tagValue: "" }],
              relativeLane: { "": 100 },
              ...instance,
            },
            options
          )
        );
      } finally {
        resolve(false);
      }
    });
  });
}
