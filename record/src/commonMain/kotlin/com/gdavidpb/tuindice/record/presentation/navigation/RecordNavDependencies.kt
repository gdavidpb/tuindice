package com.gdavidpb.tuindice.record.presentation.navigation

import androidx.compose.runtime.Stable
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import kotlinx.datetime.LocalDate

@Stable
class RecordNavDependencies(
	val onNavigateToUpdatePassword: () -> Unit,
	val onNavigateToSubjectDetail: (String) -> Unit,
	val onTopBarViewModeChangeAvailable: (((RecordViewMode) -> Unit)?) -> Unit,
	val onTopBarTermSelectionAvailable: ((() -> Unit)?) -> Unit,
	val onNavigateToEnrollmentProof: () -> Unit,
	// The Schedule sheet asks the host to show the Evaluations of the week that this Monday opens, on the
	// evaluation with this id.
	val onNavigateToEvaluationsWeek: (weekStart: LocalDate, evaluationId: String) -> Unit
)
