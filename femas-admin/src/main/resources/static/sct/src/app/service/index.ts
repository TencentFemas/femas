import { connectWithDuck } from 'saga-duck';
import ServicePage from './Page';
import ServicePageDuck from './PageDuck';

export default connectWithDuck(ServicePage, ServicePageDuck);
