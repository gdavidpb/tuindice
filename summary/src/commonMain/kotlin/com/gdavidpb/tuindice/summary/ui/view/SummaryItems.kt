package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEntry
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem

data class SummaryItemsLabels(
	val subjectsHeader: String,
	val subjectsApprovedLabel: String,
	val subjectsFailedLabel: String,
	val subjectsRetiredLabel: String,
	val creditsHeader: String,
	val creditsApprovedLabel: String,
	val creditsFailedLabel: String,
	val creditsRetiredLabel: String
)

data class SummaryItemsColors(
	val approved: Color,
	val failed: Color,
	val retired: Color
)

fun buildSummaryItems(
	state: Summary.State.Content,
	labels: SummaryItemsLabels,
	colors: SummaryItemsColors
): List<SummaryItem> {
	return listOf(
		SummaryItem(
			header = labels.subjectsHeader,
			entries = listOf(
				SummaryEntry(
					label = labels.subjectsApprovedLabel,
					value = state.approvedSubjects,
					color = colors.approved
				),
				SummaryEntry(
					label = labels.subjectsFailedLabel,
					value = state.failedSubjects,
					color = colors.failed
				),
				SummaryEntry(
					label = labels.subjectsRetiredLabel,
					value = state.retiredSubjects,
					color = colors.retired
				)
			)
		),
		SummaryItem(
			header = labels.creditsHeader,
			entries = listOf(
				SummaryEntry(
					label = labels.creditsApprovedLabel,
					value = state.approvedCredits,
					color = colors.approved
				),
				SummaryEntry(
					label = labels.creditsFailedLabel,
					value = state.failedCredits,
					color = colors.failed
				),
				SummaryEntry(
					label = labels.creditsRetiredLabel,
					value = state.retiredCredits,
					color = colors.retired
				)
			)
		)
	)
}
