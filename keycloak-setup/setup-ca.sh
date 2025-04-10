#!/bin/bash

echo 'Adding CA to trusted CA-s list...'
#cp /certs/ca.crt /etc/pki/ca-trust/source/ || exit 1
#update-ca-trust || exit 1
(cat /certs/ca.crt >> /etc/ssl/certs/ca-certificates.crt) || exit 1
