import { connectWithDuck } from '@src/common/helpers/';
import RouteDetailPage from './Page';
import RouteDetailPageDuck from './PageDuck';

export default connectWithDuck(RouteDetailPage, RouteDetailPageDuck);
