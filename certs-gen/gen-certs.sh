#!/bin/bash

# Loob CA ja serdid arenduskeskkonna jaoks.
CERTS_DIRECTORY='/certs'
KC_CERT_DIR='/kc-certs'
BE_CERT_DIR='/be-certs'
BASE64_CERT_DIR='/base64-certs'
CA_CERT_DIR='/ca-certs'

CN='DevDemo-RootCA'
C='EE'
ST='Eesti'
L='Eestimaa'
O='Arenduskeskkond'
OU='Arendaja arvuti'

CWD=$(realpath "$(pwd)")

cd "$CERTS_DIRECTORY" || exit 1

if [ -d certs ]
then 
  echo "Serdid on olemas - ei tee uuesti!"
  exit 0
fi

echo "Creating CA..."

mkdir -p certs crl newcerts private csr || exit 1
(echo '1000' > serial) || exit 1
(echo '0100' > crlnumber) || exit 1
touch index.txt || exit 1

cp "${CWD}/openssl.cnf" . || exit 1

openssl genrsa -out 'private/ca.key' 4096 || exit 1
#chmod 400 private/ca.key
openssl req -config openssl.cnf -key private/ca.key -new -x509 -days 7300 -sha256 -extensions v3_ca -out certs/ca.crt -subj "/C=${C}/ST=${ST}/L=${L}/O=${O}/OU=I${OU}/CN=${CN}" || exit 1
cp certs/ca.crt "${CA_CERT_DIR}/ca.crt" || exit 1

# Loome serdid
echo "Loon Keycloak sertifikaati..."
openssl genrsa -out private/kc.key.pem 4096 || exit 1
openssl req -new -key private/kc.key.pem -config "${CWD}/kc.csr.conf" -out csr/kc-1.csr || exit 1
openssl ca -config openssl.cnf  -in csr/kc-1.csr  -out certs/kc-1.crt  -days 2650 -notext -batch || exit 1

cp certs/ca.crt "${KC_CERT_DIR}/" || exit 1
cp certs/kc-1.crt "${KC_CERT_DIR}/tls.crt" || exit 1
cp private/kc.key.pem "${KC_CERT_DIR}/tls.key" || exit 1
openssl pkcs12 -export -out "${KC_CERT_DIR}/tls.pfx" -inkey "${KC_CERT_DIR}/tls.key" -in "${KC_CERT_DIR}/tls.crt" -certfile "${KC_CERT_DIR}/ca.crt" -password 'pass:devc3rt' || exit 1
chmod a+r "${KC_CERT_DIR}"/*

echo "Loon tagasüsteemi sertifikaati..."
openssl genrsa -out private/be.key.pem 4096 || exit 1
openssl req -new -key private/be.key.pem -config "${CWD}/be.csr.conf" -out csr/be-1.csr || exit 1
openssl ca -config openssl.cnf  -in csr/be-1.csr  -out certs/be-1.crt  -days 2650 -notext -batch || exit 1

cp certs/ca.crt "${BE_CERT_DIR}/" || exit 1
cp certs/be-1.crt "${BE_CERT_DIR}/tls.crt" || exit 1
cp private/be.key.pem "${BE_CERT_DIR}/tls.key" || exit 1
openssl pkcs12 -export -out "${BE_CERT_DIR}/tls.pfx" -inkey "${BE_CERT_DIR}/tls.key" -in "${BE_CERT_DIR}/tls.crt" -certfile "${BE_CERT_DIR}/ca.crt" -password 'pass:devc3rt' || exit 1
chmod a+r "${BE_CERT_DIR}"/*

echo "Loon base64 mikroteenuse sertifikaati..."
openssl genrsa -out private/base64.key.pem 4096 || exit 1
openssl req -new -key private/base64.key.pem -config "${CWD}/base64.csr.conf" -out csr/base64-1.csr || exit 1
openssl ca -config openssl.cnf  -in csr/base64-1.csr  -out certs/base64-1.crt  -days 2650 -notext -batch || exit 1

cp certs/ca.crt "${BASE64_CERT_DIR}/" || exit 1
cp certs/base64-1.crt "${BASE64_CERT_DIR}/tls.crt" || exit 1
cp private/base64.key.pem "${BASE64_CERT_DIR}/tls.key" || exit 1
openssl pkcs12 -export -out "${BASE64_CERT_DIR}/tls.pfx" -inkey "${BASE64_CERT_DIR}/tls.key" -in "${BASE64_CERT_DIR}/tls.crt" -certfile "${BASE64_CERT_DIR}/ca.crt" -password 'pass:devc3rt' || exit 1
chmod a+r "${BASE64_CERT_DIR}"/*

echo "Certificates generated!"
