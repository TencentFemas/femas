import { connectWithDuck } from 'saga-duck';
import ConfigPage from './Page';
import ConfigPageDuck from './PageDuck';

export default connectWithDuck(ConfigPage, ConfigPageDuck);
