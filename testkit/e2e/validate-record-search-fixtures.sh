#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
FLOW="${REPO_ROOT}/e2e/maestro/flows/record/record-synthetic-term-search-states.yaml"
SUBJECT_MAPPINGS_DIR="${REPO_ROOT}/mocks/mappings/subjects"
SUBJECT_FILES_DIR="${REPO_ROOT}/mocks/__files"
SYNC_FIXTURE="${REPO_ROOT}/mocks/__files/sync/post-sync-success.json"
PENSUM_FIXTURE="${REPO_ROOT}/mocks/__files/pensums/get-pensum-2016-degree_project.json"

if ! command -v jq >/dev/null 2>&1; then
	printf 'Missing required command for record search fixture validation: jq\n' >&2
	exit 1
fi

if [[ ! -f "${FLOW}" ]]; then
	printf 'Missing record synthetic term search flow: %s\n' "${FLOW}" >&2
	exit 1
fi

resolve_search_fixture() {
	local query="$1"
	local best_priority=999999
	local best_body=""
	local mapping

	while IFS= read -r mapping; do
		local method
		local url_path
		local body_file
		local contains_query
		local priority

		method="$(jq -r '.request.method // ""' "${mapping}")"
		url_path="$(jq -r '.request.urlPath // ""' "${mapping}")"
		body_file="$(jq -r '.response.bodyFileName // ""' "${mapping}")"
		contains_query="$(jq -r '.request.queryParameters.query.contains // ""' "${mapping}")"
		priority="$(jq -r '.priority // 5' "${mapping}")"

		if [[ "${method}" != "GET" || "${url_path}" != "/subjects/v1/search" || -z "${body_file}" ]]; then
			continue
		fi

		if [[ -n "${contains_query}" && "${query}" != *"${contains_query}"* ]]; then
			continue
		fi

		if [[ "${priority}" -lt "${best_priority}" ]]; then
			best_priority="${priority}"
			best_body="${body_file}"
		fi
	done < <(find "${SUBJECT_MAPPINGS_DIR}" -name 'search-subjects-*.json' -type f | sort)

	if [[ -z "${best_body}" ]]; then
		return 1
	fi

	printf '%s/%s\n' "${SUBJECT_FILES_DIR}" "${best_body}"
}

references_file="$(mktemp)"
trap 'rm -f "${references_file}"' EXIT

awk '
	/-[[:space:]]*(inputText|setClipboard):[[:space:]]*"/ {
		currentQuery = $0
		sub(/^.*:[[:space:]]*"/, "", currentQuery)
		sub(/".*$/, "", currentQuery)
		next
	}
	/id:[[:space:]]*record_create_synthetic_term_search_result_[0-9]+_subject_[A-Z0-9]+/ ||
	/id:[[:space:]]*record_create_synthetic_term_subject_[A-Z0-9]+_/ {
		if (length(currentQuery) < 2) {
			next
		}

		selector = $0
		sub(/^.*id:[[:space:]]*/, "", selector)
		sub(/[[:space:]].*$/, "", selector)

		subjectCode = selector
		if (subjectCode ~ /^record_create_synthetic_term_search_result_[0-9]+_subject_/) {
			sub(/^record_create_synthetic_term_search_result_[0-9]+_subject_/, "", subjectCode)
		} else {
			sub(/^record_create_synthetic_term_subject_/, "", subjectCode)
			sub(/_.*/, "", subjectCode)
		}

		print currentQuery "|" selector "|" subjectCode "|" FNR
	}
' "${FLOW}" > "${references_file}"

invalid_references=0
while IFS='|' read -r query selector subject_code line_number; do
	[[ -z "${query}" || -z "${subject_code}" ]] && continue

	fixture_file="$(resolve_search_fixture "${query}")" || {
		printf 'No subject search fixture resolves query "%s" referenced by %s at %s:%s\n' \
			"${query}" "${selector}" "${FLOW#"${REPO_ROOT}/"}" "${line_number}" >&2
		invalid_references=1
		continue
	}

	if [[ ! -f "${fixture_file}" ]]; then
		printf 'Subject search fixture file is missing for query "%s": %s\n' \
			"${query}" "${fixture_file#"${REPO_ROOT}/"}" >&2
		invalid_references=1
		continue
	fi

	if ! jq -e --arg subject_code "${subject_code}" \
		'.results[]? | select(.subject_code == $subject_code)' \
		"${fixture_file}" >/dev/null; then
		printf 'Record search selector references subject absent from resolved fixture: query="%s" selector=%s subject=%s fixture=%s line=%s\n' \
			"${query}" \
			"${selector}" \
			"${subject_code}" \
			"${fixture_file#"${REPO_ROOT}/"}" \
			"${line_number}" >&2
		invalid_references=1
	fi
done < "${references_file}"

if [[ ! -f "${SYNC_FIXTURE}" ]]; then
	printf 'Missing record sync fixture: %s\n' "${SYNC_FIXTURE#"${REPO_ROOT}/"}" >&2
	invalid_references=1
else
	while IFS='|' read -r subject_code expected_outcome; do
		actual_outcome="$(
			jq -r --arg subject_code "${subject_code}" \
				'[.record.record.terms[]?.attempts[]? | select(.subject_code == $subject_code) | .academic_outcome][0] // ""' \
				"${SYNC_FIXTURE}"
		)"
		if [[ "${actual_outcome}" != "${expected_outcome}" ]]; then
			printf 'Record sync fixture has unexpected historical outcome: subject=%s expected=%s actual=%s fixture=%s\n' \
				"${subject_code}" \
				"${expected_outcome}" \
				"${actual_outcome:-<missing>}" \
				"${SYNC_FIXTURE#"${REPO_ROOT}/"}" >&2
			invalid_references=1
		fi
	done <<'EOF'
MA1111|approved
MA1112|retired
MA1121|failed
EOF

	while IFS='|' read -r subject_code expected_kind expected_outcome expected_year expected_period; do
		actual_term="$(
			jq -r --arg subject_code "${subject_code}" \
				'[.record.record.terms[]? | select(any(.attempts[]?; .subject_code == $subject_code)) | {
					kind: .term_kind,
					year: (.period_year | tostring),
					period: .period_code,
					outcome: ([.attempts[]? | select(.subject_code == $subject_code) | .academic_outcome][0] // "")
				}][0] // {} | [.kind // "", .outcome // "", .year // "", .period // ""] | @tsv' \
				"${SYNC_FIXTURE}"
		)"
		expected_term="${expected_kind}	${expected_outcome}	${expected_year}	${expected_period}"
		if [[ "${actual_term}" != "${expected_term}" ]]; then
			printf 'Record sync fixture has unexpected tooltip term context: subject=%s expected="%s" actual="%s" fixture=%s\n' \
				"${subject_code}" \
				"${expected_term}" \
				"${actual_term:-<missing>}" \
				"${SYNC_FIXTURE#"${REPO_ROOT}/"}" >&2
			invalid_references=1
		fi
	done <<'EOF'
EP1308|synthetic|pending|2026|JUL_AUG
MA1111|historical|approved|2021|SEP_DEC
EOF
fi

if [[ ! -f "${PENSUM_FIXTURE}" ]]; then
	printf 'Missing pensum fixture: %s\n' "${PENSUM_FIXTURE#"${REPO_ROOT}/"}" >&2
	invalid_references=1
else
	ep2308_missing_requirements="$(
		jq -r '
			.selected_pensum_id as $selected |
			([.pensums[]? | select(.id == $selected)][0] // .pensum) as $pensum |
			($pensum.nodes[]? | select(.subject_code == "EP2308") | .id) as $ep2308NodeId |
			[
				$pensum.edges[]?
				| select(.to_node_id == $ep2308NodeId and .relationship_type == "REQUIREMENT")
				| .from_node_id as $fromNodeId
				| $pensum.nodes[]?
				| select(.id == $fromNodeId)
				| .subject_code
			]
			| sort
			| join(",")
		' "${PENSUM_FIXTURE}"
	)"
	if [[ "${ep2308_missing_requirements}" != "EP1308,EP5855" ]]; then
		printf 'Pensum fixture has unexpected EP2308 requirement context: expected=EP1308,EP5855 actual=%s fixture=%s\n' \
			"${ep2308_missing_requirements:-<missing>}" \
			"${PENSUM_FIXTURE#"${REPO_ROOT}/"}" >&2
		invalid_references=1
	fi
fi

if [[ "${invalid_references}" == "1" ]]; then
	exit 1
fi

printf 'Record search fixtures are valid.\n'
