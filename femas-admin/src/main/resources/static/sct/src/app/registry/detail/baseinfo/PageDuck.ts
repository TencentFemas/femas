import DetailPage from '@src/common/ducks/DetailPage';
import { ClusterInfo, describeRegistryCluster } from '../../model';
import { ComposedId } from '../PageDuck';
import { createToPayload, reduceFromPayload } from 'saga-duck';

export default class BaseInfoDuck extends DetailPage {
  Data: ClusterInfo;
  ComposedId: ComposedId;

  get baseUrl() {
    return null;
  }

  get initialFetch() {
    return false;
  }

  get watchTypes() {
    return [...super.watchTypes, this.types.LOAD];
  }

  get quickTypes() {
    enum Types {
      LOAD,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      composedId: reduceFromPayload(types.LOAD, {} as ComposedId),
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      load: createToPayload<ComposedId>(types.LOAD),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      composedId: (state: State) => state.composedId,
    };
  }

  *saga() {
    yield* super.saga();
  }

  async getData(param: this['ComposedId']) {
    const { registryId } = param;

    const res = await describeRegistryCluster({
      registryId,
    });
    return res;
  }
}
