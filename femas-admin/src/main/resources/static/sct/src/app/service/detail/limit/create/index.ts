import { connectWithDuck } from '@src/common/helpers/';
import LimitDetailPage from './Page';
import LimitDetailPageDuck from './PageDuck';

export default connectWithDuck(LimitDetailPage, LimitDetailPageDuck);
