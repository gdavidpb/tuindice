package com.gdavidpb.tuindice.summary.domain.error

sealed class GetAccountError {
	object Timeout : GetAccountError()
	object Unavailable : GetAccountError()
	object AccountDisabled : GetAccountError()
	object OutdatedPassword : GetAccountError()
	class NoConnection(val isNetworkAvailable: Boolean) : GetAccountError()
}