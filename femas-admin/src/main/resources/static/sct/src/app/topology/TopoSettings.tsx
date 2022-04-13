import * as React from 'react';
import PageDuck from './PageDuck';
import { DuckCmpProps, purify } from 'saga-duck';
import { Text } from 'tea-component';
import insertCSS from '@src/common/helpers/insertCSS';
import { circleColors } from '@src/common/qcm-chart/theme/theme';

insertCSS(
  'topology-info',
  `#topology .topo-info{
  box-shadow: 0 2px 3px 0 rgba(0,0,0,.2);
  padding: 5px 10px;
  display: inline-block;
}
#topology .topo-tip-container{
  display: inline-block;
  vertical-align: middle;
  padding: 4px 0;
}
#topology .topo-tip{
  width: 15px;
  height: 5px;
  display: inline-block;
  vertical-align: middle;
  margin-right: 10px;
}`,
);

export default purify(function Page(props: DuckCmpProps<PageDuck>) {
  const { duck } = props;
  // const {
  //   ducks: { topology },
  // } = duck;
  // const nodeCalMode = topology.selectors.nodeCalMode(store);
  // const traceShowMode = topology.selectors.traceShowMode(store);
  // const [, onVisibleChange] = React.useState(false);
  // const [nodeMode, changeNodeMode] = React.useState('');
  // const [traceMode, changeTraceMode] = React.useState('');

  return (
    <section style={{ textAlign: 'right' }} key={duck.baseUrl}>
      <section className='topo-info' style={{ marginRight: 10 }}>
        <Text className='topo-tip-container' style={{ marginRight: 10 }}>
          <span className='topo-tip' style={{ backgroundColor: circleColors[2] }} />
          <Text reset verticalAlign='middle'>
            请求成功率
          </Text>
        </Text>
        <Text className='topo-tip-container' style={{ marginRight: 10 }}>
          <span className='topo-tip' style={{ backgroundColor: circleColors[1] }} />
          <Text reset verticalAlign='middle'>
            请求错误率
          </Text>
        </Text>
        <Text className='topo-tip-container'>
          <span className='topo-tip' style={{ backgroundColor: circleColors[0] }} />
          <Text reset verticalAlign='middle'>
            无调用量服务
          </Text>
        </Text>
      </section>
      {/* <section className="topo-info">
        <Text reset verticalAlign="middle">
          {NodeCalModeName[nodeCalMode]} | 调用线上
          {TraceShowModeName[traceShowMode]}
        </Text>
        <Popover
          placement="top"
          trigger="click"
          visible={visible}
          onVisibleChange={toggleVisible}
          overlay={
            <Card style={{ width: 350 }}>
              <Card.Body
                title="设置"
                operation={
                  <Icon type="close" onClick={() => toggleVisible(false)} />
                }
              >
                <Form>
                  <FormItem label="节点权重">
                    <RadioGroup
                      layout="column"
                      value={nodeMode}
                      onChange={changeNodeMode}
                    >
                      {Object.values(NodeCalMode).map((value) => (
                        <Radio name={value} key={value}>
                          {NodeCalModeName[value]}
                        </Radio>
                      ))}
                    </RadioGroup>
                  </FormItem>
                  <FormItem label="调用关系线">
                    <RadioGroup
                      layout="column"
                      value={traceMode}
                      onChange={changeTraceMode}
                    >
                      {Object.values(TraceShowMode).map((value) => (
                        <Radio name={value} key={value}>
                          {TraceShowModeName[value]}
                        </Radio>
                      ))}
                    </RadioGroup>
                  </FormItem>
                </Form>
                <FormAction
                  style={{ textAlign: "right", border: "none", marginTop: 0 }}
                >
                  <Button type="link" onClick={submit}>
                    确定
                  </Button>
                  <Button type="link" onClick={() => toggleVisible(false)}>
                    取消
                  </Button>
                </FormAction>
              </Card.Body>
            </Card>
          }
        >
          <Button icon="setting" tooltip="设置" />
        </Popover>
      </section> */}
    </section>
  );
});
