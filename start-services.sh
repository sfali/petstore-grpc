#!/bin/bash

run() {
  readonly name=${1:?"Name of implementation"}

  shift

  docker compose -f docker-compose-"${name}".yaml up
}

run "$@"
