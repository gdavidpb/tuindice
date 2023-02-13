package com.gdavidpb.tuindice.summary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.emit
import com.gdavidpb.tuindice.base.utils.extension.emptyStateFlow
import com.gdavidpb.tuindice.base.utils.extension.stateInAction
import com.gdavidpb.tuindice.base.utils.extension.stateInFlow
import com.gdavidpb.tuindice.summary.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase

class SummaryViewModel(
	getAccountUseCase: GetAccountUseCase,
	uploadProfilePictureUseCase: UploadProfilePictureUseCase,
	removeProfilePictureUseCase: RemoveProfilePictureUseCase
) : ViewModel() {
	private val uploadProfilePictureParams = emptyStateFlow<String>()
	private val removeProfilePictureParams = emptyStateFlow<Unit>()

	fun uploadProfilePicture(path: String) =
		emit(uploadProfilePictureParams, path)

	fun removeProfilePicture() =
		emit(removeProfilePictureParams, Unit)

	val getAccount =
		stateInFlow(useCase = getAccountUseCase, params = Unit)

	val uploadProfilePicture =
		stateInAction(useCase = uploadProfilePictureUseCase, paramsFlow = uploadProfilePictureParams)

	val removeProfilePicture =
		stateInAction(useCase = removeProfilePictureUseCase, paramsFlow = removeProfilePictureParams)
}