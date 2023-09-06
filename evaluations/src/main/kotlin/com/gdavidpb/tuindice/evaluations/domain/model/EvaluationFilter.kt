package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation

interface EvaluationFilter {
	fun getLabel(): String
	fun filter(evaluation: Evaluation): Boolean
}