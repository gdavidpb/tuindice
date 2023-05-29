package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEntry
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem

@Composable
@ReadOnlyComposable
fun getSummaryItems(state: Summary.State.Content): List<SummaryItem> {
	return listOf(
		SummaryItem(
			header = pluralStringResource(
				id = R.plurals.text_subjects_header,
				count = state.enrolledSubjects,
				state.enrolledSubjects
			),
			entries = listOf(
				SummaryEntry(
					label = stringResource(id = R.string.label_subjects_approved),
					value = state.approvedSubjects,
					color = MaterialTheme.colorScheme.primary,
				),
				SummaryEntry(
					label = stringResource(id = R.string.label_subjects_failed),
					value = state.failedSubjects,
					color = MaterialTheme.colorScheme.error,
				),
				SummaryEntry(
					label = stringResource(id = R.string.label_subjects_retired),
					value = state.retiredSubjects,
					color = MaterialTheme.colorScheme.outline,
				)
			)
		),
		SummaryItem(
			header = pluralStringResource(
				id = R.plurals.text_credits_header,
				count = state.enrolledCredits,
				state.enrolledCredits
			),
			entries = listOf(
				SummaryEntry(
					label = stringResource(id = R.string.label_credits_approved),
					value = state.approvedCredits,
					color = MaterialTheme.colorScheme.primary,
				),
				SummaryEntry(
					label = stringResource(id = R.string.label_credits_failed),
					value = state.failedCredits,
					color = MaterialTheme.colorScheme.error,
				),
				SummaryEntry(
					label = stringResource(id = R.string.label_credits_retired),
					value = state.retiredCredits,
					color = MaterialTheme.colorScheme.outline,
				)
			)
		)
	)
}