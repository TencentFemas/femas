import { showDialog } from '@src/common/helpers/showDialog';
import Create from './Create';
import CreateDuck, { DialogOptions, Values } from './CreateDuck';

export default function create(instance: Values, options: DialogOptions) {
  return new Promise(resolve => {
    showDialog(Create, CreateDuck, function*(duck: CreateDuck) {
      try {
        resolve(yield* duck.execute(instance, options));
      } finally {
        resolve(false);
      }
    });
  });
}
