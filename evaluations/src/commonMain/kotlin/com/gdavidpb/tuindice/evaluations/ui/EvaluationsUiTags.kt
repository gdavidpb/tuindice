package com.gdavidpb.tuindice.evaluations.ui

object EvaluationsUiTags {
	const val EvaluationsContentContainer = "evaluations_content_container"
	const val EvaluationsFiltersContainer = "evaluations_filters_container"
	const val EvaluationsFilterRow = "evaluations_filter_row"
	const val EvaluationsList = "evaluations_list"
	const val EvaluationsAddFab = "evaluations_add_fab"
	const val EvaluationsClearFiltersFab = "evaluations_clear_filters_fab"
	const val EvaluationsLoadingIndicator = "evaluations_loading_indicator"

	const val EvaluationContentContainer = "evaluation_content_container"
	const val EvaluationDoneFab = "evaluation_done_fab"
	const val EvaluationDatePicker = "evaluation_date_picker"
	const val EvaluationDateSelectButton = "evaluation_date_select_button"
	const val EvaluationDateNoDateButton = "evaluation_date_no_date_button"
	const val EvaluationDateDialogTitle = "evaluation_date_dialog_title"
	const val EvaluationDateDialogAcceptButton = "evaluation_date_dialog_accept_button"
	const val EvaluationDateDialogCancelButton = "evaluation_date_dialog_cancel_button"
	const val EvaluationCalendarContainer = "evaluation_calendar_container"
	const val EvaluationCalendarMonthLabel = "evaluation_calendar_month_label"
	const val EvaluationCalendarPreviousMonthButton = "evaluation_calendar_previous_month_button"
	const val EvaluationCalendarNextMonthButton = "evaluation_calendar_next_month_button"
	const val EvaluationWeekdayHeaderRow = "evaluation_weekday_header_row"
	const val EvaluationSubjectPickerRow = "evaluation_subject_picker_row"
	const val EvaluationTypePickerRow = "evaluation_type_picker_row"
	const val EvaluationGradeChip = "evaluation_grade_chip"
	const val EvaluationMaxGradeChip = "evaluation_max_grade_chip"
	const val EvaluationOverdueIndicator = "evaluation_overdue_indicator"
	const val EvaluationLoadingIndicator = "evaluation_loading_indicator"
	const val EvaluationGradeWheelPicker = "evaluation_grade_wheel_picker"
	const val EvaluationGradeWheelSelectedFrame = "evaluation_grade_wheel_selected_frame"
	const val EvaluationDialogTitle = "evaluation_dialog_title"
	const val EvaluationDialogConfirmButton = "evaluation_dialog_confirm_button"
	const val EvaluationDialogDismissButton = "evaluation_dialog_dismiss_button"
	const val EvaluationSwipeToDismissContainer = "evaluation_swipe_to_dismiss_container"

	fun filterChip(label: String): String =
		"evaluations_filter_chip_${label.toTagSuffix()}"

	fun evaluationHeader(label: String): String =
		"evaluation_header_${label.toTagSuffix()}"

	fun evaluationItemCard(evaluationId: String): String =
		"evaluation_item_card_${evaluationId.toTagSuffix()}"

	fun evaluationSubjectChip(subjectId: String): String =
		"evaluation_subject_chip_${subjectId.toTagSuffix()}"

	fun evaluationTypeChip(typeName: String): String =
		"evaluation_type_chip_${typeName.toTagSuffix()}"

	fun calendarDayCell(day: Int): String =
		"evaluation_calendar_day_cell_$day"
}

private fun String.toTagSuffix(): String =
	lowercase()
		.replace(Regex("[^a-z0-9]+"), "_")
		.trim('_')
