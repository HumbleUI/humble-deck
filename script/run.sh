#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
cd "$(dirname "$0")/.."

clj -M -m humble-deck.main $@
