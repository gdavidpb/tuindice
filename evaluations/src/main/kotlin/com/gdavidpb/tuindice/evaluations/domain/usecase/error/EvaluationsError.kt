package com.gdavidpb.tuindice.evaluations.domain.usecase.error

sealed class EvaluationsError {
	object Timeout : EvaluationsError()
	object Unavailable : EvaluationsError()
	class NoConnection(val isNetworkAvailable: Boolean) : EvaluationsError()
}