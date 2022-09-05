import BasicLayout from "@src/common/components/BaseLayout";
import React from "react";
import { DuckCmpProps } from "saga-duck";
import PublishPageDuck from "./PageDuck";
import { Menu as MenuConfig } from "@src/config";
import { Tabs, TabPanel } from "tea-component";
import { TAB, TAB_LABLES } from "./types";
import Lanerule from "./lanerule";
import Lane from "./lane";
import { Tab } from "tea-component";

const tabs: Array<Tab> = Object.keys(TAB_LABLES).map((id) => ({
  id,
  label: TAB_LABLES[id],
}));

export default function PublishPage(props: DuckCmpProps<PublishPageDuck>) {
  const { duck, store, dispatch } = props;
  const { creators, selectors } = duck;
  const handlers = React.useMemo(
    () => ({
      onSwitch: (tab) => dispatch(creators.switch(tab.id)),
    }),
    []
  );
  const tab = selectors.tab(store);

  return (
    <BasicLayout
      title={MenuConfig.publish.title}
      store={store}
      selectors={duck.selectors}
    >
      <Tabs ceiling tabs={tabs} activeId={tab} onActive={handlers.onSwitch}>
        <TabPanel id={TAB.LANERULE}>
          <Lanerule />
        </TabPanel>

        <TabPanel id={TAB.LANE}>
          <Lane />
        </TabPanel>
      </Tabs>
    </BasicLayout>
  );
}
