package com.gdavidpb.tuindice.summary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.base.utils.extensions.LiveResult
import com.gdavidpb.tuindice.base.utils.extensions.execute
import com.gdavidpb.tuindice.base.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.domain.usecase.*

class SummaryViewModel(
	private val getAccountUseCase: GetAccountUseCase,
	private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
	private val removeProfilePictureUseCase: RemoveProfilePictureUseCase,
	private val signOutUseCase: SignOutUseCase
) : ViewModel() {
	val account = LiveResult<Account, Nothing>()
	val profilePicture = LiveEvent<String, ProfilePictureError>()
	val removeProfilePicture = LiveEvent<Unit, ProfilePictureError>()
	val signOut = LiveCompletable<Nothing>()

	fun getAccount() =
		execute(useCase = getAccountUseCase, params = Unit, liveData = account)

	fun uploadProfilePicture(path: String) =
		execute(
			useCase = uploadProfilePictureUseCase,
			params = path,
			liveData = profilePicture
		)

	fun removeProfilePicture() =
		execute(
			useCase = removeProfilePictureUseCase,
			params = Unit,
			liveData = removeProfilePicture
		)

	fun signOut() =
		execute(useCase = signOutUseCase, params = Unit, liveData = signOut)
}