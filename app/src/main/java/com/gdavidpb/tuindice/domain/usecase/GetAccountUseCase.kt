package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.repository.RemoteStorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.utils.PATH_PROFILE_PICTURES
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import java.io.File

@IgnoredExceptions(CancellationException::class)
open class GetAccountUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val remoteStorageRepository: RemoteStorageRepository
) : ResultUseCase<Unit, Account>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Unit): Account? {
        val activeUId = authRepository.getActiveAuth().uid

        return databaseRepository.getAccount(uid = activeUId).let { account ->
            if (account.hasProfilePicture) {
                val resource = File(PATH_PROFILE_PICTURES, "$activeUId.jpg").path

                val profilePicture = remoteStorageRepository.resolveResource(resource).toString()

                account.copy(profilePicture = profilePicture)
            } else {
                account
            }
        }
    }
}