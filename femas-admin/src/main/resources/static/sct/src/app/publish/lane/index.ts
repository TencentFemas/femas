import { connectWithDuck } from "saga-duck";
import LaneRulePage from "./Page";
import LaneRulePageDuck from "./PageDuck";

export default connectWithDuck(LaneRulePage, LaneRulePageDuck);
