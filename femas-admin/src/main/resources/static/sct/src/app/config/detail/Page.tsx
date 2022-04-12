import React from 'react';
import { DuckCmpProps } from 'saga-duck';
import RegistryDetailDuck from './PageDuck';
import DetailPage from '@src/common/duckComponents/DetailPage';
import { Tab, TabPanel, Tabs } from 'tea-component';
import { TAB, TAB_LABLES } from './types';
import BaseInfo from './baseinfo/Page';
import Version from './version/Page';

const tabs: Array<Tab> = [TAB.BaseInfo, TAB.Version].map(id => ({
  id,
  label: TAB_LABLES[id],
}));

export default function RegistryDetail(props: DuckCmpProps<RegistryDetailDuck>) {
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
    <DetailPage store={store} duck={duck} dispatch={dispatch} title={id} backRoute={'/config'}>
      <Tabs ceiling tabs={tabs} activeId={tab} onActive={handlers.switch}>
        <TabPanel id={TAB.BaseInfo}>
          <BaseInfo duck={ducks[TAB.BaseInfo]} store={store} dispatch={dispatch} />
        </TabPanel>
        <TabPanel id={TAB.Version}>
          <Version duck={ducks[TAB.Version]} store={store} dispatch={dispatch} />
        </TabPanel>
      </Tabs>
    </DetailPage>
  );
}
