import * as React from 'react';
import Duck from './PageDuck';
import CreateForm from './Form';
import { TAB } from '../../types';
import DetailPage from '@src/common/duckComponents/DetailPage';
import { notification } from 'tea-component';

export default class ServiceRouteCreatePage extends React.Component<any, any> {
  form;

  constructor(props) {
    super(props);
    this._onSubmit = this._onSubmit.bind(this);
  }

  componentDidMount() {
    const { duck, store, dispatch } = this.props;
    const { selectors, ducks } = duck as Duck;
    const { namespaceId, serviceName } = selectors.composedId(store);
    dispatch(
      ducks.form.ducks.targetGrid.creators.load({
        namespaceId,
        serviceName,
      }),
    );
  }

  _onSubmit() {
    const form = this.form;
    if (!form.isValid()) {
      notification.error({ description: '请完善信息' });
      return;
    }
    const {
      duck: { creators },
      dispatch,
    } = this.props;
    // 提交
    dispatch(creators.submit());
  }

  render() {
    const { duck, store, dispatch } = this.props;
    const { selectors, ducks } = duck as Duck;
    const ruleId = selectors.id(store);
    const { namespaceId, serviceName, registryId } = selectors.composedId(store);
    const loading = selectors.loading(store);
    const title = ruleId ? '编辑路由规则' : '创建路由规则';
    const backUrl = `/service/service-detail?id=${serviceName}&namespaceId=${namespaceId}&registryId=${registryId}&tab=${TAB.Route}`;
    return (
      <DetailPage
        backHistory
        backRoute={backUrl}
        duck={duck}
        store={store}
        dispatch={dispatch}
        title={title}
        showCard
        subTitle={serviceName}
      >
        {!loading && (
          <CreateForm
            ref={node => (this.form = node)}
            duck={ducks.form}
            dispatch={dispatch}
            store={store}
            options={{
              registryId,
              namespaceId,
              serviceName,
            }}
            onSubmit={this._onSubmit}
          />
        )}
      </DetailPage>
    );
  }
}
