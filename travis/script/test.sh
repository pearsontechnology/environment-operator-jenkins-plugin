#!/bin/bash
set -x

echo "Add tests here!"
make
export TEST_EXIT_CODE=$?
make clean
exit ${TEST_EXIT_CODE}