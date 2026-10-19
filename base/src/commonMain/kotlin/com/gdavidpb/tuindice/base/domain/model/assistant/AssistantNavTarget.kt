package com.gdavidpb.tuindice.base.domain.model.assistant

/**
 * The screens an assistant action can open. They mirror the targets of the contract one to one; the mapping from the
 * wire names lives with the data layer, so the domain does not depend on the contract.
 */
enum class AssistantNavTarget {
	SUMMARY,
	STATISTICS,
	SHARE_TERM,
	RECORD,
	RECORD_PROJECTION,
	RECORD_SCHEDULE,
	RECORD_CREATE_TERM,
	EVALUATIONS,
	EVALUATIONS_ADD,
	EVALUATIONS_DETAIL,
	PENSUM,
	SUBJECTS_SEARCH,
	SUBJECTS_DETAIL,
	ENROLLMENT_PROOF,
	ABOUT,
	SIGN_IN,
	UPDATE_PASSWORD
}
