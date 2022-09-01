#!/bin/bash

RED='\033[0;31m'
OR='\033[0;33m'
NC='\033[0m'
LBLUE='\033[1;32m'

if [ $# -eq 1 ]
  then
    echo -e "Device named as ${OR}$1 ${NC}"
    export ISOBUSDEVICE_NAME="$1"    
fi

echo -e "${LBLUE}Setting everything up...${NC}"

docker network create --subnet=172.18.0.0/16 --gateway=172.18.0.1 --attachable isobus_default

docker volume create mongo-data
docker volume create mongo-config

docker-compose up -d 

