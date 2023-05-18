package com.gdavidpb.tuindice.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.model.ServicesStatus

sealed class StartUpError {
	class NoServices(val status: ServicesStatus) : StartUpError()
}
