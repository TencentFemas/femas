import React from "react";
import { Button, Justify, Table, TagSearchBox } from "tea-component";
import { DuckCmpProps } from "saga-duck";
import { SearchBox } from "tea-component";
import { Card } from "tea-component";
import GridPageGrid from "@src/common/duckComponents/GridPageGrid";
import getColumns from "./getColumns";
import GridPagePagination from "@src/common/duckComponents/GridPagePagination";
import LaneRulePageDuck from "./PageDuck";

export default function LaneRulePage(props: DuckCmpProps<LaneRulePageDuck>) {
  const { duck, store, dispatch } = props;
  const { creators, selectors } = duck;
  const handlers = React.useMemo(
    () => ({
      reload: () => dispatch(creators.reload()),
      search: (keyword) => dispatch(creators.search(keyword)),
      inputKeyword: (keyword) => dispatch(creators.inputKeyword(keyword)),
      clearKeyword: () => dispatch(creators.search("")),
      delete: (v) => dispatch(creators.delete(v)),
      add: () => dispatch(creators.add()),
    }),
    []
  );
  const columns = React.useMemo(() => getColumns(props), []);
  console.log(
    selectors.pendingKeyword(store),
    "selectors.pendingKeyword(store)"
  );
  return (
    <>
      <Table.ActionPanel>
        <Justify
          left={
            <>
              <Button type="primary" onClick={() => handlers.add()}>
                新建泳道
              </Button>
            </>
          }
          right={
            <>
              <SearchBox
                value={selectors.pendingKeyword(store)}
                placeholder="请输入名称"
                onSearch={handlers.search}
                onChange={handlers.inputKeyword}
                onClear={handlers.clearKeyword}
              />
              <Button type="icon" icon="refresh" onClick={handlers.reload}>
                刷新
              </Button>
            </>
          }
        />
      </Table.ActionPanel>
      <Card>
        <GridPageGrid
          duck={duck}
          dispatch={dispatch}
          store={store}
          columns={columns}
        />
        <GridPagePagination duck={duck} dispatch={dispatch} store={store} />
      </Card>
    </>
  );
}
