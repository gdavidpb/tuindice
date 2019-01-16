package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.BaaSRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import kotlinx.coroutines.Dispatchers

open class SignInWithLinkUseCase(
        private val baaSRepository: BaaSRepository,
        private val settingsRepository: SettingsRepository
) : CompletableUseCase<String>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: String) {
        val email = settingsRepository.getEmailSentTo()

        baaSRepository.signInWithLink(email = email, link = params)

        settingsRepository.clearEmailSentTo()
    }
}