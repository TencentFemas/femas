#!/bin/bash

set -e
cd /usr/local/src/femas
BOOTSTRAP=$(find /usr/local/src/femas -regex ".*/femas-admin-starter-.*/startup.sh$")
bash ${BOOTSTRAP} 
tail -f $(dirname ${BOOTSTRAP})/../logs/*.log
