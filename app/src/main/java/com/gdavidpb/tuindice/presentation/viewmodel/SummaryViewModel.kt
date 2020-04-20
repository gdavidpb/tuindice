package com.gdavidpb.tuindice.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute

class SummaryViewModel(
        private val getAccountUseCase: GetAccountUseCase,
        private val getProfilePictureFileUseCase: GetProfilePictureFileUseCase,
        private val createProfilePictureFileUseCase: CreateProfilePictureFileUseCase,
        private val getProfilePictureUseCase: GetProfilePictureUseCase,
        private val updateProfilePictureUseCase: UpdateProfilePictureUseCase,
        private val removeProfilePictureUseCase: RemoveProfilePictureUseCase
) : ViewModel() {
    val account = LiveResult<Account>()
    val getProfilePictureFile = LiveEvent<Uri>()
    val createProfilePictureFile = LiveEvent<Uri>()
    val profilePicture = LiveResult<String>()
    val updateProfilePicture = LiveEvent<String>()
    val removeProfilePicture = LiveEvent<Unit>()

    fun getAccount() =
            execute(useCase = getAccountUseCase, params = Unit, liveData = account)

    fun getProfilePictureFile(optionalUri: Uri?) =
            execute(useCase = getProfilePictureFileUseCase, params = optionalUri, liveData = getProfilePictureFile)

    fun createProfilePictureFile() =
            execute(useCase = createProfilePictureFileUseCase, params = Unit, liveData = createProfilePictureFile)

    fun getProfilePicture() =
            execute(useCase = getProfilePictureUseCase, params = Unit, liveData = profilePicture)

    fun updateProfilePicture(url: Uri) =
            execute(useCase = updateProfilePictureUseCase, params = url, liveData = updateProfilePicture)

    fun removeProfilePicture() =
            execute(useCase = removeProfilePictureUseCase, params = Unit, liveData = removeProfilePicture)
}