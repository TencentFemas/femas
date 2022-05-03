import DynamicDuck from '@src/common/ducks/DynamicDuck';
import DetailGridDuck from './detail/DetailGridDuck';

export default class DynamicSubGridDuck extends DynamicDuck {
  get ProtoDuck() {
    return DetailGridDuck;
  }
}
