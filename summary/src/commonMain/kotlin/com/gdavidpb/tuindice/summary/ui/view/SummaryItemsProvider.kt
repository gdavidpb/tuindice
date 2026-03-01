package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.resource.SummaryItemsTextProvider
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem

@Composable
fun rememberSummaryItems(
	state: Summary.State.Content,
	textProvider: SummaryItemsTextProvider
): List<SummaryItem> {
	return buildSummaryItems(
		state = state,
		labels = SummaryItemsLabels(
			subjectsHeader = textProvider.subjectsHeader(state.enrolledSubjects),
			subjectsApprovedLabel = textProvider.subjectsApprovedLabel(),
			subjectsFailedLabel = textProvider.subjectsFailedLabel(),
			subjectsRetiredLabel = textProvider.subjectsRetiredLabel(),
			creditsHeader = textProvider.creditsHeader(state.enrolledCredits),
			creditsApprovedLabel = textProvider.creditsApprovedLabel(),
			creditsFailedLabel = textProvider.creditsFailedLabel(),
			creditsRetiredLabel = textProvider.creditsRetiredLabel()
		),
		colors = SummaryItemsColors(
			approved = MaterialTheme.colorScheme.primary,
			failed = MaterialTheme.colorScheme.error,
			retired = MaterialTheme.colorScheme.outline
		)
	)
}
