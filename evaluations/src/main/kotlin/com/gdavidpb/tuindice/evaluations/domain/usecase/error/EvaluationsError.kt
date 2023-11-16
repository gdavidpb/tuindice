package com.gdavidpb.tuindice.evaluations.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.Error

sealed class EvaluationsError : Error {
	data object Timeout : EvaluationsError()
	data object Unavailable : EvaluationsError()
	class NoConnection(val isNetworkAvailable: Boolean) : EvaluationsError()
}