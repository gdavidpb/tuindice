package com.gdavidpb.tuindice.evaluations.presentation.resource

interface EvaluationFilterLabelsProvider {
	fun pending(): String
	fun completed(): String
	fun noGrade(): String
	fun date(date: Long?): String
}
