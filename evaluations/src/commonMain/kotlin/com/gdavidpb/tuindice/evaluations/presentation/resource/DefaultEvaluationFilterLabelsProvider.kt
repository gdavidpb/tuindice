package com.gdavidpb.tuindice.evaluations.presentation.resource

import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsToNow

class DefaultEvaluationFilterLabelsProvider : EvaluationFilterLabelsProvider {
	override fun pending(): String = "Pendientes"

	override fun completed(): String = "Completadas"

	override fun noGrade(): String = "Sin nota"

	override fun date(date: Long?): String = date.formatAsToNow()
}
