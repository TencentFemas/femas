import { connectWithDuck } from "@src/common/helpers/";
import LaneDetailPage from "./Page";
import LaneDetailDuck from "./PageDuck";

export default connectWithDuck(LaneDetailPage, LaneDetailDuck);
