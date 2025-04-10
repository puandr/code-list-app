import { useAuth } from 'react-oidc-context';

export function AuthStatus() {
  const auth = useAuth();

  const handleLogin = () => {
    auth.signinRedirect(); 
  };

  const handleLogout = () => {
    auth.signoutRedirect(); 
  };

  if (auth.isLoading) {
    return <div className="auth-status">Loading...</div>;
  }

  if (auth.error) {
    return <div className="auth-status error">Error: {auth.error.message}</div>;
  }

  if (auth.isAuthenticated) {
    return (
      <div className="auth-status">
        <span>Hello, {auth.user?.profile?.preferred_username ?? auth.user?.profile?.name ?? 'User'}</span>
        <button onClick={handleLogout} className="ml-4 p-1 border rounded bg-red-500 text-white">
          Log Out
        </button>
      </div>
    );
  }

  return (
    <div className="auth-status">
      <button onClick={handleLogin} className="p-1 border rounded bg-blue-500 text-white">
        Log In
      </button>
    </div>
  );
}