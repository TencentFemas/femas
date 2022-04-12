import { once, ttl } from '@src/common/helpers/cacheable';
import { fetchAllInterfaceList, fetchAllServiceList } from '../../model';
import { fetchAllNamespaceList } from '@src/app/namespace/model';

export const describeApiList = once(fetchAllInterfaceList, ttl(30 * 1000));
export const describeNamespaceList = once(fetchAllNamespaceList, ttl(30 * 1000));
export const describeServiceList = once(fetchAllServiceList, ttl(30 * 1000));
