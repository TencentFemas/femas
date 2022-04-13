import { connectWithDuck } from 'saga-duck';
import RegistryPage from './Page';
import RegistryPageDuck from './PageDuck';

export default connectWithDuck(RegistryPage, RegistryPageDuck);
