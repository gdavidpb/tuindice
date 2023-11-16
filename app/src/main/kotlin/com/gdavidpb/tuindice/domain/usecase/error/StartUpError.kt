package com.gdavidpb.tuindice.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.model.ServicesStatus
import com.gdavidpb.tuindice.base.domain.usecase.base.Error

sealed class StartUpError : Error {
	class NoServices(val status: ServicesStatus) : StartUpError()
}
