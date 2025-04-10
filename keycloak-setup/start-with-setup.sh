#!/bin/bash
set -e # Optional: exit on error

# Run setup that doesn't depend on Keycloak API
echo "Running CA setup script..."
/setup/setup-ca.sh
echo "CA setup script finished."

# --- Import CA certificate section ---
echo "Importing CA certificate into Java truststore..."
# Verify path matches actual CA certificate file
CA_CERT_PATH="/ca-certs/ca.crt"

# # --- DEBUG: Print JAVA_HOME and target Truststore Path ---
# echo "DEBUG: JAVA_HOME is: [$JAVA_HOME]"
# TRUSTSTORE="$JAVA_HOME/lib/security/cacerts"
# echo "DEBUG: Truststore path used: [$TRUSTSTORE]"
# # --- End of DEBUG section ---

# Default Java truststore password
STOREPASS="changeit"

if [ -f "$CA_CERT_PATH" ]; then
    # Check if truststore path actually exists before trying to write
    if [ ! -f "$TRUSTSTORE" ]; then
      # Attempt common alternative path if default derived one fails
      ALT_TRUSTSTORE="/etc/pki/java/cacerts"
      echo "WARN: Default truststore [$TRUSTSTORE] not found. Trying alternative [$ALT_TRUSTSTORE]"
      if [ -f "$ALT_TRUSTSTORE" ]; then
        TRUSTSTORE="$ALT_TRUSTSTORE"
        echo "INFO: Using alternative truststore path [$TRUSTSTORE]"
      else
         echo "ERROR: Neither default [$JAVA_HOME/lib/security/cacerts] nor alternative [$ALT_TRUSTSTORE] truststore found."
         exit 1
      fi
    fi
    # attempt the import using the confirmed TRUSTSTORE path
    keytool -importcert -alias my-local-ca -keystore "$TRUSTSTORE" -storepass "$STOREPASS" -file "$CA_CERT_PATH" -noprompt || { echo "Failed to import CA certificate into [$TRUSTSTORE]"; exit 1; }
    echo "CA certificate imported into [$TRUSTSTORE]."
else
    echo "CA certificate not found at $CA_CERT_PATH"
    exit 1
fi
# --- End of Import section ---


# --- Start Keycloak and rest of script ---
echo "Starting Keycloak in background..."
/opt/keycloak/bin/kc.sh start-dev \
    --https-key-store-file=/certs/tls.pfx \
    --https-key-store-password=devc3rt \
    --https-port=8864 &

KC_PID=$!

echo "Waiting for Keycloak admin CLI to connect..."
timeout 300 bash -c ' \
  until /opt/keycloak/bin/kcadm.sh get serverinfo --server https://localhost:8864 --realm master --user admin --password admin; do \
    echo -n "."; \
    sleep 5; \
  done'


RET=$?
if [ $RET -ne 0 ]; then
  echo "Keycloak did not become ready for kcadm.sh in time (exit code $RET). Exiting."
  kill $KC_PID
  exit 1
fi
echo "Keycloak is ready for kcadm.sh."

echo "Running KC setup script..."

/setup/setup-kc.sh
if [ $? -ne 0 ]; then
  echo "KC setup script failed. Stopping Keycloak."
  kill $KC_PID
  exit 1
fi
echo "KC setup script finished."

echo "Bringing Keycloak process to foreground / waiting..."
wait $KC_PID
