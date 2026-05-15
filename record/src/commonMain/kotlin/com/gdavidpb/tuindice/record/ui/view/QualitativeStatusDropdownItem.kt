package com.gdavidpb.tuindice.record.ui.view

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome

data class QualitativeStatusDropdownItem(
	val outcome: AttemptOutcome,
	val label: String
)
