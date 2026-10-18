package com.gdavidpb.tuindice.chatcontract.context

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Screen of the app the assistant is looking at. The values mirror the app's AssistantScreen. */
@Serializable
public enum class ScreenId {
	@SerialName("summary")
	SUMMARY,

	@SerialName("statistics")
	STATISTICS,

	@SerialName("record")
	RECORD,

	@SerialName("pensum")
	PENSUM,

	@SerialName("evaluations")
	EVALUATIONS,

	@SerialName("evaluation_detail")
	EVALUATION_DETAIL,

	@SerialName("subject_detail")
	SUBJECT_DETAIL,

	@SerialName("subject_search")
	SUBJECT_SEARCH,

	@SerialName("about")
	ABOUT,

	/** The term report ("Tu trimestre") the student opened from the summary or from its push. */
	@SerialName("term_report")
	TERM_REPORT
}
