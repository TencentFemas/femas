import { connectWithDuck } from "saga-duck";
import PublishPage from "./Page";
import PublishPageDuck from "./PageDuck";

export default connectWithDuck(PublishPage, PublishPageDuck);
