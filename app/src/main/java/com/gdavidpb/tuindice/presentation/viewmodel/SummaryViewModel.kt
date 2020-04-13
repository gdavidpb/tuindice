package com.gdavidpb.tuindice.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.CreateProfilePictureFileUseCase
import com.gdavidpb.tuindice.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.domain.usecase.GetProfilePictureFileUseCase
import com.gdavidpb.tuindice.domain.usecase.UpdateProfilePictureUseCase
import com.gdavidpb.tuindice.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute

class SummaryViewModel(
        private val getAccountUseCase: GetAccountUseCase,
        private val getProfilePictureFileUseCase: GetProfilePictureFileUseCase,
        private val createProfilePictureFileUseCase: CreateProfilePictureFileUseCase,
        private val updateProfilePictureUseCase: UpdateProfilePictureUseCase
) : ViewModel() {
    val account = LiveResult<Account>()
    val getProfilePictureFile = LiveEvent<Uri>()
    val createProfilePictureFile = LiveEvent<Uri>()
    val updateProfilePicture = LiveEvent<String>()

    fun getAccount() =
            execute(useCase = getAccountUseCase, params = Unit, liveData = account)

    fun getProfilePictureFile(optionalUri: Uri?) =
            execute(useCase = getProfilePictureFileUseCase, params = optionalUri, liveData = getProfilePictureFile)

    fun createProfilePictureFile() =
            execute(useCase = createProfilePictureFileUseCase, params = Unit, liveData = createProfilePictureFile)

    fun updateProfilePicture(url: Uri) =
            execute(useCase = updateProfilePictureUseCase, params = url, liveData = updateProfilePicture)
}