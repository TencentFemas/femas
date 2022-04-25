import { connectWithDuck } from '@src/common/helpers';
import OperationPage from './Page';
import OperationDuck from './PageDuck';

export default connectWithDuck(OperationPage, OperationDuck);
