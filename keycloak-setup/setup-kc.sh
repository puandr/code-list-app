#!/bin/bash

#set -e # Exit on error
#set -x # Enable debugging output

echo 'Configuring Keycloak by importing realm file and adding missing pieces...'

export REALM=sso
export KEYCLOAK_URL="https://localhost:8864"
export ADMIN_USER="admin"
export ADMIN_PASSWORD="admin"

# Create realm and objects defined within realm.json
/opt/keycloak/bin/kcadm.sh create realms \
  --realm master \
  --server "$KEYCLOAK_URL" \
  --user "$ADMIN_USER" \
  --password "$ADMIN_PASSWORD" \
  -f /setup/realm.json  > /dev/null 2>&1  || exit 2

echo "Realm import finished. Fetching client IDs..."

# Get the internal IDs of the clients created by realm.json using grep and cut
KCADM_CMD="/opt/keycloak/bin/kcadm.sh get clients -r \"$REALM\" --realm master --server \"$KEYCLOAK_URL\" --user \"$ADMIN_USER\" --password \"$ADMIN_PASSWORD\" --fields id,clientId --format csv --noquotes 2> /dev/null"


BE_ID=$(eval $KCADM_CMD | grep ",devdemo-backend$" | cut -d ',' -f 1)
B64_ID=$(eval $KCADM_CMD | grep ",devdemo-base64$" | cut -d ',' -f 1)

# Check if IDs were found
if [ -z "$BE_ID" ]; then echo "Failed to get ID for client devdemo-backend"; exit 1; fi
if [ -z "$B64_ID" ]; then echo "Failed to get ID for client devdemo-base64"; exit 1; fi
echo "Found BE_ID: ${BE_ID}"
echo "Found B64_ID: ${B64_ID}"

#  Create Client Roles (assuming realm.json didn't explicitly define them)
echo "Creating client roles..."
/opt/keycloak/bin/kcadm.sh create "clients/${BE_ID}/roles" \
  --realm master \
  --server "$KEYCLOAK_URL" \
  --user "$ADMIN_USER" \
  --password "$ADMIN_PASSWORD" \
  -r "$REALM" \
  -s "name=admin" > /dev/null 2>&1 || echo "Role admin may already exist" # Redirect stdout

/opt/keycloak/bin/kcadm.sh create "clients/${BE_ID}/roles" \
  --realm master \
  --server "$KEYCLOAK_URL" \
  --user "$ADMIN_USER" \
  --password "$ADMIN_PASSWORD" \
  -r "$REALM" \
  -s "name=kasutaja" > /dev/null 2>&1 || echo "Role kasutaja may already exist" # Redirect stdout

/opt/keycloak/bin/kcadm.sh create "clients/${BE_ID}/roles" \
  --realm master \
  --server "$KEYCLOAK_URL" \
  --user "$ADMIN_USER" \
  --password "$ADMIN_PASSWORD" \
  -r "$REALM" \
  -s "name=autenditud" > /dev/null 2>&1 || echo "Role autenditud may already exist" # Redirect stdout

/opt/keycloak/bin/kcadm.sh create "clients/${B64_ID}/roles" \
  --realm master \
  --server "$KEYCLOAK_URL" \
  --user "$ADMIN_USER" \
  --password "$ADMIN_PASSWORD" \
  -r "$REALM" \
  -s "name=admin" > /dev/null 2>&1 || echo "Role admin for base64 may already exist" # Redirect stdout

# backend client protocol mappers 
echo "Creating protocol mappers..."
/opt/keycloak/bin/kcadm.sh create "clients/${BE_ID}/protocol-mappers/add-models" \
  --realm master \
  --server "$KEYCLOAK_URL" \
  --user "$ADMIN_USER" \
  --password "$ADMIN_PASSWORD" \
  -r "$REALM" \
  -f '/setup/devdemo-backend-mappers.json' > /dev/null 2>&1 || echo "Mappers may already exist" # Redirect stdout



echo 'Keycloak configuration script finished!'



