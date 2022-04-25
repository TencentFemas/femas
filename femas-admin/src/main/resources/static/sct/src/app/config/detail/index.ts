import { connectWithDuck } from '@src/common/helpers/';
import DetailPage from './Page';
import DetailPageDuck from './PageDuck';

export default connectWithDuck(DetailPage, DetailPageDuck);
