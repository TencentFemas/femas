import { apiRequest, APIRequestOption } from '@src/common/util/apiRequest';

export async function metricRequest<T>(options: APIRequestOption) {
  const res = await apiRequest<T>({
    ...options,
    serviceType: 'metric',
  });
  return res;
}

export async function fetchMetricGrafanaAddress() {
  return metricRequest({
    action: 'fetchMetricGrafanaAddress',
    data: {},
  });
}
