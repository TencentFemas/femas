import React from 'react';
import { DuckCmpProps, memorize, purify } from 'saga-duck';
import Duck, { CreateFormDuck } from './CreateDuck';
import Dialog from '../../../../common/duckComponents/Dialog';
import { Bubble, Form, Segment, Text, Icon, InputAdornment, FormText } from 'tea-component';
import FormField from '@src/common/duckComponents/form/Field';
import Input from '@src/common/duckComponents/form/Input';
import { ClusterTypeMap, K8S_NATIVE_TYPE, K8S_NATIVE_TYPE_NAME, K8S_HTTP_PROTOCOL, ClusterType } from '../../types';
import { nameTipMessage } from '@src/common/types';
import CodeMirrorBox from '@src/common/components/CodeMirrorBox';
import Select from '@src/common/duckComponents/form/Select';

export default function Create(props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const { selectors } = duck;
  const visible = selectors.visible(store);
  if (!visible) {
    return <noscript />;
  }
  return (
    <Dialog duck={duck} store={store} dispatch={dispatch} size={800} title={'配置注册中心'}>
      <CreateForm duck={duck} store={store} dispatch={dispatch} />
    </Dialog>
  );
}

const getHandlers = memorize(({ creators }: CreateFormDuck, dispatch) => ({
  retestK8sConnection: () => dispatch(creators.retestK8sConnection()),
}));

const CreateForm = purify(function CreateForm(props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const {
    ducks: { form },
  } = duck;

  const formApi = form.getAPI(store, dispatch);
  const {
    registryName,
    registryType,
    registryCluster,
    certificateType,
    kubeConfig,
    secret,
    apiServer,
    k8sApiProtocol,
  } = formApi.getFields([
    'registryName',
    'registryType',
    'registryCluster',
    'certificateType',
    'kubeConfig',
    'secret',
    'apiServer',
    'k8sApiProtocol',
  ]);
  const { addMode } = duck.selectors.options(store);
  const k8configCheckResult = form.selectors.k8configCheckResult(store);
  const servcieAcCheckResult = form.selectors.servcieAcCheckResult(store);
  const handlers = getHandlers({ ...props, duck: form });

  return (
    <>
      <Form>
        {addMode && (
          <FormField field={registryType} label='注册中心类型'>
            <Segment
              options={Object.keys(ClusterTypeMap).map(item => ({
                value: item,
                text: ClusterTypeMap[item],
              }))}
              value={registryType.getValue()}
              onChange={value => registryType.setValue(value)}
            />
          </FormField>
        )}

        <FormField field={registryName} label={'注册中心名称'} required message={nameTipMessage}>
          <Input field={registryName} maxLength={60} placeholder={'请输入注册中心名称'} />
        </FormField>

        {addMode && registryType.getValue() === ClusterType.K8s && (
          <>
            <FormField field={certificateType} label={'认证方式'}>
              {/* <Segment
                options={Object.values(K8S_NATIVE_TYPE).map(value => ({
                  value,
                  text: K8S_NATIVE_TYPE_NAME[value],
                }))}
                value={certificateType.getValue()}
                onChange={value => certificateType.setValue(value)}
              /> */}
              <FormText>{K8S_NATIVE_TYPE_NAME[K8S_NATIVE_TYPE.kubeconfig]}</FormText>
            </FormField>
            {certificateType.getValue() === K8S_NATIVE_TYPE.kubeconfig && (
              <FormField
                showStatusIcon={false}
                required
                field={kubeConfig}
                label={'kubeConfig'}
                message={'YAML格式'}
                status={
                  (kubeConfig.getTouched() && kubeConfig.getError()) ||
                  (kubeConfig.getValue() && k8configCheckResult && !k8configCheckResult.success)
                    ? 'error'
                    : null
                }
              >
                <CodeMirrorBox
                  style={{
                    borderColor:
                      (kubeConfig.getTouched() && kubeConfig.getError()) ||
                      (kubeConfig.getValue() && k8configCheckResult && !k8configCheckResult.success)
                        ? '#e1504a'
                        : '#ddd',
                    display: 'inline-block',
                  }}
                  height={300}
                  width={600}
                  value={kubeConfig.getValue()}
                  onChange={v => kubeConfig.setValue(v)}
                  onBlur={() => handlers.retestK8sConnection()}
                />
                {k8configCheckResult && (
                  <Bubble content={k8configCheckResult.message || ''}>
                    <Text
                      style={{ marginLeft: 10, position: 'absolute', marginTop: 100 }}
                      reset
                      theme={k8configCheckResult.success ? 'success' : 'danger'}
                    >
                      检测{k8configCheckResult.success ? '通过' : '失败'}
                      <Icon type={k8configCheckResult.success ? 'success' : 'error'} />
                    </Text>
                  </Bubble>
                )}
              </FormField>
            )}
            {certificateType.getValue() === K8S_NATIVE_TYPE.serviceAccount && (
              <>
                <FormField required field={apiServer} label={'API Server地址'}>
                  <InputAdornment
                    before={
                      <Select
                        field={k8sApiProtocol}
                        options={Object.values(K8S_HTTP_PROTOCOL).map(value => ({
                          value,
                        }))}
                        style={{ width: 'auto' }}
                      />
                    }
                  >
                    <Input
                      onBlur={() => handlers.retestK8sConnection()}
                      field={apiServer}
                      placeholder={'请输入API Server地址'}
                    />
                  </InputAdornment>
                </FormField>
                <FormField
                  status={
                    (secret.getTouched() && secret.getError()) ||
                    (secret.getValue() && servcieAcCheckResult && !servcieAcCheckResult.success)
                      ? 'error'
                      : null
                  }
                  showStatusIcon={false}
                  required
                  label={'Secret'}
                  field={secret}
                >
                  <Input
                    style={{ width: 550 }}
                    multiline
                    field={secret}
                    placeholder={'请输入Secret'}
                    onBlur={() => handlers.retestK8sConnection()}
                  />
                  {servcieAcCheckResult && (
                    <Bubble content={servcieAcCheckResult.message || ''}>
                      <Text
                        style={{ marginLeft: 10 }}
                        reset
                        theme={servcieAcCheckResult.success ? 'success' : 'danger'}
                      >
                        检测{servcieAcCheckResult.success ? '通过' : '失败'}
                        <Icon type={servcieAcCheckResult.success ? 'success' : 'error'} />
                      </Text>
                    </Bubble>
                  )}
                </FormField>
              </>
            )}
          </>
        )}

        {addMode && registryType.getValue() !== ClusterType.K8s && (
          <FormField field={registryCluster} label={'集群地址'} required>
            <Input field={registryCluster} multiline />
          </FormField>
        )}
      </Form>
    </>
  );
});
