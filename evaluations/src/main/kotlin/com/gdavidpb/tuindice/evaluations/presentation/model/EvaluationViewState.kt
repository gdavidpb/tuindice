package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import java.util.Date

data class EvaluationViewState(
	val subjectHeader: String,
	val name: String,
	val maxGrade: Double,
	val date: Date,
	val isDateSet: Boolean,
	val type: EvaluationType
)