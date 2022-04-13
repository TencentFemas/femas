import { connectWithDuck } from '@src/common/helpers/';
import AuthDetailPage from './Page';
import AuthDetailPageDuck from './PageDuck';

export default connectWithDuck(AuthDetailPage, AuthDetailPageDuck);
