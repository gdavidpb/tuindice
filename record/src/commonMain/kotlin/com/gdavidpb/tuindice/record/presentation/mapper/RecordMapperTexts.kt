package com.gdavidpb.tuindice.record.presentation.mapper

data class RecordMapperTexts(
	val termGrade: (Float) -> String,
	val termGradeSum: (Float) -> String,
	val termCredits: (Int) -> String,
	val termAttemptGrade: (Int) -> String,
	val termAttemptCredits: (Int) -> String
)
