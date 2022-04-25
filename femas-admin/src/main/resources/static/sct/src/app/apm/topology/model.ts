import { traceRequest } from '@src/app/trace/model';
import { changeLetterLowCase } from '@src/common/util/common';
import moment from 'moment';

export async function getTopologyGraph(params) {
  const { startTime, endTime, namespaceId } = params;
  if (!startTime || !endTime || !namespaceId) {
    return [];
  }
  const result = await traceRequest({
    action: 'describeServiceTopology',
    data: {
      namespaceId,
      queryDuration: {
        start: moment(startTime).valueOf(),
        end: moment(endTime).valueOf(),
      },
    },
  });
  return changeLetterLowCase(result);
}
