package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEntry
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_credits_approved
import tuindice.summary.generated.resources.summary_credits_failed
import tuindice.summary.generated.resources.summary_credits_header
import tuindice.summary.generated.resources.summary_credits_retired
import tuindice.summary.generated.resources.summary_subjects_approved
import tuindice.summary.generated.resources.summary_subjects_failed
import tuindice.summary.generated.resources.summary_subjects_header
import tuindice.summary.generated.resources.summary_subjects_retired

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

@Composable
fun rememberSummaryItems(
	state: Summary.State.Content
): List<SummaryItem> {
	return buildSummaryItems(
		state = state,
		labels = SummaryItemsLabels(
			subjectsHeader = pluralStringResource(
				Res.plurals.summary_subjects_header,
				state.enrolledSubjects,
				state.enrolledSubjects
			),
			subjectsApprovedLabel = stringResource(Res.string.summary_subjects_approved),
			subjectsFailedLabel = stringResource(Res.string.summary_subjects_failed),
			subjectsRetiredLabel = stringResource(Res.string.summary_subjects_retired),
			creditsHeader = pluralStringResource(
				Res.plurals.summary_credits_header,
				state.enrolledCredits,
				state.enrolledCredits
			),
			creditsApprovedLabel = stringResource(Res.string.summary_credits_approved),
			creditsFailedLabel = stringResource(Res.string.summary_credits_failed),
			creditsRetiredLabel = stringResource(Res.string.summary_credits_retired)
		),
		colors = SummaryItemsColors(
			approved = MaterialTheme.colorScheme.primary,
			failed = MaterialTheme.colorScheme.error,
			retired = MaterialTheme.colorScheme.outline
		)
	)
}
