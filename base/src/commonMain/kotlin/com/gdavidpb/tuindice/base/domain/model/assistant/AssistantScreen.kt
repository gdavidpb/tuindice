package com.gdavidpb.tuindice.base.domain.model.assistant

import kotlinx.serialization.Serializable

@Serializable
enum class AssistantScreen {
	SUMMARY,
	STATISTICS,
	RECORD,
	PENSUM,
	EVALUATIONS,
	EVALUATION_DETAIL,
	SUBJECT_DETAIL,
	SUBJECT_SEARCH,
	ABOUT,
	TERM_REPORT
}
