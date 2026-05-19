package com.gdavidpb.tuindice.record.domain.usecase.param

import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import kotlinx.coroutines.flow.StateFlow

data class ObserveSyntheticTermCreationParams(
	val queryFlow: StateFlow<String>,
	val selectedSubjectsFlow: StateFlow<List<SyntheticTermSubject>>,
	val selectedPeriodKeyFlow: StateFlow<String?>,
	val editingTermIdFlow: StateFlow<String?>,
	val editingTermKeyFlow: StateFlow<String?>
)
