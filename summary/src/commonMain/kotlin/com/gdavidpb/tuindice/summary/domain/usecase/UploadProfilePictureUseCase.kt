package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UploadProfilePictureUseCase(
	private val userRepository: UserRepository,
	override val paramsValidator: UploadProfilePictureParamsValidator,
	override val exceptionHandler: UploadProfilePictureExceptionHandler
) : FlowUseCase<PlatformFile, String, ProfilePictureUseCaseError>() {
	override suspend fun executeOnBackground(params: PlatformFile): Flow<String> {
		val url = userRepository.uploadProfilePicture(file = params).url

		return flowOf(url)
	}

}
