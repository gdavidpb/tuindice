package com.gdavidpb.tuindice.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.model.ServicesStatus
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed class StartUpUseCaseError : UseCaseError {
	class NoServices(val status: ServicesStatus) : StartUpUseCaseError()
}
