package com.gdavidpb.tuindice.summary.domain.usecase

import android.net.Uri
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.StorageRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.summary.utils.Paths
import java.io.File

class GetProfilePictureFileUseCase(
	private val authRepository: AuthRepository,
	private val storageRepository: StorageRepository
) : EventUseCase<Uri?, Uri, Nothing>() {
	override suspend fun executeOnBackground(params: Uri?): Uri {
		return if (params != null) {
			params
		} else {
			val activeUId = authRepository.getActiveAuth().uid
			val resource = File(Paths.PROFILE_PICTURES, "$activeUId.jpg").path

			return storageRepository.get(resource).toUri()
		}
	}
}