package com.gdavidpb.tuindice.evaluations.ui

import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey

object EvaluationsUiTags {
	const val EvaluationsContentContainer = "evaluations_content_container"
	const val EvaluationsWeekStrip = "evaluations_week_strip"
	const val EvaluationsWeekLabel = "evaluations_week_label"
	const val EvaluationsList = "evaluations_list"
	const val EvaluationsAddFab = "evaluations_add_fab"
	const val EvaluationsLoadingIndicator = "evaluations_loading_indicator"

	const val EvaluationContentContainer = "evaluation_content_container"
	const val EvaluationDoneFab = "evaluation_done_fab"
	const val EvaluationDoneProgress = "evaluation_done_progress"
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
	const val EvaluationTypeLeadingIcon = "evaluation_type_leading_icon"
	const val EvaluationTypeInlineIcon = "evaluation_type_inline_icon"
	const val EvaluationStatusChip = "evaluation_status_chip"
	const val EvaluationGradeActionButton = "evaluation_grade_action_button"
	const val EvaluationLoadingIndicator = "evaluation_loading_indicator"
	const val EvaluationGradeWheelPicker = "evaluation_grade_wheel_picker"
	const val EvaluationGradeWheelSelectedFrame = "evaluation_grade_wheel_selected_frame"
	const val EvaluationDialogTitle = "evaluation_dialog_title"
	const val EvaluationDialogSubtitle = "evaluation_dialog_subtitle"
	const val EvaluationDialogSubjectCodeChip = "evaluation_dialog_subject_code_chip"
	const val EvaluationDialogConfirmButton = "evaluation_dialog_confirm_button"
	const val EvaluationDialogDismissButton = "evaluation_dialog_dismiss_button"
	const val EvaluationSwipeToDismissContainer = "evaluation_swipe_to_dismiss_container"
	const val EvaluationSwipeEditAction = "evaluation_swipe_edit_action"
	const val EvaluationSwipeDeleteAction = "evaluation_swipe_delete_action"
	const val DeleteEvaluationMessage = "evaluation_delete_confirmation_message"
	const val EvaluationSubjectRequiredError = "evaluation_subject_required_error"
	const val EvaluationTypeRequiredError = "evaluation_type_required_error"
	const val EvaluationMaxGradeRequiredError = "evaluation_max_grade_required_error"

	fun evaluationHeader(label: String): String =
		"evaluation_header_${label.toTagSuffix()}"

	fun evaluationsWeekPage(key: EvaluationsWeekKey): String =
		"evaluations_week_page_${key.tagSuffix}"

	fun evaluationsWeekPage(weekNumber: Int): String =
		evaluationsWeekPage(EvaluationsWeekKey.Academic(weekNumber))

	fun evaluationsWeekChip(key: EvaluationsWeekKey): String =
		"evaluations_week_chip_${key.tagSuffix}"

	fun evaluationsWeekChip(weekNumber: Int): String =
		evaluationsWeekChip(EvaluationsWeekKey.Academic(weekNumber))

	fun evaluationsWeekHeader(key: EvaluationsWeekKey): String =
		"evaluations_week_header_${key.tagSuffix}"

	fun evaluationsWeekHeader(weekNumber: Int): String =
		evaluationsWeekHeader(EvaluationsWeekKey.Academic(weekNumber))

	fun evaluationItemCard(evaluationId: String): String =
		"evaluation_item_card_${evaluationId.toTagSuffix()}"

	fun evaluationGradeActionButton(evaluationId: String): String =
		"${EvaluationGradeActionButton}_${evaluationId.toTagSuffix()}"

	fun evaluationSubjectChip(attemptId: String): String =
		"evaluation_subject_chip_${attemptId.toTagSuffix()}"

	fun evaluationTypeChip(typeName: String): String =
		"evaluation_type_chip_${typeName.toTagSuffix()}"

	fun calendarDayCell(day: Int): String =
		"evaluation_calendar_day_cell_$day"
}

private fun String.toTagSuffix(): String =
	lowercase()
		.replace(Regex("[^a-z0-9]+"), "_")
		.trim('_')
