import insertCSS from '@src/common/helpers/insertCSS';
import { circleColors, lineColors } from '@src/common/qcm-chart/theme/theme';
import * as React from 'react';
import { purify } from 'saga-duck';
import { Badge, Button, Card, ExternalLink, List, ListItem, Popover, Text } from 'tea-component';

insertCSS(
  'topology-info',
  `#topology .topo-info{
  box-shadow: 0 2px 3px 0 rgba(0,0,0,.2);
  padding: 5px 10px;
  display: inline-block;
}
.tsf-topo-info-item{
  display: inline-block;
  width: 15px;
  height: 4px;
  border-radius: 5px;
  margin-right: 2px;
}`,
);

export default purify(function Page() {
  const [visible, onVisibleChange] = React.useState(false);
  return (
    <section style={{ textAlign: 'right' }}>
      <Popover
        visible={visible}
        onVisibleChange={onVisibleChange}
        trigger='click'
        defaultVisible={true}
        placement='top'
        overlay={
          <Card style={{ padding: 10 }}>
            <List>
              <ListItem>
                <Badge ring theme='success' style={{ borderColor: circleColors[2] }} />
                <Text style={{ marginLeft: 10 }}>调用成功率</Text>
              </ListItem>
              <ListItem>
                <Badge ring theme='warning' style={{ borderColor: circleColors[1] }} />
                <Text style={{ marginLeft: 10 }}>调用错误率</Text>
              </ListItem>
              <ListItem>
                <Badge ring theme='warning' style={{ borderColor: circleColors[0] }} />
                <Text style={{ marginLeft: 10 }}>主动调用或无调用量</Text>
              </ListItem>
              <ListItem>
                <Text className='tsf-topo-info-item' style={{ background: lineColors[0] }} />
                <Text verticalAlign='middle' style={{ marginLeft: 4 }} tooltip={'时延小于200ms'}>
                  调用延时正常
                </Text>
              </ListItem>
              <ListItem>
                <Text className='tsf-topo-info-item' style={{ background: lineColors[1] }} />
                <Text verticalAlign='middle' style={{ marginLeft: 4 }} tooltip='时延大于200ms，小于800ms'>
                  调用延时较大
                </Text>
              </ListItem>
              <ListItem>
                <Text className='tsf-topo-info-item' style={{ background: lineColors[2] }} />
                <Text verticalAlign='middle' style={{ marginLeft: 4 }} tooltip='时延大于800ms'>
                  调用延时过大
                </Text>
              </ListItem>
            </List>
          </Card>
        }
      >
        <section className='topo-info' style={{ marginRight: 10 }}>
          <Button type='link' onClick={visible ? () => onVisibleChange(false) : null}>
            {visible ? '收起图例' : '展示图例'}
          </Button>
          <ExternalLink weak href='https://cloud.tencent.com/document/product/649/15544' style={{ marginLeft: 10 }}>
            了解更多
          </ExternalLink>
        </section>
      </Popover>
    </section>
  );
});
