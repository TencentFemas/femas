import { connectWithDuck } from '@src/common/helpers';
import NamespacePage from './Page';
import NamespacePageDuck from './PageDuck';

export default connectWithDuck(NamespacePage, NamespacePageDuck);
