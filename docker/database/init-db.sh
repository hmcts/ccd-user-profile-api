#!/usr/bin/env bash

set -e

if [ -z "$USER_PROFILE_DB_USERNAME" ] || [ -z "$USER_PROFILE_DB_PASSWORD" ]; then
  echo "ERROR: Missing environment variable. Set value for both 'USER_PROFILE_DB_USERNAME' and 'USER_PROFILE_DB_PASSWORD'."
  exit 1
fi

# Create role and database
psql -v --username postgres <<-EOSQL
  CREATE USER ${USER_PROFILE_DB_USERNAME} WITH PASSWORD '${USER_PROFILE_DB_PASSWORD}';
  CREATE DATABASE ccd_user_profile
    WITH OWNER = ${USER_PROFILE_DB_USERNAME}
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
  ALTER SCHEMA public OWNER TO ${USER_PROFILE_DB_USERNAME};
EOSQL
