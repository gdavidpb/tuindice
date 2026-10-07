#!/usr/bin/env bash
# Stand-in for .github/scripts/common.sh inside the temporary repositories of the harness tests.
e2e_status_context() {
	printf 'local-e2e/%s/local-certification-suite\n' "$1"
}
