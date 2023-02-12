package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isIO
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UploadProfilePictureUseCase(
	private val authRepository: AuthRepository,
	private val accountRepository: AccountRepository,
	private val encoderRepository: EncoderRepository,
	private val networkRepository: NetworkRepository,
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

	override suspend fun executeOnException(throwable: Throwable): ProfilePictureError? {
		return when {
			throwable.isIO() -> ProfilePictureError.IO
			throwable.isTimeout() -> ProfilePictureError.Timeout
			throwable.isConnection() -> ProfilePictureError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}