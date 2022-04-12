import React from 'react';
import { DuckCmpProps, purify } from 'saga-duck';
import ServiceDetailDuck from './PageDuck';
import DetailPage from '@src/common/duckComponents/DetailPage';
import { Tab, TabPanel, Tabs } from 'tea-component';
import { TAB, TAB_LABLES } from './types';
import BaseInfo from './baseinfo/Page';
import Api from './api/Page';
import Event from './event/Page';
import Auth from './auth/Page';
import Limit from './limit/Page';
import Route from './route/Page';
import Fuse from './fuse/Page';

const tabs: Array<Tab> = [TAB.BaseInfo, TAB.Api, TAB.Auth, TAB.Limit, TAB.Route, TAB.Fuse, TAB.Event].map(id => ({
  id,
  label: TAB_LABLES[id],
}));

export default purify(function ServiceDetail(props: DuckCmpProps<ServiceDetailDuck>) {
  const { duck, store, dispatch } = props;
  const { selector, creators, ducks } = duck;
  const { tab, id } = selector(store);
  const handlers = React.useMemo(
    () => ({
      switch: (tab: Tab) => dispatch(creators.switch(tab.id)),
    }),
    [],
  );
  return (
    <DetailPage store={store} duck={duck} dispatch={dispatch} title={id} backRoute={'/service'}>
      <Tabs ceiling tabs={tabs} activeId={tab} onActive={handlers.switch}>
        <TabPanel id={TAB.BaseInfo}>
          <BaseInfo duck={ducks[TAB.BaseInfo]} store={store} dispatch={dispatch} />
        </TabPanel>
        <TabPanel id={TAB.Api}>
          <Api duck={ducks[TAB.Api]} store={store} dispatch={dispatch} />
        </TabPanel>
        <TabPanel id={TAB.Event}>
          <Event duck={ducks[TAB.Event]} store={store} dispatch={dispatch} />
        </TabPanel>
        <TabPanel id={TAB.Auth}>
          <Auth duck={ducks[TAB.Auth]} store={store} dispatch={dispatch} />
        </TabPanel>
        <TabPanel id={TAB.Limit}>
          <Limit duck={ducks[TAB.Limit]} store={store} dispatch={dispatch} />
        </TabPanel>
        <TabPanel id={TAB.Fuse}>
          <Fuse duck={ducks[TAB.Fuse]} store={store} dispatch={dispatch} />
        </TabPanel>
        <TabPanel id={TAB.Route}>
          <Route duck={ducks[TAB.Route]} store={store} dispatch={dispatch} />
        </TabPanel>
      </Tabs>
    </DetailPage>
  );
});
