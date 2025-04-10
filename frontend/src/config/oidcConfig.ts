import { AuthProviderProps } from "react-oidc-context";

const frontendPort = 8543;
const redirectUri = `http://localhost:${frontendPort}/authentication/callback`;

export const oidcConfig: AuthProviderProps = {
  authority: "https://localhost:8864/realms/sso",
  client_id: "devdemo-backend",
  redirect_uri: redirectUri,
  scope: "openid profile email roles", 
  automaticSilentRenew: true,
  loadUserInfo: true,
};