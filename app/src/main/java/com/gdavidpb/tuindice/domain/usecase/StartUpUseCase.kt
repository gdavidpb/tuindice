package com.gdavidpb.tuindice.domain.usecase

import android.content.Intent
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.repository.BaaSRepository
import com.gdavidpb.tuindice.domain.repository.LocalDatabaseRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import kotlinx.coroutines.Dispatchers

open class StartUpUseCase(
        private val settingsRepository: SettingsRepository,
        private val localDatabaseRepository: LocalDatabaseRepository,
        private val baaSRepository: BaaSRepository
) : ResultUseCase<Intent, StartUpAction>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Intent): StartUpAction? {
        val emailLink = "${params.data}"

        val activeAccount = suspend { localDatabaseRepository.getActiveAccount() != null }
        val linkReceived = suspend { baaSRepository.isSignInLink(emailLink) }
        val emailSent = suspend { settingsRepository.getEmailSentTo().isNotEmpty() }

        return when {
            activeAccount() -> StartUpAction.MAIN
            linkReceived() -> StartUpAction.EMAIL_LINK
            emailSent() -> StartUpAction.EMAIL_SENT
            else -> StartUpAction.LOGIN
        }
    }
}