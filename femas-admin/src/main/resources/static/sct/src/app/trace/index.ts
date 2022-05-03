import { connectWithDuck } from 'saga-duck';
import Page from './Page';
import PageDuck from './PageDuck';

export default connectWithDuck(Page, PageDuck);
