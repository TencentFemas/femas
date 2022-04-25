// 导入样式
import 'tea-component/dist/tea.css';
import { createBrowserHistory } from 'history';
import '../node_modules/react-resizable/css/styles.css';
import '@src/assets/css/index.css';
import React from 'react';
import ReactDOM from 'react-dom';
import App from './app/App';
import Login from './login';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import resolvePath from './common/util/resolvePath';

export const history = createBrowserHistory();

export default function render() {
  ReactDOM.unmountComponentAtNode(document.querySelector('#sct_app'));

  ReactDOM.render(
    <Router basename={resolvePath(window['FEMAS_BASE_PATH'], `femas`)} history={history}>
      <Switch>
        <Route path='/login'>
          <Login />
        </Route>
        <Route path='*'>
          <App />
        </Route>
      </Switch>
    </Router>,
    document.querySelector('#sct_app'),
  );
}
render();
