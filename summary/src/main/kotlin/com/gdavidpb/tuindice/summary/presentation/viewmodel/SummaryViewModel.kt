package com.gdavidpb.tuindice.summary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.base.utils.extension.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extension.LiveEvent
import com.gdavidpb.tuindice.base.utils.extension.LiveResult
import com.gdavidpb.tuindice.base.utils.extension.execute
import com.gdavidpb.tuindice.summary.domain.error.GetAccountError
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase

class SummaryViewModel(
	private val getAccountUseCase: GetAccountUseCase,
	private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
	private val removeProfilePictureUseCase: RemoveProfilePictureUseCase,
	private val signOutUseCase: SignOutUseCase
) : ViewModel() {
	val account = LiveResult<Account, GetAccountError>()
	val uploadProfilePicture = LiveEvent<String, ProfilePictureError>()
	val removeProfilePicture = LiveEvent<Unit, ProfilePictureError>()
	val signOut = LiveCompletable<Nothing>()

	fun getAccount() =
		execute(useCase = getAccountUseCase, params = Unit, liveData = account)

	fun uploadProfilePicture(path: String) =
		execute(
			useCase = uploadProfilePictureUseCase,
			params = path,
			liveData = uploadProfilePicture
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