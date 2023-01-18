package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import java.util.*

data class EvaluationItem(
	val uid: Long,
	val id: String,
	val nameText: CharSequence,
	val grade: Double,
	val maxGrade: Double,
	val dateText: CharSequence,
	val typeText: CharSequence,
	val date: Date,
	val isDone: Boolean,
	val data: Evaluation
)