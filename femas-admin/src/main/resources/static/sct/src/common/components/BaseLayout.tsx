import * as React from 'react';
import { Layout, LoadingTip } from 'tea-component';

const { Body, Content } = Layout;

type BasicPageProps = {
  type?: 'page' | 'fregment';
  title?: React.ReactNode;
  header?: React.ReactNode;
  headerRender?: React.ReactNode;
  more?: React.ReactNode;
  operation?: React.ReactNode;
  hideTitle?: boolean;
  showBackButton?: boolean;
  full?: boolean;
  store: any;
  selectors: {
    ready: (store) => boolean;
    loading?: (store) => boolean;
  };
};

export default class BasicLayout extends React.Component<BasicPageProps, Readonly<{}>> {
  render() {
    const ready = this.props.selectors.ready(this.props.store);
    if (ready === null) return <noscript />;

    const { type = 'page', title, operation, header, more, hideTitle, headerRender, full = true } = this.props;
    let { children } = this.props;

    if (this.props.selectors.loading) {
      const loading = this.props.selectors.loading(this.props.store);
      if (loading) {
        children = (
          <LoadingTip
            style={{
              width: '100%',
              display: 'block',
              textAlign: 'center',
            }}
          />
        );
      }
    }

    return type === 'page' ? (
      <Layout>
        <Body>
          <Content>
            {!hideTitle &&
              (headerRender || (
                <Content.Header title={title} operation={operation}>
                  {header && header}
                </Content.Header>
              ))}
            <Content.Body full={full}>{children}</Content.Body>
          </Content>
          {more && more}
        </Body>
      </Layout>
    ) : (
      <>{children}</>
    );
  }
}
