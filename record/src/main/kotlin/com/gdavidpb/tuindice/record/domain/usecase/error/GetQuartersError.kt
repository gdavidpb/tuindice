package com.gdavidpb.tuindice.record.domain.usecase.error

sealed class GetQuartersError {
	object Timeout : GetQuartersError()
	object Unavailable : GetQuartersError()
	object AccountDisabled : GetQuartersError()
	object OutdatedPassword : GetQuartersError()
	class NoConnection(val isNetworkAvailable: Boolean) : GetQuartersError()
}
