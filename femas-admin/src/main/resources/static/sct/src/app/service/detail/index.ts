import { connectWithDuck } from '@src/common/helpers/';
import ServiceDetailPage from './Page';
import ServiceDetailPageDuck from './PageDuck';

export default connectWithDuck(ServiceDetailPage, ServiceDetailPageDuck);
