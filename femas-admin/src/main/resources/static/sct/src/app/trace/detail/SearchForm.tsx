import * as React from 'react';
import { Button, Form, FormItem, InputAdornment, Text } from 'tea-component';
import { DuckCmpProps, memorize } from 'saga-duck';
import Duck from './PageDuck';
import FormField from '@src/common/duckComponents/form/Field';
import Input from '@src/common/duckComponents/form/Input';
import Checkbox from '@src/common/duckComponents/form/Checkbox';
import AutoCompleteSelector from '@src/common/components/AutoComplete';
import { CallTypeList, CallTypeMap, getDateTimeSelectRange } from '@src/app/apm/types';
import TagConfig from '../search/TagConfig';
import formatDate from '@src/common/util/formatDate';
import TimeSelect from '@src/common/components/TimeSelect';
import Service from '@src/app/service/components/Service';

const getHandlers = memorize(({ creators }: Duck, dispatch) => ({
  execSearch: () => dispatch(creators.execSearch()),
}));

export default class CreateForm extends React.Component<DuckCmpProps<Duck>, {}> {
  timePicker: any;

  constructor(props) {
    super(props);
    this.preSubmit = this.preSubmit.bind(this);
  }

  get fields() {
    const { duck, store, dispatch } = this.props;
    const {
      ducks: { form },
    } = duck;
    return form
      .getAPI(store, dispatch)
      .getFields([
        'startTime',
        'endTime',
        'client',
        'server',
        'callStatus',
        'minDuration',
        'maxDuration',
        'clientApi',
        'serverApi',
        'callType',
        'statusCode',
        'showHigh',
        'clientInstanceIp',
        'serverInstanceIp',
        'traceId',
        'spanId',
      ]);
  }

  preSubmit() {
    this.timePicker && this.timePicker.flush();
    const handlers = getHandlers(this.props);
    handlers.execSearch();
  }

  render() {
    const { duck, store, dispatch } = this.props;
    const {
      ducks: { form },
    } = duck;
    const {
      startTime,
      endTime,
      client,
      server,
      callStatus,
      minDuration,
      maxDuration,
      clientApi,
      serverApi,
      callType,
      statusCode,
      showHigh,
      clientInstanceIp,
      serverInstanceIp,
      traceId,
      spanId,
    } = this.fields;

    const changeDate = time => {
      const { from, to } = time;
      startTime.setValue(formatDate(from));
      endTime.setValue(formatDate(to));
    };

    const toggleHigh = () => {
      showHigh.setValue(!showHigh.getValue());
    };

    const durationStatus =
      (minDuration.getTouched() && minDuration.getError()) || (maxDuration.getTouched() && maxDuration.getError());

    return (
      <Form>
        <FormItem label={'时间范围'} align='middle'>
          <TimeSelect
            from={startTime.getValue()}
            to={endTime.getValue()}
            ref={picker => (this.timePicker = picker)}
            changeDate={changeDate}
            range={getDateTimeSelectRange()}
          />
        </FormItem>
        <FormField field={traceId} label={'Trace ID'}>
          <Input field={traceId} placeholder={'请输入traceId'} />
        </FormField>
        <FormField field={spanId} label={'Span ID'}>
          <Input field={spanId} placeholder={'请输入spanId'} />
        </FormField>
        <FormField field={client} label={'客户端'} align='middle'>
          <Service
            duck={form.ducks.clientService}
            dispatch={dispatch}
            store={store}
            onChange={v => client.setValue(v)}
          />
        </FormField>
        <FormField field={server} label={'服务端'} align='middle'>
          <Service
            duck={form.ducks.serverService}
            dispatch={dispatch}
            store={store}
            onChange={v => server.setValue(v)}
          />
        </FormField>
        <FormField field={callStatus} label={'失败调用'}>
          <Checkbox field={callStatus} label='查看' />
        </FormField>
        <FormItem label={'耗时范围'} align='middle'>
          <InputAdornment after='ms'>
            <Input
              field={minDuration}
              size='s'
              style={{
                borderColor: minDuration.getTouched() && minDuration.getError() ? 'red' : '#ddd',
                zIndex: 1,
              }}
            />
          </InputAdornment>
          <Text reset verticalAlign='middle' style={{ margin: '0 15px' }}>
            至
          </Text>
          <InputAdornment after='ms'>
            <Input
              field={maxDuration}
              size='s'
              style={{
                borderColor: maxDuration.getTouched() && maxDuration.getError() ? 'red' : '#ddd',
                zIndex: 1,
              }}
            />
          </InputAdornment>
          {durationStatus && (
            <Text parent='div' className='tea-form__help-text' theme='danger'>
              {durationStatus}
            </Text>
          )}
        </FormItem>
        {showHigh.getValue() && (
          <>
            <FormField field={clientApi} label={'客户端接口'}>
              <Input field={clientApi} placeholder={'请输入客户端接口名'} />
            </FormField>
            <FormField field={serverApi} label={'服务端接口'}>
              <Input field={serverApi} placeholder={'请输入服务端接口名'} />
            </FormField>
            <FormField field={clientInstanceIp} label={'客户端IP'}>
              <Input field={clientInstanceIp} placeholder={'请输入客户端IP'} />
            </FormField>
            <FormField field={serverInstanceIp} label={'服务端IP'}>
              <Input field={serverInstanceIp} placeholder={'请输入服务端IP'} />
            </FormField>
            <FormField field={callType} label={'调用类型'} align='middle'>
              <AutoCompleteSelector
                value={callType.getValue() && callType.getValue()}
                onItemSelect={item => {
                  statusCode.setValue('');
                  callType.setValue(item?.value || '');
                }}
                dataSource={CallTypeList}
                showBubble
              />
            </FormField>
            {(callType.getValue() === CallTypeMap.http || callType.getValue() === CallTypeMap.rpc) && (
              <FormField field={statusCode} label={'状态码'}>
                <Input field={statusCode} placeholder={'请输入状态码，如403'} />
              </FormField>
            )}
          </>
        )}
        <TagConfig
          duck={form}
          dispatch={dispatch}
          store={store}
          message='Tag中包含系统和业务自定义的标签，通过标识span以实现标识查询、过滤与聚合，支持精确匹配'
          title='业务标签（Tag）'
        />
        <FormItem
          label={
            <Button type='link' onClick={toggleHigh}>
              {showHigh.getValue() ? '隐藏高级筛选' : '展开高级筛选'}
            </Button>
          }
        />
        <FormItem
          label={
            <Button type='primary' title={'查询'} onClick={this.preSubmit}>
              查询
            </Button>
          }
        />
      </Form>
    );
  }
}
