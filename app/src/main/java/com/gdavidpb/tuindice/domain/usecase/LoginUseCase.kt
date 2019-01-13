package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.data.mapper.UsbIdMapper
import com.gdavidpb.tuindice.data.utils.ENDPOINT_DST_ENROLLMENT_AUTH
import com.gdavidpb.tuindice.data.utils.ENDPOINT_DST_RECORD_AUTH
import com.gdavidpb.tuindice.data.utils.asSingle
import com.gdavidpb.tuindice.data.utils.firstOrError
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.base.SingleUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class LoginUseCase(
        private val dstRepository: DstRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val localDatabaseRepository: LocalDatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val baaSRepository: BaaSRepository,
        private val usbIdMapper: UsbIdMapper
) : SingleUseCase<AuthResponse, AuthRequest>(
        subscribeOn = Schedulers.io(),
        observeOn = AndroidSchedulers.mainThread()
) {
    override fun buildUseCaseObservable(params: AuthRequest): Single<AuthResponse> {
        val email = params.usbId.let(usbIdMapper::map)

        val clearCookies = localStorageRepository.delete("cookies", false)
        val enrollmentAuth = dstRepository.auth(params.copy(serviceUrl = ENDPOINT_DST_ENROLLMENT_AUTH)).ensureSchedulers()
        val recordAuth = dstRepository.auth(params.copy(serviceUrl = ENDPOINT_DST_RECORD_AUTH)).ensureSchedulers()
        val activeRemove = localDatabaseRepository.removeActive()
        val signOut = baaSRepository.signOut()
        val sendEmail = baaSRepository.sendSignInLink(email).andThen(settingsRepository.setEmailSentTo(email))

        val auth = sendEmail.andThen(recordAuth.concatWith(enrollmentAuth).firstOrError { it.isSuccessful })
        val reset = signOut.andThen(clearCookies)

        return reset.andThen(auth).flatMap {
            val account = Account(usbId = params.usbId, password = params.password, fullName = it.name)

            val store = localDatabaseRepository.storeAccount(account = account, active = true)

            activeRemove.andThen(store).asSingle(it)
        }
    }
}