package com.gdavidpb.tuindice.wizard.presentation.model

enum class CoachmarkId {
	Summary,
	Record,
	RecordControls,
	Pensum,
	PensumTools,
	Evaluations,
	EvaluationsTools,
	EvaluationEditor,
	SubjectSearch,
	SubjectDetail,
	SyntheticTerm,
	About,
	AboutActions
}

val CoachmarkId.persistedId: String
	get() = name

val CoachmarkId.tagValue: String
	get() = name
		.replace(Regex("([a-z])([A-Z])"), "$1_$2")
		.lowercase()
