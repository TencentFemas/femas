import { connectWithDuck } from '@src/common/helpers/';
import FuseCreatePage from './Page';
import FuseCreatePageDuck from './PageDuck';

export default connectWithDuck(FuseCreatePage, FuseCreatePageDuck);
