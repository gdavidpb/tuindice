package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummaryItemList
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItemsColors
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItemsLabels
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

@Composable
fun rememberSummaryItems(
	state: Summary.State.Content
): List<SummaryItem> {
	val labels = SummaryItemsLabels(
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
	)
	val colors = SummaryItemsColors(
		approved = MaterialTheme.colorScheme.primary,
		failed = MaterialTheme.colorScheme.error,
		retired = MaterialTheme.colorScheme.outline
	)

	return remember(state, labels, colors) {
		state.toSummaryItemList(
			labels = labels,
			colors = colors
		)
	}
}
