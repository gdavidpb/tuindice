package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.data.mapper.UsbIdMapper
import com.gdavidpb.tuindice.data.utils.ENDPOINT_DST_RECORD_AUTH
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import kotlinx.coroutines.Dispatchers

open class LoginUseCase(
        private val dstRepository: DstRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val localDatabaseRepository: LocalDatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val baaSRepository: BaaSRepository,
        private val usbIdMapper: UsbIdMapper
) : ResultUseCase<AuthRequest, AuthResponse>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: AuthRequest): AuthResponse? {
        val email = params.usbId.let(usbIdMapper::map)

        baaSRepository.signOut()
        localStorageRepository.delete("cookies")

        val authResponse = dstRepository.auth(params.copy(serviceUrl = ENDPOINT_DST_RECORD_AUTH))

        if (authResponse != null) {
            baaSRepository.sendSignInLink(email)
            settingsRepository.setEmailSentTo(email)

            val account = Account(
                    usbId = params.usbId,
                    password = params.password,
                    fullName = authResponse.name,
                    email = email
            )

            localDatabaseRepository.removeActive()

            localDatabaseRepository.storeAccount(account = account, active = false)
        }

        return authResponse
    }
}