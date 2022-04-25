import { connectWithDuck } from '../common/helpers/';
import LoginPage from './Login';
import LoginPageDuck from './LoginDuck';

export default connectWithDuck(LoginPage, LoginPageDuck);
