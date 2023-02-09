package com.gdavidpb.tuindice.summary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.base.utils.extension.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extension.execute
import com.gdavidpb.tuindice.base.utils.extension.stateInEagerly
import com.gdavidpb.tuindice.base.utils.extension.stateInWhileSubscribed
import com.gdavidpb.tuindice.summary.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import kotlinx.coroutines.flow.MutableSharedFlow

class SummaryViewModel(
	getAccountUseCase: GetAccountUseCase,
	uploadProfilePictureUseCase: UploadProfilePictureUseCase,
	removeProfilePictureUseCase: RemoveProfilePictureUseCase,
	private val signOutUseCase: SignOutUseCase
) : ViewModel() {
	val uploadProfilePicturePath = MutableSharedFlow<String>()
	val removeProfilePictureAction = MutableSharedFlow<Unit>()

	val signOut = LiveCompletable<Nothing>()

	val getAccount =
		stateInWhileSubscribed(useCase = getAccountUseCase, params = Unit)

	val uploadProfilePicture =
		stateInEagerly(useCase = uploadProfilePictureUseCase, paramsFlow = uploadProfilePicturePath)

	val removeProfilePicture =
		stateInEagerly(useCase = removeProfilePictureUseCase, paramsFlow = removeProfilePictureAction)

	fun signOut() =
		execute(useCase = signOutUseCase, params = Unit, liveData = signOut)
}