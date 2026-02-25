package com.gdavidpb.tuindice.evaluations.domain.repository

interface EvaluationFilterLabelsProvider {
	fun pending(): String
	fun completed(): String
	fun noGrade(): String
	fun date(date: Long?): String
}
