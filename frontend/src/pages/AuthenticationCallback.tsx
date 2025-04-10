import React from 'react';
import { useAuth } from 'react-oidc-context';
import { useNavigate } from 'react-router-dom';

export function AuthenticationCallback() {
  const auth = useAuth();
  const navigate = useNavigate();

  React.useEffect(() => {
    if (auth.isLoading) {
      return; 
    }

    if (auth.error) {
      console.error("OIDC Callback Error:", auth.error);
      navigate('/'); 
      return;
    }

    if (auth.isAuthenticated) {
      console.log("OIDC Callback Success:", auth.user);
      navigate('/'); 
    }
  }, [auth, navigate]);

  if (auth.isLoading) {
    return <div>Loading authentication...</div>;
  }

  if(auth.error) {
    return <div>Authentication failed: {auth.error.message}</div>
  }

  return <div>Processing login callback...</div>; 
}
