import React from 'react';

import { Loading } from './Loading';
import Role from '../dsl/security/Role';

interface IUser {
  username: string;
  roles: Set<Role>;
}

interface IAuthContext {
  user?: IUser;
  onForbidden: () => void;
}

const onForbiddenDefault = () => {
  throw new Error('You are not allowed to access this page!');
}

export const AuthContext = React.createContext<IAuthContext>({ onForbidden: onForbiddenDefault });

interface IForFakeUser {}

export const ForFakeUser: React.FC<IForFakeUser> = ({ children }) => {
  const [ error, setError ] = React.useState<string>();
  const [ user, setUser] = React.useState<IUser>();

  React.useEffect(() => {
    const timeoutID = window.setTimeout(() => {
      setUser({
        username: 'demo_user',
        roles: new Set<Role>([
          Role.PACKAGE_CHANGE_STATUS,
          Role.PACKAGE_CREATE,
          Role.PACKAGE_MANAGE,
          Role.PACKAGE_VIEW,
        ]),
      });
    }, 3_000);

    return () => window.clearTimeout(timeoutID);
  }, []);

  const onForbidden = React.useCallback(
    () => setError(`Access to this page is forbidden for ${user?.username ?? 'guest'}`),
    [user],
  );

  const context = React.useMemo(() => ({ user, onForbidden }), [user, onForbidden]);

  if (user == null) {
    return (
      <Loading />
    )
  }

  return (
    <AuthContext.Provider value={context}>
      {
        error != null ? (
          <div className='error'>
            {error}
          </div>
        ) : children
      }
    </AuthContext.Provider>
  );
}
