#!/usr/bin/env bash
set -euo pipefail

wiremock_version="3.13.2"
wiremock_jar="wiremock-standalone-${wiremock_version}.jar"

if [ ! -f "${wiremock_jar}" ]; then
	echo "Missing ${wiremock_jar} in $(pwd)." >&2
	exit 1
fi

java -jar "${wiremock_jar}" --verbose
