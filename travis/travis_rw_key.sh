#!/bin/bash

# WARNING: This file is deployed from template. Raise a pull request against ansible-template to change.

set -e
set -x
# Requires Travis CLI and AWS CLI tools

aws ssm get-parameters \
    --region=eu-west-1 \
    --names "travis_rw_key:7" \
    --with-decryption \
    --output json \
	| jq -r ".Parameters[0].Value" > credentials

decrypt=$(travis encrypt-file credentials --no-interactive --print-key --force | grep openssl)

cat<<EOF >decrypt.sh
#!/usr/bin/env bash

# WARNING: This file is deployed from template. Raise a pull request against ansible-template to change.

set -e
set -x
# Decrypt AWS Credentials
mkdir ~/.aws
${decrypt}
EOF

rm credentials
