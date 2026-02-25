package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UploadProfilePictureUseCase(
	private val userRepository: UserRepository,
	private val encoderRepository: EncoderRepository,
	override val paramsValidator: UploadProfilePictureParamsValidator,
	override val exceptionHandler: UploadProfilePictureExceptionHandler
) : FlowUseCase<PlatformUri, String, ProfilePictureUseCaseError>() {
	override suspend fun executeOnBackground(params: PlatformUri): Flow<String> {
		val encodedImage = encoderRepository.encodePicture(uri = params)

		val url = userRepository.uploadProfilePicture(
			content = encodedImage.content,
			mimeType = encodedImage.mimeType
		).url

		return flowOf(url)
	}

}
