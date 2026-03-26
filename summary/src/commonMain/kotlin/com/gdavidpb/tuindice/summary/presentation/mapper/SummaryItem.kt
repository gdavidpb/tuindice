package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItemsColors
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItemsLabels
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEntry
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem

fun Summary.State.Content.toSummaryItemList(
	labels: SummaryItemsLabels,
	colors: SummaryItemsColors
): List<SummaryItem> {
	return listOf(
		SummaryItem(
			header = labels.subjectsHeader,
			entries = listOf(
				SummaryEntry(
					label = labels.subjectsApprovedLabel,
					value = approvedSubjects,
					color = colors.approved
				),
				SummaryEntry(
					label = labels.subjectsFailedLabel,
					value = failedSubjects,
					color = colors.failed
				),
				SummaryEntry(
					label = labels.subjectsRetiredLabel,
					value = retiredSubjects,
					color = colors.retired
				)
			)
		),
		SummaryItem(
			header = labels.creditsHeader,
			entries = listOf(
				SummaryEntry(
					label = labels.creditsApprovedLabel,
					value = approvedCredits,
					color = colors.approved
				),
				SummaryEntry(
					label = labels.creditsFailedLabel,
					value = failedCredits,
					color = colors.failed
				),
				SummaryEntry(
					label = labels.creditsRetiredLabel,
					value = retiredCredits,
					color = colors.retired
				)
			)
		)
	)
}
