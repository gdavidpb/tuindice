package com.gdavidpb.tuindice.record.presentation.navigation

import androidx.compose.runtime.Stable
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode

@Stable
class RecordNavDependencies(
	val onNavigateToUpdatePassword: () -> Unit,
	val onNavigateToSubjectDetail: (String) -> Unit,
	val onTopBarViewModeChangeAvailable: (((RecordViewMode) -> Unit)?) -> Unit,
	val onTopBarTermSelectionAvailable: ((() -> Unit)?) -> Unit,
	val onNavigateToEnrollmentProof: () -> Unit
)
