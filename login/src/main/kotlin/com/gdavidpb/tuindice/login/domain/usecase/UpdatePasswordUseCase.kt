package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import kotlinx.coroutines.flow.Flow

class UpdatePasswordUseCase(
	override val paramsValidator: UpdatePasswordParamsValidator,
	override val exceptionHandler: UpdatePasswordExceptionHandler
) : FlowUseCase<String, Unit, SignInUseCaseError>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		TODO()
	}
}