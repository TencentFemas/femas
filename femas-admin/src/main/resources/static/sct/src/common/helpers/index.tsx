import * as React from 'react';
import { Provider } from 'react-redux';
import { createLogger } from 'redux-logger';
import { DuckCmpProps, DuckRuntime } from 'saga-duck';

type OMIT_DUCK_CMP<TProps> = Omit<TProps, 'duck' | 'store' | 'dispatch'>;

interface DuckClass<TDuck> {
  new (...any: any[]): TDuck;
}

export function connectWithDuck<TProps extends DuckCmpProps<TDuck>, TState, TDuck>(
  Component: React.ComponentClass<TProps, TState>,
  Duck: DuckClass<TDuck>,
  extraMiddlewares?: any[],
): React.StatelessComponent<OMIT_DUCK_CMP<TProps>>;
export function connectWithDuck<TProps, TDuck>(
  Component: React.StatelessComponent<TProps>,
  Duck: DuckClass<TDuck>,
  extraMiddlewares?: any[],
): React.StatelessComponent<OMIT_DUCK_CMP<TProps>>;

export function connectWithDuck(Component, Duck, extraMiddlewares = []) {
  return function ConnectedWithDuck(props) {
    try {
      const { duckRuntime, ConnectedComponent } = React.useMemo(() => {
        const middlewares = process.env.NODE_ENV === 'development' ? [createLogger({ collapsed: true })] : [];

        const duckRuntime = new DuckRuntime(new Duck(), ...middlewares, ...extraMiddlewares);

        const ConnectedComponent = duckRuntime.connectRoot()(Component);
        return {
          duckRuntime,
          ConnectedComponent,
        };
      }, []);

      return (
        <Provider store={duckRuntime.store}>
          <ConnectedComponent {...props} />
        </Provider>
      );
    } catch (e) {
      // 这里错误有可能会被吃掉，先打印出来
      throw e;
    }
  };
}
