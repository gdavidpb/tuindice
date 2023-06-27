package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.dateGroup

interface EvaluationFilter {
	fun getLabel(): String
	fun filter(evaluations: List<Evaluation>): List<Evaluation>
}