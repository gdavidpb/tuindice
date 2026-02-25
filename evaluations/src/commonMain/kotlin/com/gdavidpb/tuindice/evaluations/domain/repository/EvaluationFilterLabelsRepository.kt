package com.gdavidpb.tuindice.evaluations.domain.repository

interface EvaluationFilterLabelsRepository {
	fun pending(): String
	fun completed(): String
	fun noGrade(): String
	fun date(date: Long?): String
}
