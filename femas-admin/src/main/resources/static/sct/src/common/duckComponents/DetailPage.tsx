import * as React from 'react';
import { DuckCmpProps, purify } from 'saga-duck';
import Duck from '../ducks/DetailPage';
import { Blank, Card, Layout, Text } from 'tea-component';
import BaseLayout from '../components/BaseLayout';
import { useHistory } from 'react-router-dom';

const TextTip = purify(function TextTip({ children }) {
  return <div style={{ textAlign: 'center', padding: '100px' }}>{children}</div>;
});

function historyBack() {
  history.go(-1);
}

interface Props {
  /** 面包屑 */
  crumb?: string;
  className?: string;
  /** 点击返回时的回调 */
  onBack?: () => any;
  /** 是否优先尝试返回历史记录（如果有的话），仅当onBack未设置时才有效 */
  backHistory?: boolean;
  /** 返回路由，仅当onBack未设置时才有效 */
  backRoute?: string;
  /** 返回文案，现有样式实际上并不会展示 @deprecated */
  backTitle?: string;
  /** 标题 */
  title?: string;
  /** 子标题 */
  subTitle?: string;
  /** 额外标题 */
  titleRight?: string | JSX.Element | JSX.Element[];
  children?: string | JSX.Element | JSX.Element[];
  popComponent?: string | JSX.Element | JSX.Element[];
  headerComponent?: string | JSX.Element | JSX.Element[];
  style?: React.CSSProperties;
  showCard?: boolean;
}

// TODO 纯render组件是否会不利于性能优化？
// 因为这里是传递完整的store进来，数据一定会变动
// 或可考虑做一层包装，即取出要用的数据后，继续下一级
export default purify(function DetailPage({
  duck: { selectors },
  store,
  /** 是否优先尝试返回历史记录（如果有的话） */
  backHistory,
  backRoute,
  title,
  subTitle,
  onBack,
  titleRight,
  children,
  popComponent = <noscript />,
  headerComponent = <noscript />,
  showCard = false,
}: DuckCmpProps<Duck> & Props) {
  const data = selectors.data(store);
  const loading = selectors.loading(store);
  const error = selectors.error(store) as any;
  const history = useHistory();
  if (!onBack) {
    // 如果历史记录大于2（chrome打开时，新标签页也是1次，当前是第2次）
    if (backHistory && history.length > 2) {
      backRoute = '';
      onBack = historyBack;
    } else {
      onBack = () => {
        history.push(backRoute);
      };
    }
  }

  let overrideChildren;
  if (loading) {
    // loading最优先
    overrideChildren = <TextTip>{'加载中...'}</TextTip>;
  } else if (error) {
    // 其次错误
    <TextTip>{error.message}</TextTip>;
  } else if (!data) {
    // 其次没有找到
    overrideChildren = <Blank theme='error' description={'未找到指定资源，请检查资源是否存在'} />;
  }

  return (
    <BaseLayout
      selectors={selectors}
      store={store}
      headerRender={
        <Layout.Content.Header
          showBackButton
          onBackButtonClick={onBack}
          title={
            <>
              {title}
              {subTitle && <Text theme='weak'>（{subTitle}）</Text>}
            </>
          }
          operation={titleRight}
        >
          {headerComponent}
        </Layout.Content.Header>
      }
    >
      {showCard ? (
        <Card>
          <Card.Body>{overrideChildren || children}</Card.Body>
        </Card>
      ) : (
        overrideChildren || children
      )}
      {popComponent}
    </BaseLayout>
  );
});
