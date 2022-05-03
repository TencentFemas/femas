import DynamicDuck from '@src/common/ducks/DynamicDuck';
import SelectDuck from './SelectDuck';

export default class DynamicSelectDuck extends DynamicDuck {
  get ProtoDuck() {
    return SelectDuck;
  }
}
