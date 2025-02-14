#!/bin/bash

run() {
  readonly operation=${1:?"Name of docker operation"}

  shift

  docker compose -f docker-compose-postgres.yaml "${operation}"
}

run "$@"
