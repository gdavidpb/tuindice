package com.gdavidpb.tuindice.record.presentation.model

data class RecordMapperTexts(
	val quarterGradeDiff: (Float) -> String,
	val quarterGradeSum: (Float) -> String,
	val quarterCredits: (Int) -> String,
	val subjectRetired: String,
	val subjectStatus: (String) -> String,
	val subjectGrade: (Int) -> String,
	val subjectCredits: (Int) -> String
)
