package com.gdavidpb.tuindice.summary.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.Error

sealed class GetAccountError : Error {
	data object Timeout : GetAccountError()
	data object Unavailable : GetAccountError()
	data object OutdatedPassword : GetAccountError()
	class NoConnection(val isNetworkAvailable: Boolean) : GetAccountError()
}