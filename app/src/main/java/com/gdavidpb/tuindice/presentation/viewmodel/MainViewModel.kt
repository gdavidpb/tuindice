package com.gdavidpb.tuindice.presentation.viewmodel

import android.net.Uri
import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.domain.usecase.request.SubjectUpdateRequest
import com.gdavidpb.tuindice.domain.usecase.response.SyncResponse
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute
import java.io.File

class MainViewModel(
        private val signOutUseCase: SignOutUseCase,
        private val startUpUseCase: StartUpUseCase,
        private val syncAccountUseCase: SyncAccountUseCase,
        private val setLastScreenUseCase: SetLastScreenUseCase,
        private val getAccountUseCase: GetAccountUseCase,
        private val getQuartersUseCase: GetQuartersUseCase,
        private val updateSubjectUseCase: UpdateSubjectUseCase,
        private val openEnrollmentProofUseCase: OpenEnrollmentProofUseCase,
        private val getProfilePictureFileUseCase: GetProfilePictureFileUseCase,
        private val createProfilePictureFileUseCase: CreateProfilePictureFileUseCase,
        private val updateProfilePictureUseCase: UpdateProfilePictureUseCase
) : ViewModel() {
    val signOut = LiveCompletable()
    val fetchStartUpAction = LiveResult<StartUpAction>()
    val sync = LiveResult<SyncResponse>()
    val lastScreen = LiveCompletable()
    val account = LiveResult<Account>()
    val quarters = LiveResult<List<Quarter>>()
    val subjectUpdate = LiveCompletable()
    val enrollment = LiveResult<File>()
    val getProfilePictureFile = LiveResult<Uri>()
    val createProfilePictureFile = LiveResult<Uri>()
    val updateProfilePicture = LiveResult<String>()

    fun signOut() =
            execute(useCase = signOutUseCase, params = Unit, liveData = signOut)

    fun fetchStartUpAction(dataString: String) =
            execute(useCase = startUpUseCase, params = dataString, liveData = fetchStartUpAction)

    fun trySyncAccount() =
            execute(useCase = syncAccountUseCase, params = Unit, liveData = sync)

    fun setLastScreen(@IdRes navId: Int) =
            execute(useCase = setLastScreenUseCase, params = navId, liveData = lastScreen)

    fun getAccount() =
            execute(useCase = getAccountUseCase, params = Unit, liveData = account)

    fun getQuarters() =
            execute(useCase = getQuartersUseCase, params = Unit, liveData = quarters)

    fun updateSubject(sid: String, grade: Int) =
            execute(useCase = updateSubjectUseCase, params = SubjectUpdateRequest(sid, grade), liveData = subjectUpdate)

    fun openEnrollmentProof() =
            execute(useCase = openEnrollmentProofUseCase, params = Unit, liveData = enrollment)

    fun getProfilePictureFile(optionalUri: Uri?) =
            execute(useCase = getProfilePictureFileUseCase, params = optionalUri, liveData = getProfilePictureFile)

    fun createProfilePictureFile() =
            execute(useCase = createProfilePictureFileUseCase, params = Unit, liveData = createProfilePictureFile)

    fun updateProfilePicture(url: Uri) =
            execute(useCase = updateProfilePictureUseCase, params = url, liveData = updateProfilePicture)
}
