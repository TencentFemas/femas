import React from 'react';
import { Layout, Menu, NavMenu } from 'tea-component';
import { Redirect, Route, Switch, useHistory } from 'react-router-dom';
import { defaultMenu, Menu as MenuConfig } from '../config';
import Guide from './guide/Page';
import Namespace from './namespace';
import Registry from './registry';
import RegistryDetail from './registry/detail';
import Service from './service';
import ServiceDetail from './service/detail';
import ServiceAuthCreate from './service/detail/auth/create';
import ServiceLimitCreate from './service/detail/limit/create';
import ServiceRouteCreate from './service/detail/route/create';
import ServiceFuseCreate from './service/detail/fuse/create';
import Operation from './operation';
import Config from './config';
import Topology from './topology';
import Trace from './trace';
import ConfigDetail from './config/detail';

const baseUrl = window['FEMAS_BASE_PATH'];

const { Header, Body, Sider, Content } = Layout;

export default function Sct() {
  const history = useHistory();
  const [selected, setSelected] = React.useState(history.location.pathname.match(/^\/(\w+)/)?.[1] || defaultMenu);
  const getMenuItemProps = id => ({
    selected: selected === id,
    onClick: () => {
      setSelected(id);
      history.push(`/${id}`);
    },
  });
  return (
    <Layout>
      <Header>
        <NavMenu
          left={
            <>
              <NavMenu.Item type='logo'>
                <img src={`${baseUrl}/icon/femas.svg`} alt='logo' />
              </NavMenu.Item>
              <NavMenu.Item style={{ fontSize: 18 }}>Femas</NavMenu.Item>
            </>
          }
        />
      </Header>
      <Body>
        <Sider>
          <Menu collapsable theme='dark'>
            {Object.keys(MenuConfig).map(item =>
              MenuConfig[item].isSubMenu ? (
                <Menu.SubMenu title={MenuConfig[item].title} icon={MenuConfig[item].icon}>
                  {MenuConfig[item].submenus.map(sub => (
                    <Menu.Item key={sub.route} {...sub} {...getMenuItemProps(sub.route)} />
                  ))}
                </Menu.SubMenu>
              ) : (
                <Menu.Item {...MenuConfig[item]} {...getMenuItemProps(item)} />
              ),
            )}
          </Menu>
        </Sider>
        <Content>
          <Switch>
            <Route exact path='/guide' component={Guide} />
            <Route exact path='/registry' component={Registry} />
            <Route exact path='/registry/detail' component={RegistryDetail} />
            <Route exact path='/namespace' component={Namespace} />
            <Route exact path='/service' component={Service} />
            <Route exact path='/service/detail' component={ServiceDetail} />
            <Route exact path='/service/service-auth-create' component={ServiceAuthCreate} />
            <Route exact path='/service/service-limit-create' component={ServiceLimitCreate} />
            <Route exact path='/service/service-route-create' component={ServiceRouteCreate} />
            <Route exact path='/service/fuse-create' component={ServiceFuseCreate} />
            <Route exact path='/operation-log' component={Operation} />
            <Route exact path='/config' component={Config} />
            <Route exact path='/config/detail' component={ConfigDetail} />
            <Route exact path='/topology' component={Topology} />
            <Route exact path='/trace' component={Trace} />
            <Route path='*'>
              <Redirect to='/guide' />
            </Route>
          </Switch>
        </Content>
      </Body>
    </Layout>
  );
}
