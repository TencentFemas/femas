import DynamicDuck from '@src/common/ducks/DynamicDuck';
import VersionSelectDuck from './VersionSelectDuck';

export class DynamicVersionSelectDuck extends DynamicDuck {
  get ProtoDuck() {
    return VersionSelectDuck;
  }
}
