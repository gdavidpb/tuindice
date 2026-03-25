package com.gdavidpb.tuindice.record.ui

object RecordUiTags {
	const val LoadingIndicator = "record_loading_indicator"
	const val QuartersList = "record_quarters_list"
	const val ContentContainer = "record_content_container"
	const val QuarterSelectorRow = "record_quarter_selector_row"
	const val SelectedQuarterSummary = "record_selected_quarter_summary"
	const val SubjectsList = "record_subjects_list"

	const val EmptyContainer = "record_empty_container"
	const val EmptyMessage = "record_empty_message"
	const val EmptyIllustration = "record_empty_illustration"

	fun quarterItem(index: Int): String = "record_quarter_item_$index"
	fun quarterChip(quarterId: String): String = "record_quarter_chip_$quarterId"
	fun subjectItem(subjectId: String): String = "record_subject_item_$subjectId"
	fun subjectCard(subjectId: String): String = "record_subject_card_$subjectId"
	fun subjectGradeSlider(subjectId: String): String = "record_subject_grade_slider_$subjectId"
}
