package com.gdavidpb.tuindice.base.presentation.model

// The tone an evaluation's state reads in. An overdue one is an error; a completed one is a success, which is drawn in
// the same surface as a state with no verdict, so the screens that list evaluations draw the two alike.
enum class EvaluationTone {
	Success,
	Error,
	Neutral
}
