import { connectWithDuck } from "saga-duck";
import Deploy from "./Deploy";
import DeployDuck from "./DeployDuck";

export default connectWithDuck(Deploy, DeployDuck);
