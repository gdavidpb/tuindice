package com.gdavidpb.tuindice.record.ui

object RecordUiTags {
	const val LoadingIndicator = "record_loading_indicator"
	const val ContentContainer = "record_content_container"
	const val TermSelectorRow = "record_term_selector_row"
	const val TermPager = "record_term_pager"
	const val SelectedTermSummary = "record_selected_term_summary"
	const val AttemptsList = "record_attempts_list"
	const val TopBarViewModeSwitch = "record_top_bar_view_mode_switch"
	const val TopBarViewModeButton = "record_top_bar_view_mode_button"
	const val TopBarViewModeBanner = "record_top_bar_view_mode_banner"
	const val TopBarViewModeInfoButton = "record_top_bar_view_mode_info_button"
	const val ViewModeInfoMessage = "record_view_mode_info_message"

	const val EmptyContainer = "record_empty_container"
	const val EmptyTitle = "record_empty_title"
	const val EmptyMessage = "record_empty_message"
	const val EmptyIllustration = "record_empty_illustration"

	fun termChip(termId: String): String = "record_term_chip_$termId"
	fun termCurrentChip(termId: String): String = "record_term_current_chip_$termId"
	fun attemptItem(attemptId: String): String = "record_attempt_item_$attemptId"
	fun attemptCard(attemptId: String): String = "record_attempt_card_$attemptId"
	fun attemptSubjectChip(attemptId: String): String = "record_attempt_subject_chip_$attemptId"
	fun attemptGradeSlider(attemptId: String): String = "record_attempt_grade_slider_$attemptId"
	fun attemptStatusChip(attemptId: String): String = "record_attempt_status_chip_$attemptId"
	fun attemptStatusSelector(attemptId: String): String = "record_attempt_status_selector_$attemptId"
	fun attemptStatusOption(attemptId: String, status: String): String = "record_attempt_status_option_${attemptId}_$status"
}
