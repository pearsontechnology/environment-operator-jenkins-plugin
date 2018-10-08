#!/usr/bin/env bash

# WARNING: This file is deployed from template. Raise a pull request against ansible-template to change.

set -e
set -x
# Decrypt AWS Credentials
mkdir ~/.aws
openssl aes-256-cbc -K $encrypted_6d477448cd11_key -iv $encrypted_6d477448cd11_iv -in credentials.enc -out credentials -d
