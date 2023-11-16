package com.gdavidpb.tuindice.record.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.Error

sealed class GetQuartersError : Error {
	data object Timeout : GetQuartersError()
	data object Unavailable : GetQuartersError()
	data object OutdatedPassword : GetQuartersError()
	class NoConnection(val isNetworkAvailable: Boolean) : GetQuartersError()
}
