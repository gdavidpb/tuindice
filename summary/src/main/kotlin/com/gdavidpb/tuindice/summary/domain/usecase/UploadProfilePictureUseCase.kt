package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UploadProfilePictureUseCase(
	private val authRepository: AuthRepository,
	private val accountRepository: AccountRepository,
	private val encoderRepository: EncoderRepository,
	override val exceptionHandler: UploadProfilePictureExceptionHandler,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, String, ProfilePictureError>() {
	override suspend fun executeOnBackground(params: String): Flow<String> {
		val activeUId = authRepository.getActiveAuth().uid
		val encodedPicture = encoderRepository.encodePicture(path = params)

		val url = accountRepository.uploadProfilePicture(
			uid = activeUId,
			encodedPicture = encodedPicture
		).url

		return flowOf(url)
	}

}