import { connectWithDuck } from '@src/common/helpers/';
import RegistryDetailPage from './Page';
import RegistryDetailPageDuck from './PageDuck';

export default connectWithDuck(RegistryDetailPage, RegistryDetailPageDuck);
