package com.gdavidpb.tuindice.base.presentation.model

sealed class TopBarAction(
	val action: String
) {
	data object SignOutAction : TopBarAction(
		action = "sign_out"
	)

	data object FetchEnrollmentProofAction : TopBarAction(
		action = "enrollment_proof"
	)

	data object RecordTermSelectionAction : TopBarAction(
		action = "record_term_selection"
	)

	data object RecordScheduleAction : TopBarAction(
		action = "record_schedule"
	)

	data object EvaluationsScheduleAction : TopBarAction(
		action = "evaluations_schedule"
	)

	data object SearchPensumAction : TopBarAction(
		action = "search_pensum"
	)

	data object ChangePensumAction : TopBarAction(
		action = "change_pensum"
	)

	// The Statistics screen offers to share one of its closed terms as an image.
	data object StatisticsShareAction : TopBarAction(
		action = "statistics_share"
	)

	// The term report ("Tu trimestre") shares its term as an image, the sheet of the Statistics screen too.
	data object TermReportShareAction : TopBarAction(
		action = "term_report_share"
	)

	// The assistant's sparkle in the bar: asks the assistant about the screen the bar belongs to.
	data object AssistantAction : TopBarAction(
		action = "assistant"
	)
}
