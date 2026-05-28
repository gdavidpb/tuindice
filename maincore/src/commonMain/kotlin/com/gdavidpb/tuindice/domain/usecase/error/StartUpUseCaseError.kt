package com.gdavidpb.tuindice.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface StartUpUseCaseError : UseCaseError {
	object NoServices : StartUpUseCaseError
}
