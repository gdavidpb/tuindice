package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation

data class GetEvaluations(
	val originalEvaluations: List<Evaluation>,
	val filteredEvaluations: List<Evaluation>
)